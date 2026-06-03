package com.vcall.scheduling.repository;

import com.vcall.scheduling.entity.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, UUID>, JpaSpecificationExecutor<ScheduleTemplate> {

    List<ScheduleTemplate> findByAgentId(UUID agentId);

    List<ScheduleTemplate> findByAgentIdAndDayOfWeek(UUID agentId, DayOfWeek dayOfWeek);

    List<ScheduleTemplate> findByAgentIdAndIsActiveTrue(UUID agentId);

    List<ScheduleTemplate> findByDayOfWeek(DayOfWeek dayOfWeek);
}
