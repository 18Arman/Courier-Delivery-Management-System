package com.smartcourier.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.smartcourier.admin.dto.HubRequest;
import com.smartcourier.admin.entity.Hub;
import com.smartcourier.admin.repository.DeliveryExceptionCaseRepository;
import com.smartcourier.admin.repository.HubRepository;
import com.smartcourier.admin.repository.ManagedUserRepository;
import com.smartcourier.admin.repository.ReportRecordRepository;
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

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(hubRepository, managedUserRepository, deliveryExceptionCaseRepository, reportRecordRepository);
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
}

