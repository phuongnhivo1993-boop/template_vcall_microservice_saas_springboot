package com.vcall.call.repository;

import com.vcall.call.entity.CallQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CallQueueRepository extends JpaRepository<CallQueue, Long>, JpaSpecificationExecutor<CallQueue> {

    Optional<CallQueue> findByName(String name);
}
