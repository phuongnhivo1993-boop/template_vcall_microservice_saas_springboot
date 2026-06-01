package com.vcall.reporting.repository;

import com.vcall.reporting.entity.ReportDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {

    List<ReportDefinition> findByReportType(ReportDefinition.ReportType reportType);

    List<ReportDefinition> findByIsActiveTrue();

    List<ReportDefinition> findByScheduleIsNotNull();
}
