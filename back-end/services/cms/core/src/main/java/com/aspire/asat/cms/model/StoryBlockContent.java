package com.aspire.asat.cms.model;

import com.aspire.asat.cms.dto.content.ContentCommonDto;
import com.aspire.asat.cms.dto.content.ContentReqDto;
import com.aspire.asat.cms.dto.content.ContentRespDto;
import com.aspire.asat.cms.dto.content.formatting.TextBackgroundSetting;
import com.aspire.asat.cms.dto.content.storyblock.*;
import com.aspire.asat.cms.dto.enums.CommonStatus;
import com.aspire.asat.cms.dto.enums.ContentType;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

//@EqualsAndHashCode(callSuper = true)
@Data
@Builder
//@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "content")
public class StoryBlockContent {
    private String id;
    private String contentName;
    private String description;
    private ContentType contentType;
    private CommonStatus status;
    private String author;
    private List<String> chapterIds;
    private List<String> tags;
    private List<StepDto> steps;
    private PopUpDto popUp;
    private String advisorInstructionText;
    private ActionButtonDto actionButton;
    private CorrectSituationDto correctSituation;
    private TextBackgroundSetting backgroundSettings;
    private boolean highContrastModeEnabled;
    private Instant createdAt;
    private Instant updatedAt;

    public static StoryBlockContent toStoryBlockContent(String id, ContentReqDto<StoryBlockContentDto> dto, Instant createdAt, Instant updatedAt) {
        return StoryBlockContent.builder()
                .id(id)
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .steps(dto.getSpecific().getSteps())
                .popUp(dto.getSpecific().getPopUp())
                .advisorInstructionText(dto.getSpecific().getAdvisorInstructionText())
                .actionButton(dto.getSpecific().getActionButton())
                .correctSituation(dto.getSpecific().getCorrectSituation())
                .backgroundSettings(dto.getSpecific().getBackgroundSettings())
                .highContrastModeEnabled(dto.getSpecific().isHighContrastModeEnabled())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static StoryBlockContent toUpdateStoryBlockContent(StoryBlockContent storyBlockContent, ContentReqDto<StoryBlockContentDto> dto, Instant updatedAt) {
        return StoryBlockContent.builder()
                .id(storyBlockContent.getId())
                .contentName(dto.getCommon().getContentName())
                .description(dto.getCommon().getDescription())
                .contentType(dto.getCommon().getContentType())
                .status(dto.getCommon().getStatus())
                .author(dto.getCommon().getAuthor())
                .chapterIds(dto.getCommon().getChapterIds())
                .tags(dto.getCommon().getTags())
                .steps(dto.getSpecific().getSteps())
                .popUp(dto.getSpecific().getPopUp())
                .advisorInstructionText(dto.getSpecific().getAdvisorInstructionText())
                .actionButton(dto.getSpecific().getActionButton())
                .correctSituation(dto.getSpecific().getCorrectSituation())
                .backgroundSettings(dto.getSpecific().getBackgroundSettings())
                .highContrastModeEnabled(dto.getSpecific().isHighContrastModeEnabled())
                .createdAt(storyBlockContent.getCreatedAt())
                .updatedAt(updatedAt)
                .build();
    }

    public static ContentRespDto<StoryBlockContentDto> toContentRespDto(StoryBlockContent storyBlockContent) {
        return ContentRespDto.<StoryBlockContentDto>builder()
                .common(ContentCommonDto.builder()
                        .contentName(storyBlockContent.getContentName())
                        .description(storyBlockContent.getDescription())
                        .contentType(storyBlockContent.getContentType())
                        .status(storyBlockContent.getStatus())
                        .author(storyBlockContent.getAuthor())
                        .chapterIds(storyBlockContent.getChapterIds())
                        .tags(storyBlockContent.getTags())
                        .build())
                .specific(StoryBlockContentDto.builder()
                        .steps(storyBlockContent.getSteps())
                        .popUp(storyBlockContent.getPopUp())
                        .advisorInstructionText(storyBlockContent.getAdvisorInstructionText())
                        .actionButton(storyBlockContent.getActionButton())
                        .correctSituation(storyBlockContent.getCorrectSituation())
                        .backgroundSettings(storyBlockContent.getBackgroundSettings())
                        .highContrastModeEnabled(storyBlockContent.isHighContrastModeEnabled())
                        .build())
                .build();
    }

}
