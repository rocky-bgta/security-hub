package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageExamResponseDTO {

    private String examId;
    private String title;
    private double passingScore;
    private List<ExamQuestionResponseDTO> questions;
}
