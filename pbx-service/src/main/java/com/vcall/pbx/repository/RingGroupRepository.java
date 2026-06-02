package com.vcall.pbx.repository;

import com.vcall.pbx.entity.RingGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RingGroupRepository extends JpaRepository<RingGroup, Long>, JpaSpecificationExecutor<RingGroup> {

    Optional<RingGroup> findByName(String name);
}
