package com.smartcourier.admin.repository;

import com.smartcourier.admin.entity.ReportRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRecordRepository extends JpaRepository<ReportRecord, Long> {

    List<ReportRecord> findAllByOrderByGeneratedAtDesc();
}

