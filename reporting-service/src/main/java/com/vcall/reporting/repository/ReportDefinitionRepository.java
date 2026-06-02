package com.vcall.reporting.repository;

import com.vcall.reporting.entity.ReportDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {

    List<ReportDefinition> findByReportType(ReportDefinition.ReportType reportType);
    Page<ReportDefinition> findByReportType(ReportDefinition.ReportType reportType, Pageable pageable);

    List<ReportDefinition> findByIsActiveTrue();
    Page<ReportDefinition> findByIsActiveTrue(Pageable pageable);

    List<ReportDefinition> findByScheduleIsNotNull();
    Page<ReportDefinition> findByScheduleIsNotNull(Pageable pageable);
}
