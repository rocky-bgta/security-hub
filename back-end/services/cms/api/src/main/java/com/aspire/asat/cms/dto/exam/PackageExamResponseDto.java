package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageExamResponseDto {
    private String id;
    private String packageId;
    private String title;
    private double passingScore;
    private List<ExamQuestion> questions;
    private String examDetails;
}
