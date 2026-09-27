package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ExamQuestionsAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository extends MongoRepository<ExamQuestionsAttempt, String> {

    /**
     * Find all attempts for a specific exam and user
     */
    List<ExamQuestionsAttempt> findByExamIdAndUserId(String examId, String userId);

    /**
     * Find attempt for a specific exam, user, and question
     */
    Optional<ExamQuestionsAttempt> findByExamIdAndUserIdAndQuestionId(String examId, String userId, String questionId);

    /**
     * Count total attempts for an exam by user
     */
    long countByExamIdAndUserId(String examId, String userId);

    /**
     * Count correct attempts for an exam by user
     */
    @Query("{ 'examId': ?0, 'userId': ?1, 'isCorrect': true }")
    long countCorrectAttemptsByExamIdAndUserId(String examId, String userId);

    /**
     * Check if user has attempted a specific question in an exam
     */
    boolean existsByExamIdAndUserIdAndQuestionId(String examId, String userId, String questionId);

    /**
     * Find all attempts for a specific exam
     */
    List<ExamQuestionsAttempt> findByExamId(String examId);

    /**
     * Delete all attempts for a specific exam and user
     */
    void deleteByExamIdAndUserId(String examId, String userId);
}
