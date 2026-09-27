package com.aspire.asat.cms.service;

import com.aspire.asat.cms.model.TopicQuestion;
import com.aspire.asat.cms.model.question.Question;
import com.aspire.asat.cms.repository.TopicQuestionRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicQuestionServiceTest {

    @Mock
    private TopicQuestionRepository topicQuestionRepository;

    @Mock
    private QuestionCustomRepository questionRepository;

    @InjectMocks
    private TopicQuestionService topicQuestionService;

    private String topicId;
    private String questionId;
    private String createdBy;
    private TopicQuestion topicQuestion;
    private Question question;

    @BeforeEach
    void setUp() {
        topicId = "topic_123";
        questionId = UUID.randomUUID().toString();
        createdBy = "user_123";
        
        topicQuestion = TopicQuestion.builder()
                .id("tq_123")
                .topicId(topicId)
                .questionId(questionId)
                .createdBy(createdBy)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        question = Question.builder()
                .id(questionId)
                .questionText("Test Question?")
                .build();
    }

    @Test
    void associateQuestionWithTopic_Success() {
        // Arrange
        when(topicQuestionRepository.existsByTopicIdAndQuestionId(topicId, questionId)).thenReturn(false);
        when(topicQuestionRepository.save(any(TopicQuestion.class))).thenReturn(topicQuestion);

        // Act
        TopicQuestion result = topicQuestionService.associateQuestionWithTopic(topicId, questionId, createdBy);

        // Assert
        assertNotNull(result);
        assertEquals(topicId, result.getTopicId());
        assertEquals(questionId, result.getQuestionId());
        assertEquals(createdBy, result.getCreatedBy());
        verify(topicQuestionRepository).save(any(TopicQuestion.class));
    }

    @Test
    void associateQuestionWithTopic_AlreadyExists() {
        // Arrange
        when(topicQuestionRepository.existsByTopicIdAndQuestionId(topicId, questionId)).thenReturn(true);
        when(topicQuestionRepository.findByTopicIdAndQuestionId(topicId, questionId)).thenReturn(Optional.of(topicQuestion));

        // Act
        TopicQuestion result = topicQuestionService.associateQuestionWithTopic(topicId, questionId, createdBy);

        // Assert
        assertNotNull(result);
        assertEquals(topicQuestion, result);
        verify(topicQuestionRepository, never()).save(any(TopicQuestion.class));
    }

    @Test
    void removeQuestionFromTopic_Success() {
        // Arrange
        doNothing().when(topicQuestionRepository).deleteByTopicIdAndQuestionId(topicId, questionId);

        // Act
        topicQuestionService.removeQuestionFromTopic(topicId, questionId);

        // Assert
        verify(topicQuestionRepository).deleteByTopicIdAndQuestionId(topicId, questionId);
    }

    @Test
    void getQuestionsForTopic_Success() {
        // Arrange
        List<TopicQuestion> topicQuestions = Arrays.asList(topicQuestion);
        List<Question> Question = Arrays.asList(question);
        
        when(topicQuestionRepository.findByTopicId(topicId)).thenReturn(topicQuestions);
        when(questionRepository.findAllById(Arrays.asList(questionId))).thenReturn(Question);

        // Act
        List<Question> result = topicQuestionService.getQuestionsForTopic(topicId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(question, result.get(0));
        verify(topicQuestionRepository).findByTopicId(topicId);
        verify(questionRepository).findAllById(Arrays.asList(questionId));
    }

    @Test
    void getQuestionsForTopics_Success() {
        // Arrange
        List<String> topicIds = Arrays.asList("topic_1", "topic_2");
        List<TopicQuestion> topicQuestions = Arrays.asList(
                TopicQuestion.builder().topicId("topic_1").questionId(String.valueOf(UUID.randomUUID())).build(),
                TopicQuestion.builder().topicId("topic_2").questionId(String.valueOf(UUID.randomUUID())).build()
        );
        List<Question> Question = Arrays.asList(question);
        
        when(topicQuestionRepository.findByTopicIdIn(topicIds)).thenReturn(topicQuestions);
        when(questionRepository.findAllById(anyList())).thenReturn(Question);

        // Act
        List<Question> result = topicQuestionService.getQuestionsForTopics(topicIds);

        // Assert
        assertNotNull(result);
        verify(topicQuestionRepository).findByTopicIdIn(topicIds);
        verify(questionRepository).findAllById(anyList());
    }

    @Test
    void countQuestionsForTopic_Success() {
        // Arrange
        when(topicQuestionRepository.countByTopicId(topicId)).thenReturn(5L);

        // Act
        long result = topicQuestionService.countQuestionsForTopic(topicId);

        // Assert
        assertEquals(5L, result);
        verify(topicQuestionRepository).countByTopicId(topicId);
    }

    @Test
    void countQuestionsForTopics_Success() {
        // Arrange
        List<String> topicIds = Arrays.asList("topic_1", "topic_2");
        when(topicQuestionRepository.countByTopicIdIn(topicIds)).thenReturn(10L);

        // Act
        long result = topicQuestionService.countQuestionsForTopics(topicIds);

        // Assert
        assertEquals(10L, result);
        verify(topicQuestionRepository).countByTopicIdIn(topicIds);
    }

    @Test
    void removeAllQuestionsFromTopic_Success() {
        // Arrange
        doNothing().when(topicQuestionRepository).deleteByTopicId(topicId);

        // Act
        topicQuestionService.removeAllQuestionsFromTopic(topicId);

        // Assert
        verify(topicQuestionRepository).deleteByTopicId(topicId);
    }

    @Test
    void getQuestionsForTopic_EmptyResult() {
        // Arrange
        when(topicQuestionRepository.findByTopicId(topicId)).thenReturn(Collections.emptyList());
        when(questionRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<Question> result = topicQuestionService.getQuestionsForTopic(topicId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(topicQuestionRepository).findByTopicId(topicId);
        verify(questionRepository).findAllById(Collections.emptyList());
    }

    @Test
    void getQuestionsForTopics_EmptyResult() {
        // Arrange
        List<String> topicIds = Arrays.asList("topic_1", "topic_2");
        when(topicQuestionRepository.findByTopicIdIn(topicIds)).thenReturn(Collections.emptyList());
        when(questionRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<Question> result = topicQuestionService.getQuestionsForTopics(topicIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(topicQuestionRepository).findByTopicIdIn(topicIds);
        verify(questionRepository).findAllById(Collections.emptyList());
    }
}
