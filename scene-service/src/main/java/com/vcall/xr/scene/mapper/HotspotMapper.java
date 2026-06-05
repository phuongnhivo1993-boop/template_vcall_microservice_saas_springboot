package com.vcall.xr.scene.mapper;

import com.vcall.xr.scene.domain.Hotspot;
import com.vcall.xr.scene.dto.HotspotRequest;
import com.vcall.xr.scene.dto.HotspotResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HotspotMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sceneId", ignore = true)
    @Mapping(target = "hotspotType", expression = "java(com.vcall.xr.scene.domain.Hotspot.HotspotType.valueOf(request.getHotspotType().toUpperCase()))")
    @Mapping(target = "actionType", expression = "java(request.getActionType() != null ? com.vcall.xr.scene.domain.Hotspot.ActionType.valueOf(request.getActionType().toUpperCase()) : null)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Hotspot toEntity(HotspotRequest request);

    @Mapping(target = "hotspotType", expression = "java(hotspot.getHotspotType() != null ? hotspot.getHotspotType().name() : null)")
    @Mapping(target = "actionType", expression = "java(hotspot.getActionType() != null ? hotspot.getActionType().name() : null)")
    HotspotResponse toResponse(Hotspot hotspot);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sceneId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "hotspotType", expression = "java(request.getHotspotType() != null ? com.vcall.xr.scene.domain.Hotspot.HotspotType.valueOf(request.getHotspotType().toUpperCase()) : null)")
    @Mapping(target = "actionType", expression = "java(request.getActionType() != null ? com.vcall.xr.scene.domain.Hotspot.ActionType.valueOf(request.getActionType().toUpperCase()) : null)")
    void updateEntity(HotspotRequest request, @MappingTarget Hotspot hotspot);
}
