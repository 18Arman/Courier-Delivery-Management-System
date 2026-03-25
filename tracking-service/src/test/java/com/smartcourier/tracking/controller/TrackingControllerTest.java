package com.smartcourier.tracking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcourier.tracking.dto.DeliveryProofRequest;
import com.smartcourier.tracking.dto.DeliveryProofResponse;
import com.smartcourier.tracking.dto.DocumentUploadResponse;
import com.smartcourier.tracking.dto.TrackingEventResponse;
import com.smartcourier.tracking.security.JwtService;
import com.smartcourier.tracking.service.TrackingService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TrackingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.smartcourier.tracking.exception.GlobalExceptionHandler.class)
class TrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrackingService trackingService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "aman@example.com", roles = "CUSTOMER")
    void trackShouldReturnTimeline() throws Exception {
        when(trackingService.getTimeline("SC123"))
                .thenReturn(List.of(new TrackingEventResponse(1L, "SC123", "IN_TRANSIT", "Hub", "Moved", LocalDateTime.now())));

        mockMvc.perform(get("/api/v1/tracking/SC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trackingNumber").value("SC123"));
    }

    @Test
    @WithMockUser(username = "aman@example.com", roles = "CUSTOMER")
    void uploadShouldAcceptMultipart() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "invoice.txt", "text/plain", "hello".getBytes());
        when(trackingService.uploadDocument(any(), any()))
                .thenReturn(new DocumentUploadResponse(1L, "SC123", "invoice.txt", "uploads/tracking/invoice.txt", LocalDateTime.now()));

        mockMvc.perform(multipart("/api/v1/tracking/documents/upload")
                        .file(file)
                        .param("trackingNumber", "SC123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("invoice.txt"));
    }

    @Test
    @WithMockUser(username = "admin@smartcourier.com", roles = "ADMIN")
    void saveProofShouldRequireAdmin() throws Exception {
        when(trackingService.saveProof(any()))
                .thenReturn(new DeliveryProofResponse(1L, "SC123", "Priya", "Delivered", "proof.jpg", LocalDateTime.now()));

        mockMvc.perform(put("/api/v1/tracking/proof")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeliveryProofRequest("SC123", "Priya", "Delivered", "proof.jpg"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientName").value("Priya"));
    }
}
