package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDetailsResponseDTO {

    @NotBlank
    private String topicId;

    @NotBlank
    private String topicName;

    private String topicDescription;

    private Instant publishDate;

    @NotNull
    private Double progress;

    @NotNull
    private Integer chapterCount;

    @NotNull
    private Integer contentCount;

    @NotNull
    private List<ChapterDetailsDTO> chapters;

    private String thumbnailUrl;

    private String certificateUrl;

    private String ImageCertificateLink;

    // Additional topic-specific fields
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private boolean isSaved; // Bookmark status
    private Integer durationMinutes;
    private List<String> categoryIds;
    private List<String> countryIds;
    private List<String> complianceIds;
    private String contentTypeId;
}
