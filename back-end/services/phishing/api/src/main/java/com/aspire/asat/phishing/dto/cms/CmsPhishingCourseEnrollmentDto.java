package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsPhishingCourseEnrollmentDto {

    private String userId;
    private String subPackageId;
    private String subPackageName;
    private LocalDate assignedDate;
    private LocalDate expiryDate;
    private String status;
    private double progress;
}
