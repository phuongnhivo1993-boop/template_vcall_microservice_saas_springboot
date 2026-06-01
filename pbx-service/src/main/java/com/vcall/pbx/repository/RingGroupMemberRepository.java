package com.vcall.pbx.repository;

import com.vcall.pbx.entity.RingGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RingGroupMemberRepository extends JpaRepository<RingGroupMember, Long> {

    List<RingGroupMember> findByRingGroupIdOrderByPosition(Long ringGroupId);

    List<RingGroupMember> findByExtensionId(Long extensionId);
}
