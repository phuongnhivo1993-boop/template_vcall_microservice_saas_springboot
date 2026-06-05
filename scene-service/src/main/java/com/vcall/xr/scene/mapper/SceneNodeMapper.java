package com.vcall.xr.scene.mapper;

import com.vcall.xr.scene.domain.SceneNode;
import com.vcall.xr.scene.dto.SceneNodeRequest;
import com.vcall.xr.scene.dto.SceneNodeResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SceneNodeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sceneId", ignore = true)
    @Mapping(target = "nodeType", expression = "java(com.vcall.xr.scene.domain.SceneNode.NodeType.valueOf(request.getNodeType().toUpperCase()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "visible", defaultValue = "true")
    @Mapping(target = "interactive", defaultValue = "false")
    @Mapping(target = "sortOrder", defaultValue = "0")
    @Mapping(target = "positionX", defaultValue = "0.0f")
    @Mapping(target = "positionY", defaultValue = "0.0f")
    @Mapping(target = "positionZ", defaultValue = "0.0f")
    @Mapping(target = "rotationX", defaultValue = "0.0f")
    @Mapping(target = "rotationY", defaultValue = "0.0f")
    @Mapping(target = "rotationZ", defaultValue = "0.0f")
    @Mapping(target = "scaleX", defaultValue = "1.0f")
    @Mapping(target = "scaleY", defaultValue = "1.0f")
    @Mapping(target = "scaleZ", defaultValue = "1.0f")
    SceneNode toEntity(SceneNodeRequest request);

    @Mapping(target = "nodeType", expression = "java(node.getNodeType() != null ? node.getNodeType().name() : null)")
    SceneNodeResponse toResponse(SceneNode node);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sceneId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "nodeType", expression = "java(request.getNodeType() != null ? com.vcall.xr.scene.domain.SceneNode.NodeType.valueOf(request.getNodeType().toUpperCase()) : null)")
    void updateEntity(SceneNodeRequest request, @MappingTarget SceneNode node);
}
