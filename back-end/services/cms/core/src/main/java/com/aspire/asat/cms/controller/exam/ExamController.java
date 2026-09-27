package com.aspire.asat.cms.controller.exam;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Exam Management API", description = "Endpoints for exam validation and submission")
@RequestMapping(value = WebApiUrlConstants.CLIENT_API, produces = "application/json")
public interface ExamController {
    @Operation(summary = "Create exam from sub-package", description = "Creates an exam based on sub-package and its associated topics")
    @PostMapping("/exam/create")
    ResponseEntity<ApiResponseDto<ExamCreationResponseDto>> createExamFromSubPackage(
            @RequestBody @Valid ExamCreationRequestDto requestDTO);

    @Operation(summary = "Get topics by sub-package ID", description = "Get topics of a sub package by sub package ID")
    @GetMapping("/exam/sub-package/{subPackageId}/topics")
    ResponseEntity<ApiResponseDto<TopicListResponseDto>> getTopicsBySubPackageId(@PathVariable String subPackageId);

    @Operation(summary = "Get exam settings for client", description = "Get exam settings for a client admin, returns default if not found")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings retrieved successfully")
    })
    @GetMapping("/exam/settings/client/{clientAdminId}")
    ResponseEntity<ApiResponseDto<ExamSettingsDto>> getExamSettingsForClient(@PathVariable String clientAdminId);

    @Operation(summary = "Submit individual question", description = "Submit individual question answer and track it in exam_attempts table")
    @PostMapping("/exam/question/submit")
    ResponseEntity<ApiResponseDto<QuestionAssessmentResponseDTO>> submitIndividualQuestion(
            @RequestBody @Valid IndividualQuestionSubmissionDto requestDTO);

    @Operation(summary = "Submit final exam", description = "Submit final exam and calculate result from exam_attempts table")
    @PostMapping("/exam/submit-final")
    ResponseEntity<ApiResponseDto<ExamSubmissionResponseDTO>> submitFinalExam(
            @RequestBody @Valid FinalExamSubmissionDto requestDTO);

    @Operation(summary = "Get exam progress", description = "Get exam progress including questions attempted, left, and submission percentage")
    @GetMapping("/exam/progress/{examId}/user/{userId}")
    ResponseEntity<ApiResponseDto<ExamProgressResponseDto>> getExamProgress(
            @PathVariable String examId, 
            @PathVariable String userId);

    @Operation(summary = "Get question by number", description = "Get question one by one with navigation support for next/previous")
    @GetMapping("/exam/{examId}/question/{questionNumber}")
    ResponseEntity<ApiResponseDto<QuestionNavigationResponseDto>> getQuestionByNumber(
            @PathVariable String examId, 
            @PathVariable int questionNumber);
}
