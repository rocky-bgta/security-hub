package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.TopicQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicQuestionRepository extends MongoRepository<TopicQuestion, String> {
    
    /**
     * Find all questions for a specific topic
     */
    List<TopicQuestion> findByTopicId(String topicId);
    
    /**
     * Find all questions for multiple topics
     */
    @Query("{ 'topicId': { $in: ?0 } }")
    List<TopicQuestion> findByTopicIdIn(List<String> topicIds);
    
    /**
     * Check if a question is already associated with a topic
     */
    boolean existsByTopicIdAndQuestionId(String topicId, String  questionId);
    
    /**
     * Find a specific topic-question association
     */
    Optional<TopicQuestion> findByTopicIdAndQuestionId(String topicId, String  questionId);
    
    /**
     * Count questions for a specific topic
     */
    long countByTopicId(String topicId);
    
    /**
     * Count questions for multiple topics
     */
    @Query(value = "{ 'topicId': { $in: ?0 } }", count = true)
    long countByTopicIdIn(List<String> topicIds);
    
    /**
     * Delete all topic-question associations for a topic
     */
    void deleteByTopicId(String topicId);
    
    /**
     * Delete a specific topic-question association
     */
    void deleteByTopicIdAndQuestionId(String topicId, String questionId);
}
