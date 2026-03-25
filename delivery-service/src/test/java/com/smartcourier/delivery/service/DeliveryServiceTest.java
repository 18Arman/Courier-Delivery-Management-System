package com.smartcourier.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartcourier.delivery.dto.AddressRequest;
import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.PackageRequest;
import com.smartcourier.delivery.entity.Address;
import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.entity.DeliveryStatus;
import com.smartcourier.delivery.entity.PackageDetails;
import com.smartcourier.delivery.entity.ServiceType;
import com.smartcourier.delivery.exception.AccessDeniedException;
import com.smartcourier.delivery.exception.ResourceNotFoundException;
import com.smartcourier.delivery.messaging.DeliveryEventPublisher;
import com.smartcourier.delivery.repository.DeliveryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private DeliveryEventPublisher deliveryEventPublisher;

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(deliveryRepository, deliveryEventPublisher);
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
        verify(deliveryEventPublisher).publish(any(Delivery.class), org.mockito.ArgumentMatchers.eq("DELIVERY_CREATED"));
    }

    @Test
    void updateStatusShouldFailWhenMissing() {
        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deliveryService.updateStatus(99L, DeliveryStatus.DELIVERED));
    }

    @Test
    void getByIdShouldRejectDifferentCustomer() {
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setCustomerEmail("owner@example.com");
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        assertThrows(AccessDeniedException.class, () -> deliveryService.getById(1L, "other@example.com", false));
    }

    @Test
    void updateStatusShouldPublishEvent() {
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setStatus(DeliveryStatus.BOOKED);
        delivery.setTrackingNumber("SC123");
        delivery.setCustomerEmail("aman@example.com");
        delivery.setServiceType(ServiceType.EXPRESS);
        delivery.setSenderAddress(address("Sender"));
        delivery.setReceiverAddress(address("Receiver"));
        delivery.setPackageDetails(packageDetails());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        var response = deliveryService.updateStatus(1L, DeliveryStatus.IN_TRANSIT);

        assertEquals(DeliveryStatus.IN_TRANSIT, response.status());
        verify(deliveryEventPublisher).publish(delivery, "DELIVERY_STATUS_UPDATED");
    }

    @Test
    void getSummaryByIdShouldReturnProjectedData() {
        Delivery delivery = new Delivery();
        delivery.setId(1L);
        delivery.setTrackingNumber("SC123");
        delivery.setCustomerEmail("aman@example.com");
        delivery.setServiceType(ServiceType.EXPRESS);
        delivery.setStatus(DeliveryStatus.BOOKED);
        delivery.setCourierCharge(BigDecimal.TEN);
        delivery.setPickupDate(LocalDate.now());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        var summary = deliveryService.getSummaryById(1L);

        assertEquals("SC123", summary.trackingNumber());
        assertEquals(DeliveryStatus.BOOKED, summary.status());
    }

    @Test
    void getStatsShouldReturnRepositoryCounts() {
        when(deliveryRepository.countByStatus(DeliveryStatus.BOOKED)).thenReturn(2L);
        when(deliveryRepository.countByStatusIn(List.of(DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT, DeliveryStatus.OUT_FOR_DELIVERY))).thenReturn(3L);
        when(deliveryRepository.countByStatus(DeliveryStatus.DELIVERED)).thenReturn(4L);
        when(deliveryRepository.countByStatusIn(List.of(DeliveryStatus.DELAYED, DeliveryStatus.FAILED, DeliveryStatus.RETURNED))).thenReturn(1L);

        var stats = deliveryService.getStats();

        assertEquals(2L, stats.bookedCount());
        assertEquals(3L, stats.inTransitCount());
        assertEquals(4L, stats.deliveredCount());
        assertEquals(1L, stats.exceptionCount());
    }

    @Test
    void isAdminShouldDetectRoleAdmin() {
        var user = User.withUsername("admin").password("x").roles("ADMIN").build();

        assertEquals(true, deliveryService.isAdmin(user));
    }

    private Address address(String contactName) {
        Address address = new Address();
        address.setContactName(contactName);
        address.setPhoneNumber("9999999999");
        address.setLine1("Line 1");
        address.setCity("Delhi");
        address.setState("DL");
        address.setPostalCode("110001");
        address.setCountry("India");
        return address;
    }

    private PackageDetails packageDetails() {
        PackageDetails packageDetails = new PackageDetails();
        packageDetails.setParcelType("Box");
        packageDetails.setWeightInKg(BigDecimal.ONE);
        packageDetails.setDeclaredValue(BigDecimal.TEN);
        packageDetails.setDimensions("10x10x10");
        packageDetails.setNotes("Test");
        return packageDetails;
    }
}
