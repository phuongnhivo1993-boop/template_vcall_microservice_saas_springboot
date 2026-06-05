package com.vcall.xr.scene.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneNodeResponse {

    private UUID id;
    private UUID sceneId;
    private UUID parentId;
    private String nodeType;
    private String name;
    private Float positionX;
    private Float positionY;
    private Float positionZ;
    private Float rotationX;
    private Float rotationY;
    private Float rotationZ;
    private Float scaleX;
    private Float scaleY;
    private Float scaleZ;
    private String content;
    private Boolean visible;
    private Boolean interactive;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
