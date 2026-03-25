package com.smartcourier.admin.repository;

import com.smartcourier.admin.entity.DeliveryExceptionCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryExceptionCaseRepository extends JpaRepository<DeliveryExceptionCase, Long> {

    List<DeliveryExceptionCase> findByResolvedFalseOrderByCreatedAtDesc();

    boolean existsByTrackingNumberAndExceptionStatusAndResolvedFalse(String trackingNumber, String exceptionStatus);
}
