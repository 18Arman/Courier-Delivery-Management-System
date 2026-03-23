package com.smartcourier.admin.service;

import com.smartcourier.admin.dto.DashboardResponse;
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
import com.smartcourier.admin.repository.DeliveryExceptionCaseRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.repository.ManagedUserRepository;
import com.smartcourier.admin.repository.ReportRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final HubRepository hubRepository;
    private final ManagedUserRepository managedUserRepository;
    private final DeliveryExceptionCaseRepository exceptionCaseRepository;
    private final ReportRecordRepository reportRecordRepository;

    public AdminService(HubRepository hubRepository,
                        ManagedUserRepository managedUserRepository,
                        DeliveryExceptionCaseRepository exceptionCaseRepository,
                        ReportRecordRepository reportRecordRepository) {
        this.hubRepository = hubRepository;
        this.managedUserRepository = managedUserRepository;
        this.exceptionCaseRepository = exceptionCaseRepository;
        this.reportRecordRepository = reportRecordRepository;
    }

    public DashboardResponse dashboard() {
        return new DashboardResponse(
                hubRepository.count(),
                managedUserRepository.count(),
                exceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc().size(),
                reportRecordRepository.count()
        );
    }

    @Transactional
    public HubResponse createHub(HubRequest request) {
        Hub hub = new Hub();
        hub.setHubCode(request.hubCode());
        hub.setCity(request.city());
        hub.setState(request.state());
        hub.setManagerName(request.managerName());
        hub.setActive(request.active());
        return toResponse(hubRepository.save(hub));
    }

    public List<HubResponse> getHubs() {
        return hubRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserAdminResponse createUser(UserAdminRequest request) {
        ManagedUser user = new ManagedUser();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRoleName(request.roleName());
        user.setActive(request.active());
        return toResponse(managedUserRepository.save(user));
    }

    public List<UserAdminResponse> getUsers() {
        return managedUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExceptionCaseResponse createExceptionCase(ExceptionCaseRequest request) {
        DeliveryExceptionCase exceptionCase = new DeliveryExceptionCase();
        exceptionCase.setDeliveryId(request.deliveryId());
        exceptionCase.setTrackingNumber(request.trackingNumber());
        exceptionCase.setExceptionStatus(request.exceptionStatus());
        exceptionCase.setIssueDescription(request.issueDescription());
        return toResponse(exceptionCaseRepository.save(exceptionCase));
    }

    public List<ExceptionCaseResponse> getOpenExceptions() {
        return exceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExceptionCaseResponse resolveException(Long id, String resolvedBy) {
        DeliveryExceptionCase exceptionCase = exceptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exception case not found"));
        exceptionCase.setResolved(true);
        exceptionCase.setResolvedBy(resolvedBy);
        exceptionCase.setResolvedAt(LocalDateTime.now());
        return toResponse(exceptionCaseRepository.save(exceptionCase));
    }

    @Transactional
    public ReportResponse generateOperationalReport() {
        ReportRecord record = new ReportRecord();
        record.setReportName("Operations Snapshot");
        record.setReportType("OPERATIONS");
        record.setSummary("Open exceptions: " + exceptionCaseRepository.findByResolvedFalseOrderByCreatedAtDesc().size()
                + ", active hubs: " + hubRepository.count()
                + ", active users: " + managedUserRepository.count());
        return toResponse(reportRecordRepository.save(record));
    }

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
                exceptionCase.getCreatedAt(),
                exceptionCase.getResolvedAt()
        );
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
