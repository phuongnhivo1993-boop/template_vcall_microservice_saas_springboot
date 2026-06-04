package com.vcall.automation.repository;

import com.vcall.automation.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
}
