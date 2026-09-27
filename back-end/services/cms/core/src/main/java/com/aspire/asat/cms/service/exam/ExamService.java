package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.dto.client.responseDto.ExamSubmissionResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.QuestionAssessmentResponseDTO;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.exam.ExamCreationRequestDto;
import com.aspire.asat.cms.dto.exam.ExamCreationResponseDto;
import com.aspire.asat.cms.dto.exam.ExamProgressResponseDto;
import com.aspire.asat.cms.dto.exam.FinalExamSubmissionDto;
import com.aspire.asat.cms.dto.exam.IndividualQuestionSubmissionDto;
import com.aspire.asat.cms.dto.exam.QuestionNavigationResponseDto;
import com.aspire.asat.cms.dto.exam.TopicListResponseDto;

public interface ExamService {
    /**
     * Creates an exam based on a subpackage and its associated topics
     * @param requestDTO The exam creation request
     * @return ExamCreationResponseDto containing created exam details
     */
    ExamCreationResponseDto createExamFromSubPackage(ExamCreationRequestDto requestDTO);

    /**
     * Get topics of a sub package by sub package ID
     * @param subPackageId The sub package ID
     * @return TopicListResponseDto containing topics and their details
     */
    TopicListResponseDto getTopicsBySubPackageId(String subPackageId);

    /**
     * Get exam settings for a client admin, returns default if not found
     * @param clientAdminId The client admin ID
     * @return ExamSettingsDto containing exam settings
     */
    ExamSettingsDto getExamSettingsForClient(String clientAdminId);

    /**
     * Submit individual question answer and track it in exam_attempts table
     * @param requestDTO The individual question submission request
     * @return QuestionAssessmentResponseDTO containing validation result
     */
    QuestionAssessmentResponseDTO submitIndividualQuestion(IndividualQuestionSubmissionDto requestDTO);

    /**
     * Submit final exam and calculate result from exam_attempts table
     * @param requestDTO The final exam submission request
     * @return ExamSubmissionResponseDTO containing exam results
     */
    ExamSubmissionResponseDTO submitFinalExam(FinalExamSubmissionDto requestDTO);

    /**
     * Get exam progress including questions attempted, left, and submission percentage
     * @param examId The exam ID
     * @param userId The user ID
     * @return ExamProgressResponseDto containing progress information
     */
    ExamProgressResponseDto getExamProgress(String examId, String userId);

    /**
     * Get question one by one with navigation support
     * @param examId The exam ID
     * @param questionNumber The question number (1-based)
     * @return QuestionNavigationResponseDto containing question details and navigation info
     */
    QuestionNavigationResponseDto getQuestionByNumber(String examId, int questionNumber);
}
