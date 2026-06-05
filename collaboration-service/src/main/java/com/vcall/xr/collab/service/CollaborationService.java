package com.vcall.xr.collab.service;

import com.vcall.xr.collab.domain.CollaborationParticipant;
import com.vcall.xr.collab.domain.CollaborationRoom;
import com.vcall.xr.collab.domain.CollaborationRoom.RoomStatus;
import com.vcall.xr.collab.repository.CollaborationRoomRepository;
import com.vcall.xr.collab.websocket.CollaborationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationService {

    private final CollaborationRoomRepository roomRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CollaborationWebSocketHandler webSocketHandler;

    private static final String ROOM_PREFIX = "collab:room:";
    private static final String ROOM_PARTICIPANTS_PREFIX = "collab:room:participants:";

    @Transactional
    public CollaborationRoom createRoom(CollaborationRoom room) {
        room.setStatus(RoomStatus.ACTIVE);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        room.setIsDeleted(false);
        if (room.getMaxParticipants() == null) {
            room.setMaxParticipants(10);
        }
        CollaborationRoom saved = roomRepository.save(room);
        log.info("Created collaboration room {} for tenant {}", saved.getId(), saved.getTenantId());
        return saved;
    }

    @Transactional(readOnly = true)
    public CollaborationRoom getRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
    }

    @Transactional(readOnly = true)
    public List<CollaborationRoom> getRoomsByTenant(UUID tenantId) {
        return roomRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<CollaborationRoom> getRoomsByHost(UUID hostUserId) {
        return roomRepository.findByHostUserId(hostUserId);
    }

    @Transactional
    public CollaborationRoom updateRoom(UUID roomId, CollaborationRoom update) {
        CollaborationRoom room = getRoom(roomId);
        room.setName(update.getName());
        room.setMaxParticipants(update.getMaxParticipants());
        room.setUpdatedAt(LocalDateTime.now());
        return roomRepository.save(room);
    }

    @Transactional
    public void closeRoom(UUID roomId) {
        CollaborationRoom room = getRoom(roomId);
        room.setStatus(RoomStatus.CLOSED);
        room.setUpdatedAt(LocalDateTime.now());
        roomRepository.save(room);
        redisTemplate.delete(ROOM_PREFIX + roomId);
        redisTemplate.delete(ROOM_PARTICIPANTS_PREFIX + roomId);
        log.info("Closed collaboration room {}", roomId);
    }

    @Transactional
    public CollaborationParticipant joinRoom(UUID roomId, UUID userId, String avatarConfig) {
        CollaborationRoom room = getRoom(roomId);
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new RuntimeException("Room is not active");
        }

        long participantCount = getParticipantCount(roomId);
        if (participantCount >= room.getMaxParticipants()) {
            throw new RuntimeException("Room is full");
        }

        CollaborationParticipant participant = CollaborationParticipant.builder()
                .roomId(roomId)
                .userId(userId)
                .avatarConfig(avatarConfig)
                .joinedAt(LocalDateTime.now())
                .isMuted(true)
                .isScreenSharing(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        redisTemplate.opsForSet().add(ROOM_PARTICIPANTS_PREFIX + roomId, participant.toString());
        redisTemplate.expire(ROOM_PARTICIPANTS_PREFIX + roomId, 24, TimeUnit.HOURS);

        log.info("User {} joined room {}", userId, roomId);
        webSocketHandler.broadcastToRoom(roomId, "USER_JOINED", userId.toString());
        return participant;
    }

    @Transactional
    public void leaveRoom(UUID roomId, UUID userId) {
        redisTemplate.opsForSet().remove(ROOM_PARTICIPANTS_PREFIX + roomId, userId.toString());
        log.info("User {} left room {}", userId, roomId);
        webSocketHandler.broadcastToRoom(roomId, "USER_LEFT", userId.toString());
    }

    @Transactional
    public void toggleMute(UUID roomId, UUID userId) {
        webSocketHandler.broadcastToRoom(roomId, "TOGGLE_MUTE", userId.toString());
        log.info("User {} toggled mute in room {}", userId, roomId);
    }

    @Transactional
    public void toggleScreenShare(UUID roomId, UUID userId) {
        webSocketHandler.broadcastToRoom(roomId, "SCREEN_SHARE", userId.toString());
        log.info("User {} toggled screen share in room {}", userId, roomId);
    }

    public long getParticipantCount(UUID roomId) {
        Long count = redisTemplate.opsForSet().size(ROOM_PARTICIPANTS_PREFIX + roomId);
        return count != null ? count : 0;
    }

    @Transactional(readOnly = true)
    public List<CollaborationRoom> getActiveRoomsByTenant(UUID tenantId) {
        return roomRepository.findByTenantIdAndStatus(tenantId, RoomStatus.ACTIVE);
    }
}
