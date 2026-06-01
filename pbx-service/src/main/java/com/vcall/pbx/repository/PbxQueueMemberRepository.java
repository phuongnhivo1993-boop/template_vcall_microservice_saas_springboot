package com.vcall.pbx.repository;

import com.vcall.pbx.entity.PbxQueueMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PbxQueueMemberRepository extends JpaRepository<PbxQueueMember, Long> {

    List<PbxQueueMember> findByQueueId(Long queueId);

    List<PbxQueueMember> findByExtensionId(Long extensionId);
}
