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
import java.util.List;

/**
 * Entity representing individual exam attempts for tracking user answers
 * and calculating exam results from submitted answers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "exam_question_attempts")
@CompoundIndex(name = "exam_user_idx", def = "{'examId': 1, 'userId': 1}")
public class ExamQuestionsAttempt {

    @Id
    private String id;

    @Indexed
    private String examId;

    @Indexed
    private String userId;

    @Indexed
    private String questionId;

    private List<String> submittedAnswers;

    private boolean isCorrect;

    private Instant submittedAt;

    private Instant createdAt;

    private Instant updatedAt;
}
