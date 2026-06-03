package com.vcall.scheduling.mapper;

import com.vcall.scheduling.dto.request.AppointmentRequest;
import com.vcall.scheduling.dto.request.ScheduleTemplateRequest;
import com.vcall.scheduling.dto.response.AppointmentResponse;
import com.vcall.scheduling.dto.response.AvailabilityResponse;
import com.vcall.scheduling.dto.response.ScheduleTemplateResponse;
import com.vcall.scheduling.entity.AgentAvailability;
import com.vcall.scheduling.entity.Appointment;
import com.vcall.scheduling.entity.ScheduleTemplate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SchedulingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    @Mapping(target = "reminderSent", constant = "false")
    @Mapping(target = "type", expression = "java(com.vcall.scheduling.entity.Appointment.AppointmentType.valueOf(request.getType().toUpperCase()))")
    Appointment toEntity(AppointmentRequest request);

    @Mapping(target = "status", expression = "java(appointment.getStatus().name())")
    @Mapping(target = "type", expression = "java(appointment.getType().name())")
    AppointmentResponse toResponse(Appointment appointment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reminderSent", ignore = true)
    void updateEntity(AppointmentRequest request, @MappingTarget Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "type", constant = "WORKING")
    @Mapping(target = "dayOfWeek", expression = "java(java.time.DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase()))")
    ScheduleTemplate toEntity(ScheduleTemplateRequest request);

    @Mapping(target = "dayOfWeek", expression = "java(template.getDayOfWeek().name())")
    @Mapping(target = "type", expression = "java(template.getType().name())")
    ScheduleTemplateResponse toResponse(ScheduleTemplate template);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(ScheduleTemplateRequest request, @MappingTarget ScheduleTemplate template);

    @Mapping(target = "status", expression = "java(availability.getStatus().name())")
    AvailabilityResponse toResponse(AgentAvailability availability);
}
