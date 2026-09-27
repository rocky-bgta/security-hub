package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponseDTO {

    @Min(0)
    private int totalCourses;

    @Min(0)
    private int completedCourses;

    @Min(0)
    private int inProgressCourses;

    @Min(0)
    private int pendingCourses;

    @Min(0)
    private int totalCertificates;
}
