package com.vcall.xr.scene.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneNodeRequest {

    private UUID parentId;

    @NotBlank(message = "Node type is required")
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
}
