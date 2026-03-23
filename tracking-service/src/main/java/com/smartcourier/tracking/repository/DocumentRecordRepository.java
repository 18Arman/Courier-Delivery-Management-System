package com.smartcourier.tracking.repository;

import com.smartcourier.tracking.entity.DocumentRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {

    List<DocumentRecord> findByTrackingNumberOrderByUploadedAtDesc(String trackingNumber);
}

