package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.config.TopicStatusConfig;
import com.aspire.asat.cms.dto.enums.TopicStatus;
import com.aspire.asat.cms.dto.question.BulkQuestionRequestDto;
import com.aspire.asat.cms.dto.question.BulkQuestionResponseDto;
import com.aspire.asat.cms.dto.question.QuestionCountByTopicProductDto;
import com.aspire.asat.cms.dto.question.QuestionRequestDto;
import com.aspire.asat.cms.dto.question.QuestionResponseDto;
import com.aspire.asat.cms.dto.question.QuestionStatus;
import com.aspire.asat.cms.dto.question.QuestionTypes;
import com.aspire.asat.cms.dto.question.QuestionOptionDto;
import com.aspire.asat.cms.dto.question.CsvImportResponseDto;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.exception.CsvParsingException;
import com.aspire.asat.cms.model.question.Question;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.NewQuestionRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.NewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewQuestionServiceImpl implements NewQuestionService {

    private final NewQuestionRepository newQuestionRepository;
    private final QuestionCustomRepository questionCustomRepository;
    private final TopicRepository topicRepository;
    private final TopicStatusConfig topicStatusConfig;

    @Override
    public QuestionResponseDto saveQuestion(QuestionRequestDto questionRequestDto) {
        log.info("Saving new question: {}", questionRequestDto);
        
        // Validate topicId exists
        Topic topic = topicRepository.findById(questionRequestDto.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + questionRequestDto.getTopicId()));
        log.info("Topic validation successful: {}", topic.getTopicName());
        
        String questionId = UUID.randomUUID().toString();
        Instant currentTime = Instant.now();
        
        Question question = Question.builder()
                .id(questionId)
                .topicId(questionRequestDto.getTopicId())
                .questionType(questionRequestDto.getQuestionType())
                .questionText(questionRequestDto.getQuestionText())
                .options(questionRequestDto.getOptions())
                .status(QuestionStatus.ACTIVE)
                .createdBy("") // TODO: Get from security context
                .updatedBy("") // TODO: Get from security context
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        Question savedQuestion = newQuestionRepository.save(question);
        log.info("Question saved successfully with ID: {}", savedQuestion.getId());
        
        // Check if topic status should be updated based on duration and question count
        updateTopicStatusIfNeeded(topic);
        
        return convertToResponseDto(savedQuestion);
    }

    @Override
    public BulkQuestionResponseDto saveBulkQuestions(BulkQuestionRequestDto bulkQuestionRequestDto) {
        log.info("Saving bulk questions: {} questions", bulkQuestionRequestDto.getQuestions().size());
        
        List<QuestionResponseDto> createdQuestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int failedCount = 0;
        
        for (int i = 0; i < bulkQuestionRequestDto.getQuestions().size(); i++) {
            QuestionRequestDto questionRequestDto = bulkQuestionRequestDto.getQuestions().get(i);
            try {
                log.info("Processing question {} of {}", i + 1, bulkQuestionRequestDto.getQuestions().size());
                QuestionResponseDto savedQuestion = saveQuestion(questionRequestDto);
                createdQuestions.add(savedQuestion);
                log.info("Successfully created question {} with ID: {}", i + 1, savedQuestion.getId());
            } catch (Exception e) {
                failedCount++;
                String errorMessage = String.format("Question %d failed: %s", i + 1, e.getMessage());
                errors.add(errorMessage);
                log.error("Failed to create question {}: {}", i + 1, e.getMessage(), e);
            }
        }
        
        log.info("Bulk question creation completed. Created: {}, Failed: {}", 
                createdQuestions.size(), failedCount);
        
        return BulkQuestionResponseDto.builder()
                .createdQuestions(createdQuestions)
                .totalCreated(createdQuestions.size())
                .failedCount(failedCount)
                .errors(errors)
                .build();
    }

    @Override
    public QuestionResponseDto getQuestionById(String id) {
        log.info("Getting question by ID: {}", id);
        
        Question question = newQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        
        return convertToResponseDto(question);
    }

    @Override
    public List<QuestionResponseDto> getAllQuestions(String topicId, String search, String status,
                                                   Integer offset, Integer pageSize, String sortBy, String order) {
        log.info("Getting all questions for topicId: {}, search: {}, status: {}, offset: {}, pageSize: {}", 
                topicId, search, status, offset, pageSize);
        
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(direction, sortBy));
        
        // Parse status if provided
        QuestionStatus questionStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                questionStatus = QuestionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}", status);
            }
        }
        
        // Use custom repository for filtering
        Page<Question> questionPage = questionCustomRepository.findQuestionsWithFilters(
                topicId, search, questionStatus, pageable);
        
        return questionPage.getContent().stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Override
    public QuestionResponseDto updateQuestionById(String id, QuestionRequestDto questionRequestDto) {
        log.info("Updating question with ID: {} and data: {}", id, questionRequestDto);
        
        Question existingQuestion = newQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        
        // Get the original topic before updating the question
        Topic originalTopic = topicRepository.findById(existingQuestion.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Original topic not found with ID: " + existingQuestion.getTopicId()));
        
        // Get the new topic if topicId is being changed
        Topic newTopic = null;
        if (!existingQuestion.getTopicId().equals(questionRequestDto.getTopicId())) {
            newTopic = topicRepository.findById(questionRequestDto.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("New topic not found with ID: " + questionRequestDto.getTopicId()));
        }
        
        existingQuestion.setTopicId(questionRequestDto.getTopicId());
        existingQuestion.setQuestionType(questionRequestDto.getQuestionType());
        existingQuestion.setQuestionText(questionRequestDto.getQuestionText());
        existingQuestion.setOptions(questionRequestDto.getOptions());
        existingQuestion.setStatus(questionRequestDto.getStatus()); // Assuming update sets status to ACTIVE
        existingQuestion.setUpdatedBy(""); // TODO: Get from security context
        existingQuestion.setUpdatedAt(Instant.now());
        
        Question updatedQuestion = newQuestionRepository.save(existingQuestion);
        log.info("Question updated successfully with ID: {}", updatedQuestion.getId());
        
        // Update topic status for both original and new topics if they differ
        updateTopicStatusIfNeeded(originalTopic);
        if (newTopic != null && !newTopic.getId().equals(originalTopic.getId())) {
            updateTopicStatusIfNeeded(newTopic);
        }
        
        return convertToResponseDto(updatedQuestion);
    }

    @Override
    public String deleteQuestionById(String id) {
        log.info("Deleting question with ID: {}", id);
        
        Question question = newQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        
        // Get the topic before deleting the question to update its status later
        Topic topic = topicRepository.findById(question.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with ID: " + question.getTopicId()));
        
        // Soft delete by setting status to DELETED
        question.setStatus(QuestionStatus.DELETED);
        question.setUpdatedBy("");
        question.setUpdatedAt(Instant.now());
        
        newQuestionRepository.save(question);
        log.info("Question deleted successfully with ID: {}", id);
        
        // Update topic status based on the new question count after deletion
        updateTopicStatusIfNeeded(topic);
        
        return "Question deleted successfully";
    }

    @Override
    public Long getTotalQuestionCount() {
        return newQuestionRepository.count();
    }


    /**
     * Get total count of questions with filters
     */
    @Override
    public Long getTotalQuestionCount(String topicId, String search, String status) {
        // Parse status if provided
        QuestionStatus questionStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                questionStatus = QuestionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}", status);
            }
        }
        
        // Use custom repository for counting
        return questionCustomRepository.countQuestionsWithFilters(topicId, search, questionStatus);
    }

    @Override
    public List<QuestionCountByTopicProductDto> getQuestionCountByTopicAndProduct() {
        log.info("Getting question count grouped by topic and product");
        return questionCustomRepository.getQuestionCountByTopicAndProduct();
    }

    private QuestionResponseDto convertToResponseDto(Question question) {
        return QuestionResponseDto.builder()
                .id(question.getId())
                .topicId(question.getTopicId())
                .questionType(question.getQuestionType())
                .questionText(question.getQuestionText())
                .options(question.getOptions())
                .status(question.getStatus())
                .createdBy(question.getCreatedBy())
                .updatedBy(question.getUpdatedBy())
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .build();
    }

    /**
     * Updates topic status based on duration and question count criteria using TopicStatusConfig.
     * This method uses the centralized configuration for determining topic status.
     */
    private void updateTopicStatusIfNeeded(Topic topic) {
        Integer durationMinutes = topic.getDurationMinutes();
        if (durationMinutes == null) {
            log.info("Topic {} has no duration set, skipping status update", topic.getId());
            return;
        }

        // Count active questions for this topic
        long questionCount = questionCustomRepository.countQuestionsWithFilters(
                topic.getId(), 
                null, // search - null for no text search
                QuestionStatus.ACTIVE
        );

        log.info("Topic {} has {} active questions, duration: {} minutes", 
                topic.getId(), questionCount, durationMinutes);

        // Use the centralized config to determine the appropriate status
        TopicStatus newStatus = topicStatusConfig.determineTopicStatus(durationMinutes, questionCount);
        
        // Log the criteria being applied
        String criteriaDescription = topicStatusConfig.getCriteriaDescription(durationMinutes);
        log.info("Applying criteria: {}", criteriaDescription);

        // Update topic status if it changed
        if (!newStatus.equals(topic.getStatus())) {
            TopicStatus oldStatus = topic.getStatus();
            topic.setStatus(newStatus);
            topic.setUpdatedAt(Instant.now());
            topicRepository.save(topic);
            log.info("Topic {} status updated from {} to {} based on duration and question count", 
                    topic.getId(), oldStatus, newStatus);
        } else {
            log.info("Topic {} status remains {} - no change needed", topic.getId(), topic.getStatus());
        }
    }

    @Override
    public CsvImportResponseDto importQuestionsFromCsv(MultipartFile file, String topicId) {
        log.info("Starting CSV import for questions. File: {}, topicId: {}", 
                file != null ? file.getOriginalFilename() : "null", topicId);

        // Validate topicId is provided (required)
        if (topicId == null || topicId.trim().isEmpty()) {
            throw new CsvParsingException("topicId is required and cannot be empty");
        }

        // Validate topic exists
        if (!topicRepository.existsById(topicId.trim())) {
            throw new CsvParsingException("Topic with ID '" + topicId.trim() + "' does not exist");
        }

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new CsvParsingException("CSV file is empty or null");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new CsvParsingException("File must be a CSV file (.csv extension required)");
        }

        List<QuestionRequestDto> parsedQuestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int failedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new CsvParsingException("CSV file is empty or missing header row");
            }

            // Validate header
            String[] headers = parseCsvLine(headerLine);
            validateCsvHeaders(headers);

            // Read data rows
            String line;
            int rowNumber = 1; // Start from 1 (header is row 0)
            
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                totalRows++;
                
                if (line.trim().isEmpty()) {
                    log.warn("Skipping empty row at line {}", rowNumber);
                    continue;
                }

                try {
                    QuestionRequestDto question = parseQuestionFromCsvRow(line, headers, topicId, rowNumber);
                    parsedQuestions.add(question);
                    log.debug("Successfully parsed question at row {}", rowNumber);
                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = String.format("Row %d: %s", rowNumber, e.getMessage());
                    errors.add(errorMsg);
                    log.error("Failed to parse question at row {}: {}", rowNumber, e.getMessage());
                }
            }

            log.info("CSV import completed. Total rows: {}, Parsed: {}, Failed: {}", 
                    totalRows, parsedQuestions.size(), failedCount);

        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            throw new CsvParsingException("Failed to read CSV file: " + e.getMessage(), e);
        } catch (CsvParsingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during CSV parsing", e);
            throw new CsvParsingException("Unexpected error during CSV parsing: " + e.getMessage(), e);
        }

        return CsvImportResponseDto.builder()
                .questions(parsedQuestions)
                .totalParsed(parsedQuestions.size())
                .failedCount(failedCount)
                .errors(errors)
                .build();
    }

    /**
     * Validates CSV headers match expected format
     * topicId is always provided via query parameter, so it's not required in CSV
     */
    private void validateCsvHeaders(String[] headers) {
        if (headers.length < 2) {
            throw new CsvParsingException(
                "Invalid CSV format: Expected at least 2 columns (questionText, questionType), got " + headers.length);
        }

        // Check for required headers (case-insensitive)
        String headerLine = String.join(",", headers).toLowerCase();
        if (!headerLine.contains("questiontext") || !headerLine.contains("questiontype")) {
            throw new CsvParsingException(
                "Invalid CSV format: Missing required headers. Expected: questionText, questionType");
        }
    }

    /**
     * Parses a CSV line handling quoted fields
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add last field
        fields.add(currentField.toString().trim());
        
        return fields.toArray(new String[0]);
    }

    /**
     * Parses a question from a CSV row
     * topicId is always provided via query parameter
     */
    private QuestionRequestDto parseQuestionFromCsvRow(String line, String[] headers, 
                                                       String topicId, int rowNumber) {
        String[] fields = parseCsvLine(line);
        
        // Minimum required: questionText and questionType (topicId comes from query param)
        if (fields.length < 2) {
            throw new CsvParsingException(
                String.format("Invalid row format: Expected at least 2 columns (questionText, questionType), got %d", fields.length));
        }

        // Find header indices (case-insensitive)
        int questionTextIndex = findHeaderIndex(headers, "questionText");
        int questionTypeIndex = findHeaderIndex(headers, "questionType");

        if (questionTextIndex == -1 || questionTypeIndex == -1) {
            throw new CsvParsingException("Missing required headers in CSV: questionText and questionType are required");
        }

        // Use topicId from query parameter (already validated at service level)
        String rowTopicId = topicId.trim();

        // Extract question text
        String questionText = questionTextIndex < fields.length ? fields[questionTextIndex].trim() : "";
        if (questionText.isEmpty()) {
            throw new CsvParsingException("questionText is required but missing or empty");
        }

        // Extract and validate question type
        String questionTypeStr = questionTypeIndex < fields.length 
            ? fields[questionTypeIndex].trim().toUpperCase() : "";
        if (questionTypeStr.isEmpty()) {
            throw new CsvParsingException("questionType is required but missing or empty");
        }

        QuestionTypes questionType;
        try {
            questionType = QuestionTypes.valueOf(questionTypeStr);
        } catch (IllegalArgumentException e) {
            throw new CsvParsingException(
                String.format("Invalid questionType '%s'. Valid values: MULTIPLE_CHOICE, SINGLE_CHOICE, TRUE_FALSE, YES_NO", 
                    questionTypeStr));
        }

        // Parse options (up to 6 options supported)
        List<QuestionOptionDto> options = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            int optionTextIndex = findHeaderIndex(headers, "option" + i + "Text");
            int optionCorrectIndex = findHeaderIndex(headers, "option" + i + "IsCorrect");

            if (optionTextIndex == -1 || optionCorrectIndex == -1) {
                continue; // This option column doesn't exist
            }

            if (optionTextIndex >= fields.length || optionCorrectIndex >= fields.length) {
                continue; // Row doesn't have enough columns
            }

            String optionText = fields[optionTextIndex].trim();
            String isCorrectStr = fields[optionCorrectIndex].trim();

            // Skip empty options
            if (optionText.isEmpty() && isCorrectStr.isEmpty()) {
                continue;
            }

            if (optionText.isEmpty()) {
                throw new CsvParsingException(
                    String.format("Option %d text is empty but isCorrect value is provided", i));
            }

            // Parse isCorrect boolean
            Boolean isCorrect;
            if (isCorrectStr.isEmpty()) {
                isCorrect = false; // Default to false if not specified
            } else {
                String lowerIsCorrect = isCorrectStr.toLowerCase();
                if (lowerIsCorrect.equals("true") || lowerIsCorrect.equals("1") || lowerIsCorrect.equals("yes")) {
                    isCorrect = true;
                } else if (lowerIsCorrect.equals("false") || lowerIsCorrect.equals("0") || lowerIsCorrect.equals("no")) {
                    isCorrect = false;
                } else {
                    throw new CsvParsingException(
                        String.format("Invalid isCorrect value '%s' for option %d. Expected: true/false, 1/0, yes/no", 
                            isCorrectStr, i));
                }
            }

            options.add(QuestionOptionDto.builder()
                    .optionText(optionText)
                    .isCorrect(isCorrect)
                    .build());
        }

        // Validate options based on question type
        validateOptionsForQuestionType(questionType, options, rowNumber);

        // Build and return question DTO
        return QuestionRequestDto.builder()
                .topicId(rowTopicId)
                .questionText(questionText)
                .questionType(questionType)
                .options(options)
                .build();
    }

    /**
     * Finds the index of a header (case-insensitive)
     */
    private int findHeaderIndex(String[] headers, String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(lowerHeaderName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Validates options based on question type
     */
    private void validateOptionsForQuestionType(QuestionTypes questionType, List<QuestionOptionDto> options, int rowNumber) {
        if (options.isEmpty()) {
            throw new CsvParsingException("At least one option is required for a question");
        }

        // Count correct answers
        long correctCount = options.stream()
                .filter(QuestionOptionDto::getIsCorrect)
                .count();

        switch (questionType) {
            case SINGLE_CHOICE:
            case TRUE_FALSE:
            case YES_NO:
                if (correctCount != 1) {
                    throw new CsvParsingException(
                        String.format("Question type '%s' requires exactly one correct answer, found %d", 
                            questionType, correctCount));
                }
                break;
            case MULTIPLE_CHOICE:
                if (correctCount < 1) {
                    throw new CsvParsingException(
                        String.format("Question type '%s' requires at least one correct answer, found %d", 
                            questionType, correctCount));
                }
                break;
        }

        // Validate TRUE_FALSE and YES_NO have exactly 2 options
        if (questionType == QuestionTypes.TRUE_FALSE || questionType == QuestionTypes.YES_NO) {
            if (options.size() != 2) {
                throw new CsvParsingException(
                    String.format("Question type '%s' requires exactly 2 options, found %d", 
                        questionType, options.size()));
            }
        }
    }
}
