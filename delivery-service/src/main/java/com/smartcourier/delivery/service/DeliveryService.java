package com.smartcourier.delivery.service;

import com.smartcourier.delivery.dto.AddressRequest;
import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.dto.DeliveryStatsResponse;
import com.smartcourier.delivery.dto.DeliverySummaryResponse;
import com.smartcourier.delivery.dto.PackageRequest;
import com.smartcourier.delivery.entity.Address;
import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.entity.DeliveryStatus;
import com.smartcourier.delivery.entity.PackageDetails;
import com.smartcourier.delivery.exception.AccessDeniedException;
import com.smartcourier.delivery.exception.ResourceNotFoundException;
import com.smartcourier.delivery.messaging.DeliveryEventPublisher;
import com.smartcourier.delivery.repository.DeliveryRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventPublisher deliveryEventPublisher;

    public DeliveryService(DeliveryRepository deliveryRepository, DeliveryEventPublisher deliveryEventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.deliveryEventPublisher = deliveryEventPublisher;
    }

    @Transactional
    public DeliveryResponse create(String customerEmail, CreateDeliveryRequest request) {
        Delivery delivery = new Delivery();
        delivery.setCustomerEmail(customerEmail);
        delivery.setTrackingNumber(generateTrackingNumber());
        delivery.setServiceType(request.serviceType());
        delivery.setStatus(DeliveryStatus.BOOKED);
        delivery.setSenderAddress(toAddress(request.sender()));
        delivery.setReceiverAddress(toAddress(request.receiver()));
        delivery.setPackageDetails(toPackageDetails(request.parcel()));
        delivery.setPickupDate(request.pickupDate());
        delivery.setCourierCharge(calculateCharge(request));
        Delivery saved = deliveryRepository.save(delivery);
        deliveryEventPublisher.publish(saved, "DELIVERY_CREATED");
        return toResponse(saved);
    }

    public List<DeliveryResponse> getMyDeliveries(String email) {
        return deliveryRepository.findByCustomerEmailOrderByCreatedAtDesc(email).stream().map(this::toResponse).toList();
    }

    public DeliveryResponse getById(Long id, String email, boolean admin) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        if (!admin && !delivery.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You are not allowed to access this delivery");
        }
        return toResponse(delivery);
    }

    @Transactional
    public DeliveryResponse updateStatus(Long id, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        delivery.setStatus(status);
        Delivery saved = deliveryRepository.save(delivery);
        deliveryEventPublisher.publish(saved, "DELIVERY_STATUS_UPDATED");
        return toResponse(saved);
    }

    public DeliverySummaryResponse getSummaryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
        return new DeliverySummaryResponse(
                delivery.getId(),
                delivery.getTrackingNumber(),
                delivery.getCustomerEmail(),
                delivery.getServiceType(),
                delivery.getStatus(),
                delivery.getCourierCharge(),
                delivery.getPickupDate()
        );
    }

    public DeliveryStatsResponse getStats() {
        return new DeliveryStatsResponse(
                deliveryRepository.countByStatus(DeliveryStatus.BOOKED),
                deliveryRepository.countByStatusIn(List.of(DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT, DeliveryStatus.OUT_FOR_DELIVERY)),
                deliveryRepository.countByStatus(DeliveryStatus.DELIVERED),
                deliveryRepository.countByStatusIn(List.of(DeliveryStatus.DELAYED, DeliveryStatus.FAILED, DeliveryStatus.RETURNED))
        );
    }

    public boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private BigDecimal calculateCharge(CreateDeliveryRequest request) {
        BigDecimal base = switch (request.serviceType()) {
            case DOMESTIC -> BigDecimal.valueOf(150);
            case EXPRESS -> BigDecimal.valueOf(300);
            case INTERNATIONAL -> BigDecimal.valueOf(900);
        };
        return base
                .add(request.parcel().weightInKg().multiply(BigDecimal.valueOf(40)))
                .add(request.parcel().declaredValue().multiply(BigDecimal.valueOf(0.01)));
    }

    private String generateTrackingNumber() {
        return "SC" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    }

    private Address toAddress(AddressRequest request) {
        Address address = new Address();
        address.setContactName(request.contactName());
        address.setPhoneNumber(request.phoneNumber());
        address.setLine1(request.line1());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        return address;
    }

    private PackageDetails toPackageDetails(PackageRequest request) {
        PackageDetails details = new PackageDetails();
        details.setParcelType(request.parcelType());
        details.setWeightInKg(request.weightInKg());
        details.setDeclaredValue(request.declaredValue());
        details.setDimensions(request.dimensions());
        details.setNotes(request.notes());
        return details;
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getTrackingNumber(),
                delivery.getCustomerEmail(),
                delivery.getServiceType(),
                delivery.getStatus(),
                delivery.getCourierCharge(),
                delivery.getPickupDate(),
                new AddressRequest(
                        delivery.getSenderAddress().getContactName(),
                        delivery.getSenderAddress().getPhoneNumber(),
                        delivery.getSenderAddress().getLine1(),
                        delivery.getSenderAddress().getCity(),
                        delivery.getSenderAddress().getState(),
                        delivery.getSenderAddress().getPostalCode(),
                        delivery.getSenderAddress().getCountry()
                ),
                new AddressRequest(
                        delivery.getReceiverAddress().getContactName(),
                        delivery.getReceiverAddress().getPhoneNumber(),
                        delivery.getReceiverAddress().getLine1(),
                        delivery.getReceiverAddress().getCity(),
                        delivery.getReceiverAddress().getState(),
                        delivery.getReceiverAddress().getPostalCode(),
                        delivery.getReceiverAddress().getCountry()
                ),
                new PackageRequest(
                        delivery.getPackageDetails().getParcelType(),
                        delivery.getPackageDetails().getWeightInKg(),
                        delivery.getPackageDetails().getDeclaredValue(),
                        delivery.getPackageDetails().getDimensions(),
                        delivery.getPackageDetails().getNotes()
                ),
                delivery.getCreatedAt()
        );
    }
}
