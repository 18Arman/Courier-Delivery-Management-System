package com.smartcourier.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.smartcourier.admin.dto.ExceptionCaseRequest;
import com.smartcourier.admin.dto.HubRequest;
import com.smartcourier.admin.dto.UserAdminRequest;
import com.smartcourier.admin.entity.DeliveryExceptionCase;
import com.smartcourier.admin.entity.Hub;
import com.smartcourier.admin.entity.ManagedUser;
import com.smartcourier.admin.entity.ReportRecord;
import com.smartcourier.admin.exception.ResourceNotFoundException;
import com.smartcourier.admin.integration.DeliveryEventMessage;
import com.smartcourier.admin.integration.DeliveryServiceFacade;
import com.smartcourier.admin.integration.DeliveryStatsClientResponse;
import com.smartcourier.admin.integration.DeliverySummaryClientResponse;
import com.smartcourier.admin.repository.DeliveryExceptionCaseRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.repository.ManagedUserRepository;
import com.smartcourier.admin.repository.ReportRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private HubRepository hubRepository;
    @Mock
    private ManagedUserRepository managedUserRepository;
    @Mock
    private DeliveryExceptionCaseRepository deliveryExceptionCaseRepository;
    @Mock
    private ReportRecordRepository reportRecordRepository;
    @Mock
    private DeliveryServiceFacade deliveryServiceFacade;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(hubRepository, managedUserRepository, deliveryExceptionCaseRepository, reportRecordRepository, deliveryServiceFacade);
    }

    @Test
    void createHubShouldPersistHub() {
        HubRequest request = new HubRequest("DEL-01", "Delhi", "DL", "Rahul", true);
        when(hubRepository.save(org.mockito.ArgumentMatchers.any(Hub.class))).thenAnswer(invocation -> {
            Hub hub = invocation.getArgument(0);
            hub.setId(1L);
            return hub;
        });

        var response = adminService.createHub(request);

        assertEquals("DEL-01", response.hubCode());
        assertEquals("Delhi", response.city());
    }

    @Test
    void dashboardShouldCombineLocalAndRemoteStats() {
        when(hubRepository.count()).thenReturn(1L);
        when(managedUserRepository.count()).thenReturn(2L);
        when(reportRecordRepository.count()).thenReturn(3L);
        when(deliveryExceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of());
        when(deliveryServiceFacade.fetchStats()).thenReturn(new DeliveryStatsClientResponse(4, 5, 6, 1));
        when(deliveryServiceFacade.isHealthy()).thenReturn(true);

        var dashboard = adminService.dashboard();

        assertEquals(4L, dashboard.bookedDeliveries());
        assertEquals("UP", dashboard.deliveryServiceState());
    }

    @Test
    void createExceptionCaseShouldUseDeliveryOverview() {
        when(deliveryServiceFacade.fetchDeliverySummary(1L)).thenReturn(
                new DeliverySummaryClientResponse(1L, "SC123", "aman@example.com", "EXPRESS", "DELAYED", BigDecimal.TEN, LocalDate.now())
        );
        when(deliveryExceptionCaseRepository.save(org.mockito.ArgumentMatchers.any(DeliveryExceptionCase.class))).thenAnswer(invocation -> {
            DeliveryExceptionCase exceptionCase = invocation.getArgument(0);
            exceptionCase.setId(1L);
            return exceptionCase;
        });

        var response = adminService.createExceptionCase(new ExceptionCaseRequest(1L, "SC123", "DELAYED", "Weather issue"));

        assertEquals("SC123", response.trackingNumber());
        assertFalse(response.downstreamSyncPending());
    }

    @Test
    void processDeliveryEventShouldCreateExceptionForFailureState() {
        DeliveryEventMessage message = new DeliveryEventMessage();
        message.setDeliveryId(1L);
        message.setTrackingNumber("SC123");
        message.setStatus("DELAYED");
        when(deliveryExceptionCaseRepository.existsByTrackingNumberAndExceptionStatusAndResolvedFalse("SC123", "DELAYED")).thenReturn(false);

        adminService.processDeliveryEvent(message);

        org.mockito.Mockito.verify(deliveryExceptionCaseRepository).save(org.mockito.ArgumentMatchers.any(DeliveryExceptionCase.class));
    }

    @Test
    void getDeliveryOverviewShouldMapSummary() {
        when(deliveryServiceFacade.fetchDeliverySummary(5L)).thenReturn(
                new DeliverySummaryClientResponse(5L, "SC5", "user@example.com", "EXPRESS", "BOOKED", BigDecimal.TEN, LocalDate.now())
        );

        var response = adminService.getDeliveryOverview(5L);

        assertEquals("SC5", response.trackingNumber());
        assertEquals("BOOKED", response.status());
    }

    @Test
    void getHubsShouldReturnMappedResults() {
        Hub hub = new Hub();
        hub.setId(2L);
        hub.setHubCode("MUM-01");
        hub.setCity("Mumbai");
        hub.setState("MH");
        hub.setManagerName("Priya");
        hub.setActive(true);
        when(hubRepository.findAll()).thenReturn(List.of(hub));

        var response = adminService.getHubs();

        assertEquals(1, response.size());
        assertEquals("MUM-01", response.get(0).hubCode());
    }

    @Test
    void createUserShouldPersistManagedUser() {
        UserAdminRequest request = new UserAdminRequest("Admin User", "admin@example.com", "ROLE_ADMIN", true);
        when(managedUserRepository.save(org.mockito.ArgumentMatchers.any(ManagedUser.class))).thenAnswer(invocation -> {
            ManagedUser user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });

        var response = adminService.createUser(request);

        assertEquals("admin@example.com", response.email());
        assertEquals("ROLE_ADMIN", response.roleName());
    }

    @Test
    void getUsersShouldReturnMappedResults() {
        ManagedUser user = new ManagedUser();
        user.setId(1L);
        user.setFullName("Aman");
        user.setEmail("aman@example.com");
        user.setRoleName("ROLE_CUSTOMER");
        user.setActive(true);
        when(managedUserRepository.findAll()).thenReturn(List.of(user));

        var response = adminService.getUsers();

        assertEquals(1, response.size());
        assertEquals("Aman", response.get(0).fullName());
    }

    @Test
    void getOpenExceptionsShouldReturnMappedResults() {
        DeliveryExceptionCase exceptionCase = new DeliveryExceptionCase();
        exceptionCase.setId(10L);
        exceptionCase.setDeliveryId(1L);
        exceptionCase.setTrackingNumber("SC123");
        exceptionCase.setExceptionStatus("DELAYED");
        exceptionCase.setIssueDescription("Weather");
        when(deliveryExceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of(exceptionCase));

        var response = adminService.getOpenExceptions();

        assertEquals(1, response.size());
        assertEquals("DELAYED", response.get(0).exceptionStatus());
    }

    @Test
    void resolveExceptionShouldThrowWhenMissing() {
        when(deliveryExceptionCaseRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.resolveException(99L, "Admin"));
    }

    @Test
    void resolveExceptionShouldMarkResolved() {
        DeliveryExceptionCase exceptionCase = new DeliveryExceptionCase();
        exceptionCase.setId(1L);
        when(deliveryExceptionCaseRepository.findById(1L)).thenReturn(java.util.Optional.of(exceptionCase));
        when(deliveryExceptionCaseRepository.save(exceptionCase)).thenReturn(exceptionCase);

        var response = adminService.resolveException(1L, "Admin");

        assertEquals(true, response.resolved());
        assertEquals("Admin", response.resolvedBy());
    }

    @Test
    void generateOperationalReportShouldPersistSummary() {
        when(deliveryExceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of(new DeliveryExceptionCase()));
        when(hubRepository.count()).thenReturn(2L);
        when(managedUserRepository.count()).thenReturn(3L);
        when(reportRecordRepository.save(org.mockito.ArgumentMatchers.any(ReportRecord.class))).thenAnswer(invocation -> {
            ReportRecord report = invocation.getArgument(0);
            report.setId(8L);
            return report;
        });

        var response = adminService.generateOperationalReport();

        assertEquals("OPERATIONS", response.reportType());
        assertEquals(8L, response.id());
    }

    @Test
    void getReportsShouldReturnSortedList() {
        ReportRecord report = new ReportRecord();
        report.setId(1L);
        report.setReportName("Ops");
        report.setReportType("OPERATIONS");
        report.setSummary("Summary");
        when(reportRecordRepository.findAllByOrderByGeneratedAtDesc()).thenReturn(List.of(report));

        var response = adminService.getReports();

        assertEquals(1, response.size());
        assertEquals("Ops", response.get(0).reportName());
    }

    @Test
    void processDeliveryEventShouldIgnoreNonExceptionStatuses() {
        DeliveryEventMessage message = new DeliveryEventMessage();
        message.setStatus("IN_TRANSIT");

        adminService.processDeliveryEvent(message);

        org.mockito.Mockito.verify(deliveryExceptionCaseRepository, org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any(DeliveryExceptionCase.class));
    }
}
