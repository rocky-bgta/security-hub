package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Phishing-local mirror of CMS TopicRespDto (key fields for recommendation display).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsTopicRespDto {

    private String id;
    private String topicName;
    private String description;
    private Integer durationMinutes;
    private String thumbnailUrl;
    private String thumbnailPreviewUrl;
    private String contentTypeId;
    private Integer totalContentCount;
    private String status;
    private List<String> tags;
    private List<String> categoryIds;
    private List<String> countryIds;
    private List<String> complianceIds;
    private Instant createdAt;
    private Integer matchScore;
}
