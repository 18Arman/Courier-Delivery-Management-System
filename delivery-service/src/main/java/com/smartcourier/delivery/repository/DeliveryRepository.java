package com.smartcourier.delivery.repository;

import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.entity.DeliveryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    long countByStatus(DeliveryStatus status);

    long countByStatusIn(List<DeliveryStatus> statuses);
}
