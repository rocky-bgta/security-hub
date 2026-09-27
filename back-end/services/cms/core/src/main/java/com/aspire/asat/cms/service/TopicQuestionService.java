package com.aspire.asat.cms.service;

import com.aspire.asat.cms.model.TopicQuestion;
import com.aspire.asat.cms.model.question.Question;
import com.aspire.asat.cms.repository.TopicQuestionRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopicQuestionService {

    private final TopicQuestionRepository topicQuestionRepository;
    private final QuestionCustomRepository questionRepository;

    /**
     * Associates a question with a topic
     */
    public TopicQuestion associateQuestionWithTopic(String topicId, String questionId, String createdBy) {
        // Check if the association already exists
        if (topicQuestionRepository.existsByTopicIdAndQuestionId(topicId, questionId)) {
            log.warn("Question {} is already associated with topic {}", questionId, topicId);
            return topicQuestionRepository.findByTopicIdAndQuestionId(topicId, questionId)
                    .orElse(null);
        }

        TopicQuestion topicQuestion = TopicQuestion.create(topicId, String.valueOf(questionId), createdBy);
        return topicQuestionRepository.save(topicQuestion);
    }

    /**
     * Removes association between a question and topic
     */
    public void removeQuestionFromTopic(String topicId, String questionId) {
        topicQuestionRepository.deleteByTopicIdAndQuestionId(topicId, questionId);
        log.info("Removed question {} from topic {}", questionId, topicId);
    }

    /**
     * Gets all questions for a specific topic
     */
    public List<Question> getQuestionsForTopic(String topicId) {
        List<TopicQuestion> topicQuestions = topicQuestionRepository.findByTopicId(topicId);
        List<String> questionIds = topicQuestions.stream()
                .map(TopicQuestion::getQuestionId)
                .toList();

        return questionRepository.findAllById(questionIds);
    }

    /**
     * Gets all questions for multiple topics
     */
    public List<Question> getQuestionsForTopics(List<String> topicIds) {
        List<TopicQuestion> topicQuestions = topicQuestionRepository.findByTopicIdIn(topicIds);
        List<String> questionIds = topicQuestions.stream()
                .map(TopicQuestion::getQuestionId)
                .distinct()
                .toList();

        return questionRepository.findAllById(questionIds);
    }

    /**
     * Counts questions for a specific topic
     */
    public long countQuestionsForTopic(String topicId) {
        return topicQuestionRepository.countByTopicId(topicId);
    }

    /**
     * Counts questions for multiple topics
     */
    public long countQuestionsForTopics(List<String> topicIds) {
        return topicQuestionRepository.countByTopicIdIn(topicIds);
    }

    /**
     * Removes all question associations for a topic
     */
    public void removeAllQuestionsFromTopic(String topicId) {
        topicQuestionRepository.deleteByTopicId(topicId);
        log.info("Removed all question associations for topic {}", topicId);
    }
}
