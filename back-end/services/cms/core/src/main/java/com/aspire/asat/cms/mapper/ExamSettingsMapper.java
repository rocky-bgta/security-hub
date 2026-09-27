package com.aspire.asat.cms.mapper;

import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsCreateRequestDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsUpdateRequestDto;
import com.aspire.asat.cms.model.ExamSettings;
import com.aspire.asat.cms.util.CommonUtil;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExamSettingsMapper {

    /**
     * Convert ClientExamSettings entity to ClientExamSettingsDto
     */
    public ExamSettingsDto toDto(ExamSettings settings) {
        return getClientExamSettingsDto(settings);
    }

    /**
     * Convert ClientExamSettingsCreateRequestDto to ClientExamSettings entity
     */
    public ExamSettings toEntity(ExamSettingsCreateRequestDto dto) {
        return toClientExamSettings(dto);
    }

    /**
     * Update the existing ClientExamSettings entity with ExamSettingsUpdateRequestDto
     */
    public ExamSettings updateEntity(ExamSettings existing, ExamSettingsUpdateRequestDto dto) {
        return toUpdatedClientExamSettings(existing, dto);
    }

    private static ExamSettingsDto getClientExamSettingsDto(ExamSettings settings) {
        if (settings == null) {
            return null;
        }

        return ExamSettingsDto.builder()
                .id(settings.getId())
                .clientId(settings.getClientId())
                .timeLimitMinutes(settings.getTimeLimitMinutes())
                .passingScore(settings.getPassingScore())
                .retakePolicy(settings.getRetakePolicy())
                .totalQuestions(settings.getTotalQuestions())
                .distributionStrategy(settings.getDistributionStrategy())
                .customDistribution(mapToDtoCustomDistribution(settings.getCustomDistribution()))
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .isActive(settings.getIsActive())
                .createdBy(settings.getCreatedBy())
                .updatedBy(settings.getUpdatedBy())
                .defaultSettings(settings.getDefaultSettings())
                .build();
    }

    /**
     * Convert ClientExamSettingsCreateRequestDto to ClientExamSettings entity
     */
    public static ExamSettings toClientExamSettings(ExamSettingsCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Instant now = Instant.now();
        return ExamSettings.builder()
                .id(CommonUtil.generateUUID())
                .clientId(dto.getClientId())
                .timeLimitMinutes(dto.getTimeLimitMinutes())
                .passingScore(dto.getPassingScore())
                .retakePolicy(dto.getRetakePolicy())
                .totalQuestions(dto.getTotalQuestions())
                .distributionStrategy(dto.getDistributionStrategy())
                .customDistribution(mapToEntityCustomDistribution(dto.getCustomDistribution()))
                .createdAt(now)
                .updatedAt(now)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE)
                .createdBy(CommonUtil.getLoggedInUser())
                .defaultSettings(Boolean.FALSE)
                .build();
    }

    /**
     * Convert ExamSettingsUpdateRequestDto to ClientExamSettings entity
     */
    private static ExamSettings toUpdatedClientExamSettings(ExamSettings existing, ExamSettingsUpdateRequestDto dto) {
        if (existing == null || dto == null) {
            return existing;
        }

        return ExamSettings.builder()
                .id(existing.getId())
                .clientId(existing.getClientId())
                .timeLimitMinutes(dto.getTimeLimitMinutes() != null ? dto.getTimeLimitMinutes() : existing.getTimeLimitMinutes())
                .passingScore(dto.getPassingScore() != null ? dto.getPassingScore() : existing.getPassingScore())
                .retakePolicy(dto.getRetakePolicy() != null ? dto.getRetakePolicy() : existing.getRetakePolicy())
                .totalQuestions(dto.getTotalQuestions() != null ? dto.getTotalQuestions() : existing.getTotalQuestions())
                .distributionStrategy(dto.getDistributionStrategy() != null ? dto.getDistributionStrategy() : existing.getDistributionStrategy())
                .customDistribution(dto.getCustomDistribution() != null ? mapToEntityCustomDistributionFromUpdateDto(dto.getCustomDistribution()) : existing.getCustomDistribution())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : existing.getIsActive())
                .createdBy(existing.getCreatedBy())
                .updatedBy(CommonUtil.getLoggedInUser())
                .defaultSettings(existing.getDefaultSettings())
                .build();
    }

    /**
     * Map entity custom distribution to DTO custom distribution
     */
    private static List<ExamSettingsDto.TopicQuestionDistributionDto> mapToDtoCustomDistribution(List<ExamSettings.TopicQuestionDistribution> entityList) {
        if (entityList == null) {
            return null;
        }
        return entityList.stream()
                .map(entity -> ExamSettingsDto.TopicQuestionDistributionDto.builder()
                        .topicId(entity.getTopicId())
                        .questionCount(entity.getQuestionCount())
                        .build())
                .toList();
    }

    /**
     * Map DTO custom distribution to entity custom distribution
     */
    private static List<ExamSettings.TopicQuestionDistribution> mapToEntityCustomDistribution(List<ExamSettingsCreateRequestDto.TopicQuestionDistributionDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream()
                .map(dto -> ExamSettings.TopicQuestionDistribution.builder()
                        .topicId(dto.getTopicId())
                        .questionCount(dto.getQuestionCount())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Map update DTO custom distribution to entity custom distribution
     */
    private static List<ExamSettings.TopicQuestionDistribution> mapToEntityCustomDistributionFromUpdateDto(List<ExamSettingsUpdateRequestDto.TopicQuestionDistributionDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream()
                .map(dto -> ExamSettings.TopicQuestionDistribution.builder()
                        .topicId(dto.getTopicId())
                        .questionCount(dto.getQuestionCount())
                        .build())
                .collect(Collectors.toList());
    }
}
