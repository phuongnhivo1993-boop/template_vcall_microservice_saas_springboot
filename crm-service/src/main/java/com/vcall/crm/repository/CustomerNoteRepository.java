package com.vcall.crm.repository;

import com.vcall.crm.entity.CustomerNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    List<CustomerNote> findByCustomerId(UUID customerId);

    Page<CustomerNote> findByCustomerId(UUID customerId, Pageable pageable);

    List<CustomerNote> findByLeadId(UUID leadId);

    Page<CustomerNote> findByLeadId(UUID leadId, Pageable pageable);
}
