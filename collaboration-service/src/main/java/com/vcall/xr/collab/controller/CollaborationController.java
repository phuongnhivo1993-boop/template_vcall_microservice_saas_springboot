package com.vcall.xr.collab.controller;

import com.vcall.xr.collab.domain.CollaborationParticipant;
import com.vcall.xr.collab.domain.CollaborationRoom;
import com.vcall.xr.collab.service.CollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/collaboration")
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "XR Collaboration room management")
public class CollaborationController {

    private final CollaborationService collaborationService;

    @PostMapping("/rooms")
    @Operation(summary = "Create a collaboration room")
    public ResponseEntity<CollaborationRoom> createRoom(@RequestBody CollaborationRoom room) {
        CollaborationRoom created = collaborationService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get collaboration room by ID")
    public ResponseEntity<CollaborationRoom> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(collaborationService.getRoom(roomId));
    }

    @GetMapping("/rooms/tenant/{tenantId}")
    @Operation(summary = "Get all rooms for a tenant")
    public ResponseEntity<List<CollaborationRoom>> getRoomsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(collaborationService.getRoomsByTenant(tenantId));
    }

    @GetMapping("/rooms/tenant/{tenantId}/active")
    @Operation(summary = "Get active rooms for a tenant")
    public ResponseEntity<List<CollaborationRoom>> getActiveRoomsByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(collaborationService.getActiveRoomsByTenant(tenantId));
    }

    @GetMapping("/rooms/host/{hostUserId}")
    @Operation(summary = "Get rooms hosted by a user")
    public ResponseEntity<List<CollaborationRoom>> getRoomsByHost(@PathVariable UUID hostUserId) {
        return ResponseEntity.ok(collaborationService.getRoomsByHost(hostUserId));
    }

    @PutMapping("/rooms/{roomId}")
    @Operation(summary = "Update collaboration room")
    public ResponseEntity<CollaborationRoom> updateRoom(@PathVariable UUID roomId,
                                                         @RequestBody CollaborationRoom room) {
        return ResponseEntity.ok(collaborationService.updateRoom(roomId, room));
    }

    @PostMapping("/rooms/{roomId}/close")
    @Operation(summary = "Close collaboration room")
    public ResponseEntity<Void> closeRoom(@PathVariable UUID roomId) {
        collaborationService.closeRoom(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/join")
    @Operation(summary = "Join a collaboration room")
    public ResponseEntity<CollaborationParticipant> joinRoom(
            @PathVariable UUID roomId,
            @RequestParam UUID userId,
            @RequestParam(required = false) String avatarConfig) {
        return ResponseEntity.ok(collaborationService.joinRoom(roomId, userId, avatarConfig));
    }

    @PostMapping("/rooms/{roomId}/leave")
    @Operation(summary = "Leave a collaboration room")
    public ResponseEntity<Void> leaveRoom(@PathVariable UUID roomId, @RequestParam UUID userId) {
        collaborationService.leaveRoom(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/mute")
    @Operation(summary = "Toggle mute for user in room")
    public ResponseEntity<Void> toggleMute(@PathVariable UUID roomId, @RequestParam UUID userId) {
        collaborationService.toggleMute(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/rooms/{roomId}/screen-share")
    @Operation(summary = "Toggle screen share for user in room")
    public ResponseEntity<Void> toggleScreenShare(@PathVariable UUID roomId, @RequestParam UUID userId) {
        collaborationService.toggleScreenShare(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rooms/{roomId}/participants/count")
    @Operation(summary = "Get participant count for room")
    public ResponseEntity<Map<String, Long>> getParticipantCount(@PathVariable UUID roomId) {
        long count = collaborationService.getParticipantCount(roomId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
