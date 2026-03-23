package com.smartcourier.tracking.repository;

import com.smartcourier.tracking.entity.DeliveryProof;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryProofRepository extends JpaRepository<DeliveryProof, Long> {

    Optional<DeliveryProof> findByTrackingNumber(String trackingNumber);
}

