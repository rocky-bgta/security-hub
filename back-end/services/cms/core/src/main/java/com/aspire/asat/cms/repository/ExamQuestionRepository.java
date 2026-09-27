package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ExamQuestionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamQuestionRepository extends MongoRepository<ExamQuestionEntity, String> {

    /**
     * Find all questions for a specific exam ordered by questionOrder
     */
    List<ExamQuestionEntity> findByExamIdOrderByQuestionOrder(String examId);

    /**
     * Find a specific question by exam ID and question order
     */
    Optional<ExamQuestionEntity> findByExamIdAndQuestionOrder(String examId, int questionOrder);

    /**
     * Find a specific question by exam ID and question ID
     */
    Optional<ExamQuestionEntity> findByExamIdAndQuestionId(String examId, String questionId);

    /**
     * Count total questions for an exam
     */
    long countByExamId(String examId);

    /**
     * Find questions by exam ID and topic ID
     */
    List<ExamQuestionEntity> findByExamIdAndTopicId(String examId, String topicId);

    /**
     * Delete all questions for a specific exam
     */
    void deleteByExamId(String examId);

    /**
     * Check if a question exists in an exam
     */
    boolean existsByExamIdAndQuestionId(String examId, String questionId);

    /**
     * Get the next question order for an exam
     */
    @Query(value = "{ 'examId': ?0 }", sort = "{ 'questionOrder': -1 }")
    Optional<ExamQuestionEntity> findTopByExamIdOrderByQuestionOrderDesc(String examId);
}
