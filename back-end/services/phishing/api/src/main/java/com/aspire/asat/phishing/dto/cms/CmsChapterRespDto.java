package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Phishing-local mirror of CMS {@code ChapterResponseDto} (key fields returned on chapter creation).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsChapterRespDto {

    private String id;
    private String topicId;
    private String chapterName;
    private String chapterDescription;
    private Integer position;
    private String chapterStatus;
    private List<String> contentIds;
}
