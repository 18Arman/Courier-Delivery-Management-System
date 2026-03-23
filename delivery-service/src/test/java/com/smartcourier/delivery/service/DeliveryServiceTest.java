package com.smartcourier.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.smartcourier.delivery.dto.AddressRequest;
import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.PackageRequest;
import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.entity.DeliveryStatus;
import com.smartcourier.delivery.entity.ServiceType;
import com.smartcourier.delivery.exception.ResourceNotFoundException;
import com.smartcourier.delivery.repository.DeliveryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryRepository);
    }

    @Test
    void createShouldCalculateCharge() {
        CreateDeliveryRequest request = new CreateDeliveryRequest(
                ServiceType.EXPRESS,
                new AddressRequest("Sender", "9999999999", "Line 1", "Delhi", "DL", "110001", "India"),
                new AddressRequest("Receiver", "8888888888", "Line 2", "Mumbai", "MH", "400001", "India"),
                new PackageRequest("Box", BigDecimal.valueOf(2), BigDecimal.valueOf(1000), "10x10x10", "Fragile"),
                LocalDate.now()
        );
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery delivery = invocation.getArgument(0);
            delivery.setId(1L);
            return delivery;
        });

        var response = deliveryService.create("aman@example.com", request);

        assertEquals(DeliveryStatus.BOOKED, response.status());
        assertEquals(0, BigDecimal.valueOf(390.00).compareTo(response.courierCharge()));
    }

    @Test
    void updateStatusShouldFailWhenMissing() {
        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deliveryService.updateStatus(99L, DeliveryStatus.DELIVERED));
    }
}
