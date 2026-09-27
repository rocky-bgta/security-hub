package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreationResponseDto {
    
    private String examId;
    private String examTitle;
    private String examDescription;
    private String subPackageId;
    private String subPackageName;
    private Integer totalQuestions;
    private Double passingScore;
    private List<TopicExamInfoDto> topics;
    private Instant createdAt;
    private String status;
}
