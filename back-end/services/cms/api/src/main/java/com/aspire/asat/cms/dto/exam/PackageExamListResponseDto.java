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
public class PackageExamListResponseDto {
    private String examId;
    private String packageId;
    private String packageName;
    private String title;
    private double passingScore;
    private String examDetails;
    private List<ExamQuestion> questions;
}
