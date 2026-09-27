package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Public phishing course enrollment detail row returned by
 * {@code GET /api/v1/phishing/phishing-course/details}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhishingCourseDetailDto {

    private String campaignName;
    private String userName;
    private String email;
    private String department;
    private LocalDate assignedDate;
    private LocalDate expireDate;
    private String status;
    private double progress;
}
