package com.smartcourier.delivery.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartcourier.delivery.dto.AddressRequest;
import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.dto.DeliveryStatsResponse;
import com.smartcourier.delivery.dto.DeliverySummaryResponse;
import com.smartcourier.delivery.dto.PackageRequest;
import com.smartcourier.delivery.dto.UpdateStatusRequest;
import com.smartcourier.delivery.entity.DeliveryStatus;
import com.smartcourier.delivery.entity.ServiceType;
import com.smartcourier.delivery.service.DeliveryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;

    private DeliveryController deliveryController;
    private InternalDeliveryController internalDeliveryController;

    @BeforeEach
    void setUp() {
        deliveryController = new DeliveryController(deliveryService);
        internalDeliveryController = new InternalDeliveryController(deliveryService);
    }

    @Test
    void createShouldReturnCreated() {
        CreateDeliveryRequest request = sampleRequest();
        DeliveryResponse response = sampleResponse();
        var authentication = new UsernamePasswordAuthenticationToken("aman@example.com", null);
        when(deliveryService.create("aman@example.com", request)).thenReturn(response);

        var result = deliveryController.create(request, authentication);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(deliveryService).create("aman@example.com", request);
    }

    @Test
    void myDeliveriesShouldReturnList() {
        DeliveryResponse response = sampleResponse();
        var authentication = new UsernamePasswordAuthenticationToken("aman@example.com", null);
        when(deliveryService.getMyDeliveries("aman@example.com")).thenReturn(List.of(response));

        var result = deliveryController.myDeliveries(authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertSame(response, result.getBody().get(0));
        verify(deliveryService).getMyDeliveries("aman@example.com");
    }

    @Test
    void getByIdShouldDelegateAdminCheck() {
        DeliveryResponse response = sampleResponse();
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin@smartcourier.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(deliveryService.isAdmin(any())).thenReturn(true);
        when(deliveryService.getById(1L, "admin@smartcourier.com", true)).thenReturn(response);

        var result = deliveryController.getById(1L, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(deliveryService).isAdmin(any());
        verify(deliveryService).getById(1L, "admin@smartcourier.com", true);
    }

    @Test
    void updateStatusShouldReturnUpdatedDelivery() {
        DeliveryResponse response = sampleResponse();
        when(deliveryService.updateStatus(1L, DeliveryStatus.IN_TRANSIT)).thenReturn(response);

        var result = deliveryController.updateStatus(1L, new UpdateStatusRequest(DeliveryStatus.IN_TRANSIT));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(deliveryService).updateStatus(1L, DeliveryStatus.IN_TRANSIT);
    }

    @Test
    void internalSummaryShouldReturnDeliverySummary() {
        DeliverySummaryResponse response = new DeliverySummaryResponse(
                1L,
                "SC123",
                "aman@example.com",
                ServiceType.EXPRESS,
                DeliveryStatus.BOOKED,
                BigDecimal.TEN,
                LocalDate.now()
        );
        when(deliveryService.getSummaryById(1L)).thenReturn(response);

        var result = internalDeliveryController.getSummary(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(deliveryService).getSummaryById(1L);
    }

    @Test
    void internalStatsShouldReturnCurrentCounts() {
        DeliveryStatsResponse response = new DeliveryStatsResponse(1, 2, 3, 4);
        when(deliveryService.getStats()).thenReturn(response);

        var result = internalDeliveryController.getStats();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(deliveryService).getStats();
    }

    private CreateDeliveryRequest sampleRequest() {
        return new CreateDeliveryRequest(
                ServiceType.EXPRESS,
                new AddressRequest("Sender", "9999999999", "Line 1", "Delhi", "DL", "110001", "India"),
                new AddressRequest("Receiver", "8888888888", "Line 2", "Mumbai", "MH", "400001", "India"),
                new PackageRequest("Docs", BigDecimal.ONE, BigDecimal.TEN, "10x10x2", "Note"),
                LocalDate.now()
        );
    }

    private DeliveryResponse sampleResponse() {
        return new DeliveryResponse(
                1L,
                "SC123",
                "aman@example.com",
                ServiceType.EXPRESS,
                DeliveryStatus.BOOKED,
                BigDecimal.TEN,
                LocalDate.now(),
                new AddressRequest("Sender", "9999999999", "Line 1", "Delhi", "DL", "110001", "India"),
                new AddressRequest("Receiver", "8888888888", "Line 2", "Mumbai", "MH", "400001", "India"),
                new PackageRequest("Docs", BigDecimal.ONE, BigDecimal.TEN, "10x10x2", "Note"),
                LocalDateTime.now()
        );
    }
}
