package com.vcall.recording.repository;

import com.vcall.recording.entity.RetentionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetentionPolicyRepository extends JpaRepository<RetentionPolicy, Long> {

    List<RetentionPolicy> findByIsActiveTrue();
}
