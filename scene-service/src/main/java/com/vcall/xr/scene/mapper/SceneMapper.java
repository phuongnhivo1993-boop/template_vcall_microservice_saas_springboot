package com.vcall.xr.scene.mapper;

import com.vcall.xr.scene.domain.Scene;
import com.vcall.xr.scene.domain.SceneType;
import com.vcall.xr.scene.domain.PublishStatus;
import com.vcall.xr.scene.dto.SceneRequest;
import com.vcall.xr.scene.dto.SceneResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SceneMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "type", expression = "java(com.vcall.xr.scene.domain.SceneType.valueOf(request.getType().toUpperCase()))")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "version", constant = "1")
    @Mapping(target = "viewCount", constant = "0")
    @Mapping(target = "avgViewTimeSeconds", constant = "0.0")
    @Mapping(target = "publishedUrl", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    Scene toEntity(SceneRequest request);

    @Mapping(target = "type", expression = "java(scene.getType() != null ? scene.getType().name() : null)")
    @Mapping(target = "status", expression = "java(scene.getStatus() != null ? scene.getStatus().name() : null)")
    @Mapping(target = "nodeCount", ignore = true)
    @Mapping(target = "hotspotCount", ignore = true)
    SceneResponse toResponse(Scene scene);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "publishedUrl", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "avgViewTimeSeconds", ignore = true)
    @Mapping(target = "type", expression = "java(request.getType() != null ? com.vcall.xr.scene.domain.SceneType.valueOf(request.getType().toUpperCase()) : null)")
    void updateEntity(SceneRequest request, @MappingTarget Scene scene);
}
