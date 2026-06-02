package com.vcall.customer.mapper;

import com.vcall.customer.dto.CustomerResponse;
import com.vcall.customer.entity.Customer;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "tagMappings", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    Customer toEntity(com.vcall.customer.dto.CustomerRequest request);

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    CustomerResponse toResponse(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "tagMappings", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "customerCode", ignore = true)
    void updateEntity(com.vcall.customer.dto.CustomerRequest request, @MappingTarget Customer customer);
}
