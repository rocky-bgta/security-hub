package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageExamCreateDto {

    @NotBlank(message = "Exam title cannot be blank")
    private String title;

    @PositiveOrZero(message = "Passing score must be 0 or greater")
    private double passingScore;

    private List<ExamQuestion> questions;

    private String examDetails;
}
