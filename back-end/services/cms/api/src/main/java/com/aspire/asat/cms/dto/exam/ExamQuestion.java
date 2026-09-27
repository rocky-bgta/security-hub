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
public class ExamQuestion {

    private String questionId;          // Unique ID for question (UUID or generated)

    private String text;                // Question text

    private List<String> options;       // Example: ["Java", "Python", "HTML", "C++"]

    private List<String> correctAnswers; // Example: ["Java", "Python"]

    private QuestionType questionType;           // Type of question

}

