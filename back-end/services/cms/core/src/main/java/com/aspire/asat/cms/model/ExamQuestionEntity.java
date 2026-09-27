package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Entity representing the relationship between exams and questions.
 * This replaces the comma-separated questionIds string with a proper normalized table structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "exam_questions")
@CompoundIndex(name = "exam_question_idx", def = "{'examId': 1, 'questionId': 1}", unique = true)
public class ExamQuestionEntity {

    @Id
    private String id;

    @Indexed
    private String examId;

    @Indexed
    private String questionId;

    private int questionOrder; // Order of question in the exam (1, 2, 3, etc.)

    private String topicId; // Reference to the topic this question belongs to

    private Instant createdAt;

    private Instant updatedAt;
}
