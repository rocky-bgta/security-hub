package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.NewQuestionController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.question.BulkQuestionRequestDto;
import com.aspire.asat.cms.dto.question.BulkQuestionResponseDto;
import com.aspire.asat.cms.dto.question.QuestionCountByTopicProductDto;
import com.aspire.asat.cms.dto.question.QuestionRequestDto;
import com.aspire.asat.cms.dto.question.QuestionResponseDto;
import com.aspire.asat.cms.dto.question.CsvImportResponseDto;
import com.aspire.asat.cms.exception.CsvParsingException;
import com.aspire.asat.cms.service.NewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NewQuestionControllerImpl implements NewQuestionController {

    private final NewQuestionService newQuestionService;

    @Override
    public ResponseEntity<ApiResponseDto<BulkQuestionResponseDto>> createBulkQuestions(BulkQuestionRequestDto bulkQuestionRequestDto) {
        log.info("Request received to create bulk questions: {} questions", 
                bulkQuestionRequestDto.getQuestions().size());
        
        BulkQuestionResponseDto bulkResponse = newQuestionService.saveBulkQuestions(bulkQuestionRequestDto);
        
        String message = String.format("Bulk question creation completed. Created: %d, Failed: %d", 
                bulkResponse.getTotalCreated(), bulkResponse.getFailedCount());
        
        ApiResponseDto<BulkQuestionResponseDto> response = new ApiResponseDto<>(
                message, 
                HttpStatus.CREATED.value(), 
                bulkResponse
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<QuestionResponseDto>> getQuestionById(String id) {
        log.info("Request received to get question by ID: {}", id);
        
        QuestionResponseDto question = newQuestionService.getQuestionById(id);
        ApiResponseDto<QuestionResponseDto> response = new ApiResponseDto<>(
                "Question retrieved successfully", 
                HttpStatus.OK.value(), 
                question
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<QuestionResponseDto>>>> getAllQuestions(
            String topicId, String search, String status, 
            Integer offset, Integer pageSize, String sortBy, String order) {
        
        log.info("Request received to get all questions for topicId: {}, search: {}, status: {}, offset: {}, pageSize: {}", 
                topicId, search, status, offset, pageSize);
        
        // Validate mandatory parameters
        if (topicId == null || topicId.trim().isEmpty()) {
            log.error("TopicId is mandatory for getting questions");
            ApiResponseDto<AllResponseDto<List<QuestionResponseDto>>> errorResponse = new ApiResponseDto<>(
                    "TopicId is mandatory for getting questions", 
                    HttpStatus.BAD_REQUEST.value(), 
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        
        List<QuestionResponseDto> questions = newQuestionService.getAllQuestions(
                topicId, search, status, offset, pageSize, sortBy, order
        );
        
        AllResponseDto<List<QuestionResponseDto>> allResponseDto = new AllResponseDto<>(
                offset, 
                pageSize, 
                newQuestionService.getTotalQuestionCount(topicId, search, status), 
                questions
        );
        
        ApiResponseDto<AllResponseDto<List<QuestionResponseDto>>> response = new ApiResponseDto<>(
                "Questions retrieved successfully", 
                HttpStatus.OK.value(), 
                allResponseDto
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<QuestionResponseDto>> updateQuestionById(String id, QuestionRequestDto questionRequestDto) {
        log.info("Request received to update question with ID: {} and data: {}", id, questionRequestDto);
        
        QuestionResponseDto updatedQuestion = newQuestionService.updateQuestionById(id, questionRequestDto);
        ApiResponseDto<QuestionResponseDto> response = new ApiResponseDto<>(
                "Question updated successfully", 
                HttpStatus.OK.value(), 
                updatedQuestion
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteQuestionById(String id) {
        log.info("Request received to delete question with ID: {}", id);
        
        String result = newQuestionService.deleteQuestionById(id);
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Question deleted successfully", 
                HttpStatus.OK.value(), 
                result
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<QuestionCountByTopicProductDto>>> getQuestionCountByTopicAndProduct() {
        log.info("Request received to get question count by topic and product");
        
        List<QuestionCountByTopicProductDto> counts = newQuestionService.getQuestionCountByTopicAndProduct();
        
        ApiResponseDto<List<QuestionCountByTopicProductDto>> response = new ApiResponseDto<>(
                "Question count by topic and product retrieved successfully", 
                HttpStatus.OK.value(), 
                counts
        );
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CsvImportResponseDto>> importQuestionsFromCsv(
            MultipartFile file, String topicId) {
        log.info("Request received to import questions from CSV. File: {}, topicId: {}", 
                file != null ? file.getOriginalFilename() : "null", topicId);
        
        // Validate topicId is provided
        if (topicId == null || topicId.trim().isEmpty()) {
            log.error("TopicId is required for CSV import");
            ApiResponseDto<CsvImportResponseDto> errorResponse = new ApiResponseDto<>(
                    "TopicId is required for CSV import", 
                    HttpStatus.BAD_REQUEST.value(), 
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        
        // Validate file is provided
        if (file == null || file.isEmpty()) {
            log.error("CSV file is required for import");
            ApiResponseDto<CsvImportResponseDto> errorResponse = new ApiResponseDto<>(
                    "CSV file is required for import", 
                    HttpStatus.BAD_REQUEST.value(), 
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        
        try {
            CsvImportResponseDto importResponse = newQuestionService.importQuestionsFromCsv(file, topicId);
            
            String message = String.format("CSV import completed. Parsed: %d questions, Failed: %d rows", 
                    importResponse.getTotalParsed(), importResponse.getFailedCount());
            
            ApiResponseDto<CsvImportResponseDto> response = new ApiResponseDto<>(
                    message, 
                    HttpStatus.OK.value(), 
                    importResponse
            );
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (CsvParsingException e) {
            log.error("CSV parsing error: {}", e.getMessage());
            ApiResponseDto<CsvImportResponseDto> errorResponse = new ApiResponseDto<>(
                    "CSV parsing failed: " + e.getMessage(), 
                    HttpStatus.BAD_REQUEST.value(), 
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            
        } catch (Exception e) {
            log.error("Unexpected error during CSV import", e);
            ApiResponseDto<CsvImportResponseDto> errorResponse = new ApiResponseDto<>(
                    "Unexpected error during CSV import: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    null
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
