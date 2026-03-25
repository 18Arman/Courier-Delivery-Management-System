package com.smartcourier.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcourier.admin.dto.DashboardResponse;
import com.smartcourier.admin.dto.DeliverySummaryView;
import com.smartcourier.admin.dto.ExceptionCaseResponse;
import com.smartcourier.admin.dto.ResolveExceptionRequest;
import com.smartcourier.admin.dto.ReportResponse;
import com.smartcourier.admin.security.JwtService;
import com.smartcourier.admin.service.AdminService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.smartcourier.admin.exception.GlobalExceptionHandler.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "admin@smartcourier.com", roles = "ADMIN")
    void dashboardShouldReturnMetricsForAdmin() throws Exception {
        when(adminService.dashboard()).thenReturn(new DashboardResponse(1, 2, 3, 4, 5, 6, 7, 8, "UP"));

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryServiceState").value("UP"));
    }

    @Test
    @WithMockUser(username = "admin@smartcourier.com", roles = "ADMIN")
    void deliveryOverviewShouldReturnLiveView() throws Exception {
        when(adminService.getDeliveryOverview(1L))
                .thenReturn(new DeliverySummaryView(1L, "SC123", "aman@example.com", "EXPRESS", "BOOKED", java.math.BigDecimal.TEN, LocalDate.now()));

        mockMvc.perform(get("/api/v1/admin/deliveries/1/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("SC123"));
    }

    @Test
    @WithMockUser(username = "admin@smartcourier.com", roles = "ADMIN")
    void resolveShouldReturnUpdatedException() throws Exception {
        when(adminService.resolveException(any(), any()))
                .thenReturn(new ExceptionCaseResponse(1L, 1L, "SC123", "DELAYED", "Issue", "System Admin", true, false, LocalDateTime.now(), LocalDateTime.now()));

        mockMvc.perform(put("/api/v1/admin/deliveries/1/resolve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new ResolveExceptionRequest("System Admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true));
    }

    @Test
    @WithMockUser(username = "admin@smartcourier.com", roles = "ADMIN")
    void reportsShouldReturnList() throws Exception {
        when(adminService.getReports())
                .thenReturn(List.of(new ReportResponse(1L, "Ops", "OPERATIONS", "Summary", LocalDateTime.now())));

        mockMvc.perform(get("/api/v1/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportName").value("Ops"));
    }
}
