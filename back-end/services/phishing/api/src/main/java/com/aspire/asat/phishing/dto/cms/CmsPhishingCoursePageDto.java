package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mirror of the CMS {@code AllResponseDto<List<PhishingCourseEnrollmentDto>>} payload returned
 * by {@code GET /api/v1/client/phishing-course/details}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsPhishingCoursePageDto {

    private Integer offset;
    private Integer pageSize;
    private Long total;
    private List<CmsPhishingCourseEnrollmentDto> items;
}
