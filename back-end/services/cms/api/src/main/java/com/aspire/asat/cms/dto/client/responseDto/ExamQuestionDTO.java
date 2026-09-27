package com.aspire.asat.cms.dto.client.responseDto;

import com.aspire.asat.cms.dto.exam.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDTO {
    private String questionId;
    private String text;
    private List<String> options;
    private QuestionType questionType;
}
