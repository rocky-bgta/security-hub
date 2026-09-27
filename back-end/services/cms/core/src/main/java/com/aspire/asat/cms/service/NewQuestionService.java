package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.question.BulkQuestionRequestDto;
import com.aspire.asat.cms.dto.question.BulkQuestionResponseDto;
import com.aspire.asat.cms.dto.question.QuestionCountByTopicProductDto;
import com.aspire.asat.cms.dto.question.QuestionRequestDto;
import com.aspire.asat.cms.dto.question.QuestionResponseDto;

import java.util.List;

public interface NewQuestionService {

    /**
     * Save a new question
     * @param questionRequestDto the question data to save
     * @return the saved question response DTO
     */
    QuestionResponseDto saveQuestion(QuestionRequestDto questionRequestDto);

    /**
     * Save multiple questions in bulk
     * @param bulkQuestionRequestDto the bulk question data to save
     * @return the bulk question response DTO with created questions and any errors
     */
    BulkQuestionResponseDto saveBulkQuestions(BulkQuestionRequestDto bulkQuestionRequestDto);

    /**
     * Get a question by its ID
     * @param id the question ID
     * @return the question response DTO
     */
    QuestionResponseDto getQuestionById(String id);

    /**
     * Get all questions for a specific topic with optional filtering and pagination
     * @param topicId topic ID (mandatory)
     * @param search search text for question text (optional)
     * @param status filter by status (optional)
     * @param offset pagination offset
     * @param pageSize page size
     * @param sortBy field to sort by
     * @param order sort order (asc/desc)
     * @return list of question response DTOs
     */
    List<QuestionResponseDto> getAllQuestions(String topicId, String search, String status,
                                            Integer offset, Integer pageSize, String sortBy, String order);

    /**
     * Update a question by its ID
     * @param id the question ID
     * @param questionRequestDto the updated question data
     * @return the updated question response DTO
     */
    QuestionResponseDto updateQuestionById(String id, QuestionRequestDto questionRequestDto);

    /**
     * Delete a question by its ID
     * @param id the question ID
     * @return deletion confirmation message
     */
    String deleteQuestionById(String id);

    /**
     * Get total count of questions
     * @return total count
     */
    Long getTotalQuestionCount();

    /**
     * Get total count of questions with filters
     * @param topicId topic ID (mandatory)
     * @param search search text for question text (optional)
     * @param status filter by status (optional)
     * @return total count with filters
     */
    Long getTotalQuestionCount(String topicId, String search, String status);

    /**
     * Get question count grouped by topic
     * @return list of question count DTOs grouped by topic
     */
    List<QuestionCountByTopicProductDto> getQuestionCountByTopicAndProduct();

    /**
     * Parse CSV file and return parsed questions without saving to database
     * @param file the CSV file to parse
     * @param topicId optional topic ID (if provided, will override CSV topicId column)
     * @return CSV import response DTO with parsed questions and validation errors
     */
    com.aspire.asat.cms.dto.question.CsvImportResponseDto importQuestionsFromCsv(
            org.springframework.web.multipart.MultipartFile file, String topicId);

}
