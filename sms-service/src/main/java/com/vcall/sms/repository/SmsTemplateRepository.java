package com.vcall.sms.repository;

import com.vcall.sms.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {

    Optional<SmsTemplate> findByName(String name);

    List<SmsTemplate> findByIsActiveTrue();
}
