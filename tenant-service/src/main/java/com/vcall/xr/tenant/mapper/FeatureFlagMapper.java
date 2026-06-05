package com.vcall.xr.tenant.mapper;

import com.vcall.xr.tenant.domain.FeatureFlag;
import com.vcall.xr.tenant.dto.FeatureFlagRequest;
import com.vcall.xr.tenant.dto.FeatureFlagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FeatureFlagMapper {

    FeatureFlag toEntity(FeatureFlagRequest request);

    FeatureFlagResponse toResponse(FeatureFlag entity);

    void updateEntity(FeatureFlagRequest request, @MappingTarget FeatureFlag entity);
}
