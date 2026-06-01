package com.vcall.cdr.repository;

import com.vcall.cdr.entity.CdrImportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CdrImportLogRepository extends JpaRepository<CdrImportLog, Long> {

    List<CdrImportLog> findByStatus(CdrImportLog.ImportStatus status);
}
