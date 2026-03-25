package com.smartcourier.admin.service;

import com.smartcourier.admin.dto.DashboardResponse;
import com.smartcourier.admin.dto.DeliverySummaryView;
import com.smartcourier.admin.dto.ExceptionCaseRequest;
import com.smartcourier.admin.dto.ExceptionCaseResponse;
import com.smartcourier.admin.dto.HubRequest;
import com.smartcourier.admin.dto.HubResponse;
import com.smartcourier.admin.dto.ReportResponse;
import com.smartcourier.admin.dto.UserAdminRequest;
import com.smartcourier.admin.dto.UserAdminResponse;
import com.smartcourier.admin.entity.DeliveryExceptionCase;
import com.smartcourier.admin.entity.Hub;
import com.smartcourier.admin.entity.ManagedUser;
import com.smartcourier.admin.entity.ReportRecord;
import com.smartcourier.admin.exception.ResourceNotFoundException;
import com.smartcourier.admin.integration.DeliveryEventMessage;
import com.smartcourier.admin.integration.DeliveryServiceFacade;
import com.smartcourier.admin.repository.DeliveryExceptionCaseRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.repository.ManagedUserRepository;
import com.smartcourier.admin.repository.ReportRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final HubRepository hubRepository;
    private final ManagedUserRepository managedUserRepository;
    private final DeliveryExceptionCaseRepository exceptionCaseRepository;
    private final ReportRecordRepository reportRecordRepository;
    private final DeliveryServiceFacade deliveryServiceFacade;

    public AdminService(HubRepository hubRepository,
                        ManagedUserRepository managedUserRepository,
                        DeliveryExceptionCaseRepository exceptionCaseRepository,
                        ReportRecordRepository reportRecordRepository,
                        DeliveryServiceFacade deliveryServiceFacade) {
        this.hubRepository = hubRepository;
        this.managedUserRepository = managedUserRepository;
        this.exceptionCaseRepository = exceptionCaseRepository;
        this.reportRecordRepository = reportRecordRepository;
        this.deliveryServiceFacade = deliveryServiceFacade;
    }

    @Cacheable("adminDashboard")
    public DashboardResponse dashboard() {
        var deliveryStats = deliveryServiceFacade.fetchStats();
        boolean deliveryHealthy = deliveryServiceFacade.isHealthy();
        return new DashboardResponse(
                hubRepository.count(),
                managedUserRepository.count(),
                exceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc().size(),
                reportRecordRepository.count(),
                deliveryHealthy ? deliveryStats.bookedCount() : 0,
                deliveryHealthy ? deliveryStats.inTransitCount() : 0,
                deliveryHealthy ? deliveryStats.deliveredCount() : 0,
                deliveryHealthy ? deliveryStats.exceptionCount() : 0,
                deliveryHealthy ? "UP" : "DEGRADED"
        );
    }

    @Transactional
    @CacheEvict(value = {"adminDashboard", "adminHubs"}, allEntries = true)
    public HubResponse createHub(HubRequest request) {
        Hub hub = new Hub();
        hub.setHubCode(request.hubCode());
        hub.setCity(request.city());
        hub.setState(request.state());
        hub.setManagerName(request.managerName());
        hub.setActive(request.active());
        return toResponse(hubRepository.save(hub));
    }

    @Cacheable("adminHubs")
    public List<HubResponse> getHubs() {
        return hubRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = {"adminDashboard", "adminUsers"}, allEntries = true)
    public UserAdminResponse createUser(UserAdminRequest request) {
        ManagedUser user = new ManagedUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRoleName(request.roleName());
        user.setActive(request.active());
        return toResponse(managedUserRepository.save(user));
    }

    @Cacheable("adminUsers")
    public List<UserAdminResponse> getUsers() {
        return managedUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    @CacheEvict(value = {"adminDashboard", "adminOpenExceptions"}, allEntries = true)
    public ExceptionCaseResponse createExceptionCase(ExceptionCaseRequest request) {
        DeliverySummaryView deliverySummary = getDeliveryOverview(request.deliveryId());
        DeliveryExceptionCase exceptionCase = new DeliveryExceptionCase();
        exceptionCase.setDeliveryId(request.deliveryId());
        exceptionCase.setTrackingNumber(resolveTrackingNumber(request.trackingNumber(), deliverySummary));
        exceptionCase.setExceptionStatus(request.exceptionStatus());
        exceptionCase.setIssueDescription(request.issueDescription() + " | delivery-status=" + deliverySummary.status());
        exceptionCase.setDownstreamSyncPending("UNAVAILABLE".equalsIgnoreCase(deliverySummary.trackingNumber()));
        return toResponse(exceptionCaseRepository.save(exceptionCase));
    }

    @Cacheable("adminOpenExceptions")
    public List<ExceptionCaseResponse> getOpenExceptions() {
        return exceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminOpenExceptions", allEntries = true),
            @CacheEvict(value = "adminDashboard", allEntries = true)
    })
    public ExceptionCaseResponse resolveException(Long id, String resolvedBy) {
        DeliveryExceptionCase exceptionCase = exceptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exception case not found"));
        exceptionCase.setResolved(true);
        exceptionCase.setResolvedBy(resolvedBy);
        exceptionCase.setResolvedAt(LocalDateTime.now());
        return toResponse(exceptionCaseRepository.save(exceptionCase));
    }

    @Cacheable(value = "adminDeliveryOverview", key = "#deliveryId")
    public DeliverySummaryView getDeliveryOverview(Long deliveryId) {
        var summary = deliveryServiceFacade.fetchDeliverySummary(deliveryId);
        return new DeliverySummaryView(
                summary.id(),
                summary.trackingNumber(),
                summary.customerEmail(),
                summary.serviceType(),
                summary.status(),
                summary.courierCharge(),
                summary.pickupDate()
        );
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "adminOpenExceptions", allEntries = true),
            @CacheEvict(value = "adminDashboard", allEntries = true)
    })
    public void processDeliveryEvent(DeliveryEventMessage message) {
        if (!List.of("DELAYED", "FAILED", "RETURNED").contains(message.getStatus())) {
            return;
        }
        if (exceptionCaseRepository.existsByTrackingNumberAndExceptionStatusAndResolvedFalse(message.getTrackingNumber(), message.getStatus())) {
            return;
        }
        DeliveryExceptionCase exceptionCase = new DeliveryExceptionCase();
        exceptionCase.setDeliveryId(message.getDeliveryId());
        exceptionCase.setTrackingNumber(message.getTrackingNumber());
        exceptionCase.setExceptionStatus(message.getStatus());
        exceptionCase.setIssueDescription("Asynchronous exception captured from delivery service event bus");
        exceptionCase.setDownstreamSyncPending(false);
        exceptionCaseRepository.save(exceptionCase);
    }

    @Transactional
    @CacheEvict(value = {"adminDashboard", "adminReports"}, allEntries = true)
    public ReportResponse generateOperationalReport() {
        ReportRecord record = new ReportRecord();
        record.setReportName("Operations Snapshot");
        record.setReportType("OPERATIONS");
        record.setSummary("Open exceptions: " + exceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc().size()
                + ", active hubs: " + hubRepository.count()
                + ", active users: " + managedUserRepository.count());
        return toResponse(reportRecordRepository.save(record));
    }

    @Cacheable("adminReports")
    public List<ReportResponse> getReports() {
        return reportRecordRepository.findAllByOrderByGeneratedAtDesc().stream().map(this::toResponse).toList();
    }

    private HubResponse toResponse(Hub hub) {
        return new HubResponse(hub.getId(), hub.getHubCode(), hub.getCity(), hub.getState(), hub.getManagerName(), hub.isActive());
    }

    private UserAdminResponse toResponse(ManagedUser user) {
        return new UserAdminResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRoleName(), user.isActive());
    }

    private ExceptionCaseResponse toResponse(DeliveryExceptionCase exceptionCase) {
        return new ExceptionCaseResponse(
                exceptionCase.getId(),
                exceptionCase.getDeliveryId(),
                exceptionCase.getTrackingNumber(),
                exceptionCase.getExceptionStatus(),
                exceptionCase.getIssueDescription(),
                exceptionCase.getResolvedBy(),
                exceptionCase.isResolved(),
                exceptionCase.isDownstreamSyncPending(),
                exceptionCase.getCreatedAt(),
                exceptionCase.getResolvedAt()
        );
    }

    private String resolveTrackingNumber(String requestedTrackingNumber, DeliverySummaryView summary) {
        if (summary.trackingNumber() != null && !"UNAVAILABLE".equalsIgnoreCase(summary.trackingNumber())) {
            return summary.trackingNumber();
        }
        return requestedTrackingNumber;
    }

    private ReportResponse toResponse(ReportRecord reportRecord) {
        return new ReportResponse(
                reportRecord.getId(),
                reportRecord.getReportName(),
                reportRecord.getReportType(),
                reportRecord.getSummary(),
                reportRecord.getGeneratedAt()
        );
    }
}
