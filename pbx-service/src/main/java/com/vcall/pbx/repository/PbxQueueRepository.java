package com.vcall.pbx.repository;

import com.vcall.pbx.entity.PbxQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PbxQueueRepository extends JpaRepository<PbxQueue, Long> {

    Optional<PbxQueue> findByName(String name);
}
