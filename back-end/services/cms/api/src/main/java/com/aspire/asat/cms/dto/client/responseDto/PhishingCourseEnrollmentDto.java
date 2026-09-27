package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhishingCourseEnrollmentDto {

    private String userId;
    private String subPackageId;
    private String subPackageName;
    private LocalDate assignedDate;
    private LocalDate expiryDate;
    private String status;
    private double progress;
}
