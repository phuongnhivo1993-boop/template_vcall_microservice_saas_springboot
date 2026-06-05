package com.vcall.xr.collab.repository;

import com.vcall.xr.collab.domain.CollaborationRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollaborationRoomRepository extends JpaRepository<CollaborationRoom, UUID> {

    List<CollaborationRoom> findByTenantId(UUID tenantId);

    List<CollaborationRoom> findByHostUserId(UUID hostUserId);

    List<CollaborationRoom> findByStatus(CollaborationRoom.RoomStatus status);

    List<CollaborationRoom> findByTenantIdAndStatus(UUID tenantId, CollaborationRoom.RoomStatus status);
}
