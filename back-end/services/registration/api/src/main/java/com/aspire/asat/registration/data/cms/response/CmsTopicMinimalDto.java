package com.aspire.asat.registration.data.cms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal topic details from CMS")
public class CmsTopicMinimalDto {

    private String topicId;
    private String topicName;
    private String description;
    private Integer durationMinutes;
    private String thumbnail;
    private String contentType;
    private List<String> category;
}
