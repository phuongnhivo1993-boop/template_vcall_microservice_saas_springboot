package com.vcall.survey.mapper;

import com.vcall.survey.dto.request.SurveyAnswerRequest;
import com.vcall.survey.dto.request.SurveyQuestionRequest;
import com.vcall.survey.dto.request.SurveyRequest;
import com.vcall.survey.dto.request.SurveyTemplateRequest;
import com.vcall.survey.dto.response.SurveyAnswerResponse;
import com.vcall.survey.dto.response.SurveyQuestionResponse;
import com.vcall.survey.dto.response.SurveyResponse;
import com.vcall.survey.dto.response.SurveyTemplateResponse;
import com.vcall.survey.entity.Survey;
import com.vcall.survey.entity.SurveyQuestion;
import com.vcall.survey.entity.SurveyResponseEntity;
import com.vcall.survey.entity.SurveyTemplate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SurveyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "type", expression = "java(com.vcall.survey.entity.Survey.SurveyType.valueOf(request.getType().toUpperCase()))")
    Survey toEntity(SurveyRequest request);

    @Mapping(target = "type", expression = "java(survey.getType().name())")
    SurveyResponse toResponse(Survey survey);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(SurveyRequest request, @MappingTarget Survey survey);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "survey", ignore = true)
    @Mapping(target = "questionType", expression = "java(com.vcall.survey.entity.SurveyQuestion.QuestionType.valueOf(request.getQuestionType().toUpperCase()))")
    SurveyQuestion toEntity(SurveyQuestionRequest request);

    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "questionType", expression = "java(question.getQuestionType().name())")
    SurveyQuestionResponse toResponse(SurveyQuestion question);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "survey", ignore = true)
    void updateEntity(SurveyQuestionRequest request, @MappingTarget SurveyQuestion question);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "submittedAt", expression = "java(java.time.LocalDateTime.now())")
    SurveyResponseEntity toEntity(SurveyAnswerRequest request);

    SurveyAnswerResponse toResponse(SurveyResponseEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "trigger", expression = "java(com.vcall.survey.entity.SurveyTemplate.TriggerType.valueOf(request.getTrigger().toUpperCase()))")
    SurveyTemplate toEntity(SurveyTemplateRequest request);

    @Mapping(target = "trigger", expression = "java(template.getTrigger().name())")
    SurveyTemplateResponse toResponse(SurveyTemplate template);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(SurveyTemplateRequest request, @MappingTarget SurveyTemplate template);
}
