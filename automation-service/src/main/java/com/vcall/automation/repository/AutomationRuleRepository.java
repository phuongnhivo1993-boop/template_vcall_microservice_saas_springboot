package com.vcall.automation.repository;

import com.vcall.automation.entity.AutomationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long>, JpaSpecificationExecutor<AutomationRule> {

    List<AutomationRule> findByIsActiveTrue();

    Page<AutomationRule> findByIsActive(Boolean isActive, Pageable pageable);
}
