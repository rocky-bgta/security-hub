package com.aspire.asat.cms.controller.exam;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
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
import com.aspire.asat.cms.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ExamControllerImpl implements ExamController {
    private final ExamService examService;


    @Override
    public ResponseEntity<ApiResponseDto<ExamCreationResponseDto>> createExamFromSubPackage(ExamCreationRequestDto requestDTO) {
        ExamCreationResponseDto response = examService.createExamFromSubPackage(requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Exam created successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TopicListResponseDto>> getTopicsBySubPackageId(String subPackageId) {
        log.info("Getting topics for sub-package: {}", subPackageId);
        TopicListResponseDto response = examService.getTopicsBySubPackageId(subPackageId);
        return ResponseEntity.ok(new ApiResponseDto<>("Topics retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamSettingsDto>> getExamSettingsForClient(String clientAdminId) {
        log.info("Getting exam settings for client admin: {}", clientAdminId);
        ExamSettingsDto response = examService.getExamSettingsForClient(clientAdminId);
        return ResponseEntity.ok(new ApiResponseDto<>("Exam settings retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<QuestionAssessmentResponseDTO>> submitIndividualQuestion(IndividualQuestionSubmissionDto requestDTO) {
        log.info("Submitting individual question for exam: {}, user: {}, question: {}", 
                requestDTO.getExamId(), requestDTO.getUserId(), requestDTO.getQuestionId());
        QuestionAssessmentResponseDTO response = examService.submitIndividualQuestion(requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Individual question submitted successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamSubmissionResponseDTO>> submitFinalExam(FinalExamSubmissionDto requestDTO) {
        log.info("Submitting final exam for exam: {}, user: {}", requestDTO.getExamId(), requestDTO.getUserId());
        ExamSubmissionResponseDTO response = examService.submitFinalExam(requestDTO);
        return ResponseEntity.ok(new ApiResponseDto<>("Final exam submitted successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ExamProgressResponseDto>> getExamProgress(String examId, String userId) {
        log.info("Getting exam progress for exam: {}, user: {}", examId, userId);
        ExamProgressResponseDto response = examService.getExamProgress(examId, userId);
        return ResponseEntity.ok(new ApiResponseDto<>("Exam progress retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<QuestionNavigationResponseDto>> getQuestionByNumber(String examId, int questionNumber) {
        log.info("Getting question {} for exam: {}", questionNumber, examId);
        QuestionNavigationResponseDto response = examService.getQuestionByNumber(examId, questionNumber);
        return ResponseEntity.ok(new ApiResponseDto<>("Question retrieved successfully", 200, response));
    }
}
