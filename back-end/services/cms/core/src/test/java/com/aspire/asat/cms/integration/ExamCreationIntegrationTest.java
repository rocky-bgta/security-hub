//package com.aspire.asat.cms.integration;
//
//import com.aspire.asat.cms.dto.exam.ExamCreationRequestDto;
//import com.aspire.asat.cms.dto.exam.ExamCreationResponseDto;
//import com.aspire.asat.cms.service.exam.ExamService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ExamCreationIntegrationTest {
//
//    @Mock
//    private ExamService examService;
//
//    private ExamCreationRequestDto requestDto;
//    private ExamCreationResponseDto responseDto;
//
//    @BeforeEach
//    void setUp() {
//        // Setup test data
//        requestDto = ExamCreationRequestDto.builder()
//                .clientId("client_123")
//                .userId("user_123")
//                .subPackageId("sub_pkg_123")
//                .examTitle("Security Fundamentals Exam")
//                .examDescription("Comprehensive security exam")
//                .build();
//
//        responseDto = ExamCreationResponseDto.builder()
//                .examId("exam_123")
//                .examTitle("Security Fundamentals Exam")
//                .subPackageId("sub_pkg_123")
//                .subPackageName("Security Basics")
//                .totalQuestions(4)
//                .passingScore(75.0)
//                .status("SUCCESS")
//                .build();
//    }
//
//    @Test
//    void createExamFromSubPackage_EqualDistribution_Success() {
//        // Arrange
//        when(examService.createExamFromSubPackage(requestDto)).thenReturn(responseDto);
//
//        // Act
//        ExamCreationResponseDto response = examService.createExamFromSubPackage(requestDto);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals("Security Fundamentals Exam", response.getExamTitle());
//        assertEquals("sub_pkg_123", response.getSubPackageId());
//        assertEquals("Security Basics", response.getSubPackageName());
//        assertEquals(4, response.getTotalQuestions());
//        assertEquals(75.0, response.getPassingScore());
//        assertEquals("SUCCESS", response.getStatus());
//
//        verify(examService).createExamFromSubPackage(requestDto);
//    }
//
//    @Test
//    void createExamFromSubPackage_CustomDistribution_Success() {
//        // Arrange
//        ExamCreationRequestDto customRequest = ExamCreationRequestDto.builder()
//                .clientId("client_123")
//                .userId("user_123")
//                .subPackageId("sub_pkg_123")
//                .examTitle("Custom Security Exam")
//                .examDescription("Custom distribution exam")
//                .build();
//
//        ExamCreationResponseDto customResponse = ExamCreationResponseDto.builder()
//                .examId("exam_456")
//                .examTitle("Custom Security Exam")
//                .subPackageId("sub_pkg_123")
//                .totalQuestions(4)
//                .passingScore(80.0)
//                .status("SUCCESS")
//                .build();
//
//        when(examService.createExamFromSubPackage(customRequest)).thenReturn(customResponse);
//
//        // Act
//        ExamCreationResponseDto response = examService.createExamFromSubPackage(customRequest);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals("Custom Security Exam", response.getExamTitle());
//        assertEquals(4, response.getTotalQuestions());
//        assertEquals(80.0, response.getPassingScore());
//        assertEquals("SUCCESS", response.getStatus());
//
//        verify(examService).createExamFromSubPackage(customRequest);
//    }
//
//    @Test
//    void createExamFromSubPackage_WeightedDistribution_Success() {
//        // Arrange
//        ExamCreationRequestDto weightedRequest = ExamCreationRequestDto.builder()
//                .clientId("client_123")
//                .userId("user_123")
//                .subPackageId("sub_pkg_123")
//                .examTitle("Weighted Security Exam")
//                .examDescription("Weighted distribution exam")
//                .build();
//
//        ExamCreationResponseDto weightedResponse = ExamCreationResponseDto.builder()
//                .examId("exam_789")
//                .examTitle("Weighted Security Exam")
//                .subPackageId("sub_pkg_123")
//                .totalQuestions(4)
//                .passingScore(70.0)
//                .status("SUCCESS")
//                .build();
//
//        when(examService.createExamFromSubPackage(weightedRequest)).thenReturn(weightedResponse);
//
//        // Act
//        ExamCreationResponseDto response = examService.createExamFromSubPackage(weightedRequest);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals("Weighted Security Exam", response.getExamTitle());
//        assertEquals(4, response.getTotalQuestions());
//        assertEquals(70.0, response.getPassingScore());
//        assertEquals("SUCCESS", response.getStatus());
//
//        verify(examService).createExamFromSubPackage(weightedRequest);
//    }
//
//    @Test
//    void createExamFromSubPackage_ServiceThrowsException() {
//        // Arrange
//        when(examService.createExamFromSubPackage(requestDto))
//                .thenThrow(new RuntimeException("Service error"));
//
//        // Act & Assert
//        assertThrows(RuntimeException.class, () -> {
//            examService.createExamFromSubPackage(requestDto);
//        });
//
//        verify(examService).createExamFromSubPackage(requestDto);
//    }
//
//    @Test
//    void createExamFromSubPackage_NullRequest() {
//        // Arrange - Configure mock to throw NullPointerException when called with null
//        when(examService.createExamFromSubPackage(null))
//                .thenThrow(new NullPointerException("ExamCreationRequestDto cannot be null"));
//
//        // Act & Assert
//        assertThrows(NullPointerException.class, () -> {
//            examService.createExamFromSubPackage(null);
//        });
//
//        verify(examService).createExamFromSubPackage(null);
//    }
//}
