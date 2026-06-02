package com.vcall.reporting.repository;

import com.vcall.reporting.entity.ReportExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    List<ReportExecution> findByReportDefinitionIdOrderByExecutedAtDesc(Long reportDefinitionId);
    Page<ReportExecution> findByReportDefinitionIdOrderByExecutedAtDesc(Long reportDefinitionId, Pageable pageable);

    List<ReportExecution> findByStatus(ReportExecution.ExecutionStatus status);
    Page<ReportExecution> findByStatus(ReportExecution.ExecutionStatus status, Pageable pageable);

    List<ReportExecution> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end);
    Page<ReportExecution> findByExecutedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
