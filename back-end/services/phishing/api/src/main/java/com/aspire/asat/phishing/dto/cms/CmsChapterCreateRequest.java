package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phishing-local mirror of CMS {@code ChapterRequestDto}. Sent to {@code POST {service.cms.url}/chapters}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsChapterCreateRequest {

    private String topicId;
    private String chapterName;
    private String chapterDescription;
    private Integer position;
    private String chapterStatus;
    private List<String> contentIds;
}
