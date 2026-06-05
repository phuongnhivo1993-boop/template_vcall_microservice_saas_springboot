package com.vcall.xr.tenant.mapper;

import com.vcall.xr.tenant.domain.Tenant;
import com.vcall.xr.tenant.dto.TenantRequest;
import com.vcall.xr.tenant.dto.TenantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    Tenant toEntity(TenantRequest request);

    TenantResponse toResponse(Tenant entity);

    void updateEntity(TenantRequest request, @MappingTarget Tenant entity);
}
