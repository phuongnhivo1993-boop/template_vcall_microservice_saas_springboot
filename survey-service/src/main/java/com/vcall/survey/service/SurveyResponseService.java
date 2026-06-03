package com.vcall.survey.service;

import com.vcall.common.exception.ResourceNotFoundException;
import com.vcall.survey.dto.request.SurveyAnswerRequest;
import com.vcall.survey.dto.response.SurveyAnswerResponse;
import com.vcall.survey.dto.response.SurveyStatsResponse;
import com.vcall.survey.entity.Survey;
import com.vcall.survey.entity.SurveyResponseEntity;
import com.vcall.survey.mapper.SurveyMapper;
import com.vcall.survey.repository.SurveyRepository;
import com.vcall.survey.repository.SurveyResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyResponseService {

    private final SurveyResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyMapper mapper;

    @Transactional
    public SurveyAnswerResponse submitAnswer(SurveyAnswerRequest request) {
        SurveyResponseEntity entity = mapper.toEntity(request);
        entity = responseRepository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public SurveyAnswerResponse getAnswer(UUID id) {
        SurveyResponseEntity entity = responseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey response not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<SurveyAnswerResponse> getAllAnswers(Pageable pageable) {
        return responseRepository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SurveyAnswerResponse> searchAnswers(Specification<SurveyResponseEntity> spec, Pageable pageable) {
        return responseRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SurveyStatsResponse getStats(UUID surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found with id: " + surveyId));

        long totalResponses = responseRepository.countBySurveyId(surveyId);
        Double avgRating = responseRepository.averageRatingBySurveyId(surveyId);

        SurveyStatsResponse.SurveyStatsResponseBuilder builder = SurveyStatsResponse.builder()
                .totalResponses(totalResponses)
                .averageRating(avgRating != null ? avgRating : 0.0);

        if (survey.getType() == Survey.SurveyType.CSAT) {
            builder.csatCount(totalResponses);
            builder.csatScore(avgRating != null ? (avgRating / 5.0) * 100.0 : 0.0);
        } else if (survey.getType() == Survey.SurveyType.NPS) {
            long promoters = responseRepository.countPromoters(surveyId);
            long passives = responseRepository.countPassives(surveyId);
            long detractors = responseRepository.countDetractors(surveyId);
            builder.npsCount(totalResponses);
            builder.promoterCount(promoters);
            builder.passiveCount(passives);
            builder.detractorCount(detractors);
            if (totalResponses > 0) {
                int npsScore = (int) Math.round(((double) promoters - detractors) / totalResponses * 100.0);
                builder.npsScore(npsScore);
            } else {
                builder.npsScore(0);
            }
        }

        return builder.build();
    }
}
