package com.vcall.reporting.repository;

import com.vcall.reporting.entity.ReportExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    List<ReportExecution> findByReportDefinitionIdOrderByExecutedAtDesc(Long reportDefinitionId);

    List<ReportExecution> findByStatus(ReportExecution.ExecutionStatus status);

    List<ReportExecution> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);
}
