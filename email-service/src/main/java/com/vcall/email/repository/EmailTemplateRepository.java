package com.vcall.email.repository;

import com.vcall.email.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByName(String name);

    List<EmailTemplate> findByCategory(String category);
    Page<EmailTemplate> findByCategory(String category, Pageable pageable);

    List<EmailTemplate> findByIsActiveTrue();
}
