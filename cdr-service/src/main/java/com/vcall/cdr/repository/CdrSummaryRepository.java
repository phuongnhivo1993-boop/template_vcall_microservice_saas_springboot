package com.vcall.cdr.repository;

import com.vcall.cdr.entity.CdrSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CdrSummaryRepository extends JpaRepository<CdrSummary, Long> {

    Optional<CdrSummary> findByDateAndTenantId(LocalDate date, UUID tenantId);

    List<CdrSummary> findByDateBetween(LocalDate start, LocalDate end);
    Page<CdrSummary> findByDateBetween(LocalDate start, LocalDate end, Pageable pageable);

    Page<CdrSummary> findByTenantIdAndDateBetween(UUID tenantId, LocalDate start, LocalDate end, Pageable pageable);
}
