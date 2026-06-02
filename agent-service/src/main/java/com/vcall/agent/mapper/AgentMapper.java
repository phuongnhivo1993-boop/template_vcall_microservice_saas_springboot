package com.vcall.agent.mapper;

import com.vcall.agent.dto.AgentRequest;
import com.vcall.agent.dto.AgentResponse;
import com.vcall.agent.entity.Agent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AgentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "status", constant = "OFFLINE")
    Agent toEntity(AgentRequest request);

    @Mapping(target = "currentSession", ignore = true)
    @Mapping(target = "status", expression = "java(agent.getStatus().name())")
    AgentResponse toResponse(Agent agent);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "agentCode", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateEntity(AgentRequest request, @MappingTarget Agent agent);
}
