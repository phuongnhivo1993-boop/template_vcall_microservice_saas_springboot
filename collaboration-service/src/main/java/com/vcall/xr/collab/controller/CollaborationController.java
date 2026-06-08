package com.vcall.xr.collab.controller;

import com.vcall.xr.collab.dto.CollaborationParticipantResponse;
import com.vcall.xr.collab.dto.CollaborationRoomMapper;
import com.vcall.xr.collab.dto.CollaborationRoomRequest;
import com.vcall.xr.collab.dto.CollaborationRoomResponse;
import com.vcall.xr.collab.service.CollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/collaboration")
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "XR Collaboration room management")
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Create a collaboration room")
    public ResponseEntity<CollaborationRoomResponse> createRoom(@Valid @RequestBody CollaborationRoomRequest request) {
        CollaborationRoomResponse created = CollaborationRoomMapper.toResponse(
                collaborationService.createRoom(CollaborationRoomMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/rooms/{roomId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get collaboration room by ID")
    public ResponseEntity<CollaborationRoomResponse> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(CollaborationRoomMapper.toResponse(collaborationService.getRoom(roomId)));
    }

    @GetMapping("/rooms/tenant/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get all rooms for a tenant")
    public ResponseEntity<List<CollaborationRoomResponse>> getRoomsByTenant(@PathVariable UUID tenantId) {
        List<CollaborationRoomResponse> rooms = collaborationService.getRoomsByTenant(tenantId).stream()
                .map(CollaborationRoomMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/tenant/{tenantId}/active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get active rooms for a tenant")
    public ResponseEntity<List<CollaborationRoomResponse>> getActiveRoomsByTenant(@PathVariable UUID tenantId) {
        List<CollaborationRoomResponse> rooms = collaborationService.getActiveRoomsByTenant(tenantId).stream()
                .map(CollaborationRoomMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/host/{hostUserId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get rooms hosted by a user")
    public ResponseEntity<List<CollaborationRoomResponse>> getRoomsByHost(@PathVariable UUID hostUserId) {
        List<CollaborationRoomResponse> rooms = collaborationService.getRoomsByHost(hostUserId).stream()
                .map(CollaborationRoomMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rooms);
    }

    @PutMapping("/rooms/{roomId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Update collaboration room")
    public ResponseEntity<CollaborationRoomResponse> updateRoom(@PathVariable UUID roomId,
                                                                 @Valid @RequestBody CollaborationRoomRequest request) {
        CollaborationRoomResponse updated = CollaborationRoomMapper.toResponse(
                collaborationService.updateRoom(roomId, CollaborationRoomMapper.toEntity(request)));
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/rooms/{roomId}/close")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Close collaboration room")
    public ResponseEntity<Void> closeRoom(@PathVariable UUID roomId) {
        collaborationService.closeRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/join")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Join a collaboration room")
    public ResponseEntity<CollaborationParticipantResponse> joinRoom(
            @PathVariable UUID roomId,
            @RequestParam UUID userId,
            @RequestParam(required = false) String avatarConfig) {
        return ResponseEntity.ok(toParticipantResponse(
                collaborationService.joinRoom(roomId, userId, avatarConfig)));
    }

    @PostMapping("/rooms/{roomId}/leave")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Leave a collaboration room")
    public ResponseEntity<Void> leaveRoom(@PathVariable UUID roomId, @RequestParam UUID userId) {
        collaborationService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/mute")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Toggle mute for user in room")
    public ResponseEntity<Void> toggleMute(@PathVariable UUID roomId, @RequestParam UUID userId) {
        collaborationService.toggleMute(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/screen-share")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Toggle screen share for user in room")
    public ResponseEntity<Void> toggleScreenShare(@PathVariable UUID roomId, @RequestParam UUID userId) {
        collaborationService.toggleScreenShare(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}/participants/count")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SUPERVISOR')")
    @Operation(summary = "Get participant count for room")
    public ResponseEntity<Map<String, Long>> getParticipantCount(@PathVariable UUID roomId) {
        long count = collaborationService.getParticipantCount(roomId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    private CollaborationParticipantResponse toParticipantResponse(
            com.vcall.xr.collab.domain.CollaborationParticipant participant) {
        if (participant == null) return null;
        return CollaborationParticipantResponse.builder()
                .id(participant.getId())
                .roomId(participant.getRoomId())
                .userId(participant.getUserId())
                .avatarConfig(participant.getAvatarConfig())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .isMuted(participant.getIsMuted())
                .isScreenSharing(participant.getIsScreenSharing())
                .build();
    }
}
