package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phishing-local mirror of CMS {@code TopicReqDto} (only the fields we send when creating the
 * per-client "microContent" topic). Sent to {@code POST {service.cms.url}/topics}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsTopicCreateRequest {

    private String topicName;
    private List<String> categoryIds;
    private List<String> countryIds;
    private List<String> complianceIds;
    private String contentTypeId;
    private Integer durationMinutes;
    private String description;
    private String thumbnailUrl;
    private List<CmsProductPackageMapping> productPackages;
    private List<String> tags;
    private String status;
    /** When set, CMS stores the topic as private to this client. */
    private String clientId;
}
