package com.aspire.asat.cms.dto.clientAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalyticsDTO {
    private String productId;
    private String courseName;
    private int totalEnrolled;
    private int completed;
    private int inProgress;
    private int notStarted;
    private int completionRate;
}
