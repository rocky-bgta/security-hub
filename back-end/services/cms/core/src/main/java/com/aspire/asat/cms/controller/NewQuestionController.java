package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.question.BulkQuestionRequestDto;
import com.aspire.asat.cms.dto.question.BulkQuestionResponseDto;
import com.aspire.asat.cms.dto.question.QuestionCountByTopicProductDto;
import com.aspire.asat.cms.dto.question.QuestionRequestDto;
import com.aspire.asat.cms.dto.question.QuestionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "New Question Management", description = "APIs for managing questions using the new question model")
@RequestMapping(value = WebApiUrlConstants.API_URI_ROOT + "/new-questions", produces = "application/json")
public interface NewQuestionController {

    @PostMapping
    @Operation(summary = "Create multiple questions in bulk", description = "Create multiple questions with product and topic association")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Questions created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data")
    })
    ResponseEntity<ApiResponseDto<BulkQuestionResponseDto>> createBulkQuestions(@Valid @RequestBody BulkQuestionRequestDto bulkQuestionRequestDto);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get question by ID", description = "Retrieve a specific question by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
    ResponseEntity<ApiResponseDto<QuestionResponseDto>> getQuestionById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all questions", description = "Retrieve all questions for a specific topic with optional search, pagination, and sorting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - topicId is required")
    })
    ResponseEntity<ApiResponseDto<AllResponseDto<List<QuestionResponseDto>>>> getAllQuestions(
            @RequestParam(value = "topicId") String topicId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update question by ID", description = "Update an existing question by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question updated successfully"),
            @ApiResponse(responseCode = "404", description = "Question not found"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid input data")
    })
    ResponseEntity<ApiResponseDto<QuestionResponseDto>> updateQuestionById(
            @PathVariable("id") String id,
            @Valid @RequestBody QuestionRequestDto questionRequestDto);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete question by ID", description = "Delete a question by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
    ResponseEntity<ApiResponseDto<String>> deleteQuestionById(@PathVariable("id") String id);

    @GetMapping("/count/by-topic-product")
    @Operation(summary = "Get question count by topic and product", description = "Get question count grouped by topic and product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question count by topic and product retrieved successfully")
    })
    ResponseEntity<ApiResponseDto<List<QuestionCountByTopicProductDto>>> getQuestionCountByTopicAndProduct();

    @PostMapping("/import-csv")
    @Operation(
        summary = "Import questions from CSV file", 
        description = "Parses CSV file and returns parsed questions without saving to database. " +
                      "Frontend should use the returned questions to call bulk create API. " +
                      "CSV format: questionText, questionType, option1Text, option1IsCorrect, option2Text, option2IsCorrect, ... (up to 6 options). " +
                      "topicId must be provided as query parameter and will be applied to all questions in the CSV.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV parsed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid CSV format, file, or missing topicId"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<com.aspire.asat.cms.dto.question.CsvImportResponseDto>> importQuestionsFromCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("topicId") String topicId);

}
