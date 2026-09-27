//package com.aspire.asat.cms.mapper;
//
//import com.aspire.asat.cms.dto.exam.ExamCreationResponseDto;
//import com.aspire.asat.cms.dto.exam.TopicExamInfoDto;
//import com.aspire.asat.cms.model.Exams;
//import com.aspire.asat.cms.model.SubPackage;
//import com.aspire.asat.cms.model.topic.Topic;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class ExamMapperTest {
//
//    @InjectMocks
//    private ExamMapper examMapper;
//
//    private Exams exams;
//    private SubPackage subPackage;
//    private List<Topic> topics;
//    private List<Integer> questionCountsPerTopic;
//
//    @BeforeEach
//    void setUp() {
//        exams = new Exams();
//        exams.setExamId("exam_123");
//        exams.setTitle("Test Exam");
//        exams.setExamDetails("Test Description");
//        exams.setPassingScore(70.0);
//        exams.setTotalQuestions(10);
//        exams.setCreatedAt(java.time.Instant.now());
//
//        subPackage = SubPackage.builder()
//                .id("sub_pkg_123")
//                .name("Test SubPackage")
//                .build();
//
//        topics = Arrays.asList(
//                Topic.builder().id("topic_1").topicName("Topic 1").build(),
//                Topic.builder().id("topic_2").topicName("Topic 2").build()
//        );
//
//        questionCountsPerTopic = Arrays.asList(5, 5);
//    }
//
//    @Test
//    void toExamCreationResponseDto_Success() {
//        // Act
//        ExamCreationResponseDto result = examMapper.toExamCreationResponseDto(
//                exams, subPackage, topics, questionCountsPerTopic);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("exam_123", result.getExamId());
//        assertEquals("Test Exam", result.getExamTitle());
//        assertEquals("Test Description", result.getExamDescription());
//        assertEquals("sub_pkg_123", result.getSubPackageId());
//        assertEquals("Test SubPackage", result.getSubPackageName());
//        assertEquals(70.0, result.getPassingScore());
//        assertEquals("SUCCESS", result.getStatus());
//
//        // Verify topics
//        assertNotNull(result.getTopics());
//        assertEquals(2, result.getTopics().size());
//
//        TopicExamInfoDto topic1 = result.getTopics().get(0);
//        assertEquals("topic_1", topic1.getTopicId());
//        assertEquals("Topic 1", topic1.getTopicName());
//        assertEquals(5, topic1.getQuestionCount());
//        assertEquals(0, topic1.getTotalAvailableQuestions()); // Will be set by service
//
//        TopicExamInfoDto topic2 = result.getTopics().get(1);
//        assertEquals("topic_2", topic2.getTopicId());
//        assertEquals("Topic 2", topic2.getTopicName());
//        assertEquals(5, topic2.getQuestionCount());
//        assertEquals(0, topic2.getTotalAvailableQuestions()); // Will be set by service
//    }
//
//    @Test
//    void toExamCreationResponseDto_NullPackageExam() {
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            examMapper.toExamCreationResponseDto(null, subPackage, topics, questionCountsPerTopic);
//        });
//    }
//
//    @Test
//    void toExamCreationResponseDto_NullSubPackage() {
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            examMapper.toExamCreationResponseDto(exams, null, topics, questionCountsPerTopic);
//        });
//    }
//
//    @Test
//    void toExamCreationResponseDto_NullTopics() {
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            examMapper.toExamCreationResponseDto(exams, subPackage, null, questionCountsPerTopic);
//        });
//    }
//
//    @Test
//    void toExamCreationResponseDto_NullQuestionCounts() {
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            examMapper.toExamCreationResponseDto(exams, subPackage, topics, null);
//        });
//    }
//
//    @Test
//    void toExamCreationResponseDto_EmptyTopics() {
//        // Arrange
//        List<Topic> emptyTopics = Arrays.asList();
//
//        // Act
//        ExamCreationResponseDto result = examMapper.toExamCreationResponseDto(
//                exams, subPackage, emptyTopics, Arrays.asList());
//
//        // Assert
//        assertNotNull(result);
//        assertNotNull(result.getTopics());
//        assertTrue(result.getTopics().isEmpty());
//    }
//
//    @Test
//    void toExamCreationResponseDto_MismatchedTopicAndQuestionCounts() {
//        // Arrange
//        List<Integer> mismatchedCounts = Arrays.asList(3, 7, 2); // 3 counts for 2 topics
//
//        // Act
//        ExamCreationResponseDto result = examMapper.toExamCreationResponseDto(
//                exams, subPackage, topics, mismatchedCounts);
//
//        // Assert
//        assertNotNull(result);
//        assertNotNull(result.getTopics());
//        assertEquals(2, result.getTopics().size());
//
//        // First topic should get first count
//        assertEquals(3, result.getTopics().get(0).getQuestionCount());
//        // Second topic should get second count
//        assertEquals(7, result.getTopics().get(1).getQuestionCount());
//    }
//
//    @Test
//    void toTopicExamInfoDto_Success() {
//        // Arrange
//        Topic topic = Topic.builder()
//                .id("topic_123")
//                .topicName("Test Topic")
//                .build();
//        int questionCount = 5;
//        int totalAvailableQuestions = 10;
//
//        // Act
//        TopicExamInfoDto result = examMapper.toTopicExamInfoDto(topic, questionCount, totalAvailableQuestions);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("topic_123", result.getTopicId());
//        assertEquals("Test Topic", result.getTopicName());
//        assertEquals(5, result.getQuestionCount());
//        assertEquals(10, result.getTotalAvailableQuestions());
//    }
//
//    @Test
//    void toTopicExamInfoDto_NullTopic() {
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            examMapper.toTopicExamInfoDto(null, 5, 10);
//        });
//    }
//
//    @Test
//    void toTopicExamInfoDto_ZeroQuestionCount() {
//        // Arrange
//        Topic topic = Topic.builder()
//                .id("topic_123")
//                .topicName("Test Topic")
//                .build();
//
//        // Act
//        TopicExamInfoDto result = examMapper.toTopicExamInfoDto(topic, 0, 10);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(0, result.getQuestionCount());
//        assertEquals(10, result.getTotalAvailableQuestions());
//    }
//
//    @Test
//    void toTopicExamInfoDto_ZeroTotalAvailable() {
//        // Arrange
//        Topic topic = Topic.builder()
//                .id("topic_123")
//                .topicName("Test Topic")
//                .build();
//
//        // Act
//        TopicExamInfoDto result = examMapper.toTopicExamInfoDto(topic, 5, 0);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(5, result.getQuestionCount());
//        assertEquals(0, result.getTotalAvailableQuestions());
//    }
//}
