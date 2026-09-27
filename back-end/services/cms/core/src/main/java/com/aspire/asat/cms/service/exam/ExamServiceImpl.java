package com.aspire.asat.cms.service.exam;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.client.responseDto.ExamSubmissionResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.QuestionAssessmentResponseDTO;
import com.aspire.asat.cms.dto.clientExamSettings.ExamSettingsDto;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.dto.enums.DistributionStrategy;
import com.aspire.asat.cms.dto.enums.ExamStatus;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.dto.exam.CertificateLinksDTO;
import com.aspire.asat.cms.dto.exam.ClientAdminCountryAndMsp;
import com.aspire.asat.cms.dto.exam.ExamCreationRequestDto;
import com.aspire.asat.cms.dto.exam.ExamCreationResponseDto;
import com.aspire.asat.cms.dto.exam.ExamProgressResponseDto;
import com.aspire.asat.cms.dto.exam.ExamQuestion;
import com.aspire.asat.cms.dto.exam.ExamSubmissionAsyncPayload;
import com.aspire.asat.cms.dto.exam.FinalExamSubmissionDto;
import com.aspire.asat.cms.dto.exam.IndividualQuestionSubmissionDto;
import com.aspire.asat.cms.dto.exam.QuestionNavigationResponseDto;
import com.aspire.asat.cms.dto.exam.TopicListResponseDto;
import com.aspire.asat.cms.dto.notification.CertificateNotificationRequest;
import com.aspire.asat.cms.dto.question.QuestionOptionDto;
import com.aspire.asat.cms.dto.question.QuestionStatus;
import com.aspire.asat.cms.exception.ExamSettingsNotFoundException;
import com.aspire.asat.cms.exception.ResourceNotAllowed;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.mapper.ExamMapper;
import com.aspire.asat.cms.model.ExamQuestionEntity;
import com.aspire.asat.cms.model.ExamQuestionsAttempt;
import com.aspire.asat.cms.model.Exams;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.model.question.Question;
import com.aspire.asat.cms.model.topic.Topic;
import com.aspire.asat.cms.repository.ClientDashboardRepository;
import com.aspire.asat.cms.repository.ExamAttemptRepository;
import com.aspire.asat.cms.repository.ExamQuestionRepository;
import com.aspire.asat.cms.repository.ExamRepository;
import com.aspire.asat.cms.repository.NewQuestionRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.question.QuestionCustomRepository;
import com.aspire.asat.cms.repository.topic.TopicRepository;
import com.aspire.asat.cms.service.certificate.CertificateService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {
    private final ExamRepository examRepository;
    private final CertificateService certificateService;
    private final ExamSettingsService examSettingsService;
    private final SubPackageRepository subPackageRepository;
    private final TopicRepository topicRepository;
    private final QuestionCustomRepository questionCustomRepository;
    private final NewQuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamMapper examMapper;
    private final UserSubPackageRepository userSubPackageRepository;
    private final UserCertificateRepository userCertificateRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ClientDashboardRepository clientDashboardRepository;
    private final CmsNotificationClient notificationClient;
    private final ClientAdminServiceClient clientAdminServiceClient;
    private final ExamSubmissionAsyncProcessor examSubmissionAsyncProcessor;

    private static final Integer CERTIFICATE_VALIDITY_DAYS = 365;


    private ExamSettingsDto getExamSettings(CurrentUserContext userContext) {
        return examSettingsService.getExamSettingForClient(userContext.getClientAdminId());
    }

    private UserSubPackage fetchUserPackage(String userId, String packageId) {
        return userSubPackageRepository
                .findByUserIdAndSubPackageId(userId, packageId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User package not found for userId: " + userId + " and packageId: " + packageId));

    }

    private Exams fetchExam(String examId) {
        return examRepository.findByExamId(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found for examId: " + examId));
    }

    private UserSubPackage handleCertificate(UserSubPackage userSubPackage, Exams exam, boolean passed) {
        if (passed) {
            userSubPackage = certificateGenerate(userSubPackage, exam);
        } else {
            userSubPackage.setCertificateLink(null);
            userSubPackage.setImageCertificateLink(null);
            exam.setCertificateLink(null);
            exam.setImageCertificateLink(null);
        }
        return userSubPackage;
    }

    private String getPackageName(String subPackageId) {
        return subPackageRepository.findById(subPackageId)
                .map(SubPackage::getName)
                .orElse("N/A");
    }

    private void updateExamAfterEvaluation(Exams exam, int correctCount, int incorrectCount, double percentage, boolean passed) {
        exam.setExamCompleted(true);
        exam.setExamScore(percentage);
        exam.setExamPassed(passed);
        exam.setCorrectAnswers(correctCount);
        exam.setIncorrectAnswers(incorrectCount);
        exam.setExamCompletedAt(Instant.now());
        exam.setExamAttempts(Math.max(0, exam.getExamAttempts()) + 1);
    }

    private ExamSubmissionResponseDTO buildExamSubmissionResponse(
            Exams exam,
            String packageName,
            int totalQuestions,
            int correctCount,
            int incorrectCount,
            double percentage,
            boolean passed,
            Integer passingScore
    ) {
        return ExamSubmissionResponseDTO.builder()
                .packageId(exam.getSubPackageId())
                .packageName(packageName)
                .examCompletedAt(exam.getExamCompletedAt())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctCount)
                .incorrectAnswers(incorrectCount)
                .percentageScore(percentage)
                .status(passed ? ExamStatus.PASSED.name() : ExamStatus.FAILED.name())
                .certificateLink(exam.getCertificateLink())
                .passingScore(passingScore)
                .build();
    }

    private UserSubPackage certificateGenerate(UserSubPackage userPackage, Exams exam) {
        // TODO: Move certificate generation to an async job and stream to storage to avoid big heap spikes
        try {

            String certificateId = getCertificateId();
            CertificateLinksDTO links = certificateService.generateCertificate(
                    userPackage.getUserId(), userPackage.getSubPackageName(), userPackage.getSubPackageId(), certificateId, userPackage.getClientAdminId(), userPackage.getIsTrial());
            if (links != null && links.getPdfLink() != null && !links.getPdfLink().isEmpty()) {
                userPackage.setCertificateLink(links.getPdfLink());
                userPackage.setImageCertificateLink(links.getImageLink());
                userPackage.setStatus(SubPackageStatus.COMPLETED.name());

                exam.setCertificateLink(links.getPdfLink());
                exam.setImageCertificateLink(links.getImageLink());

                exam = examRepository.save(exam);
                saveCertificateDetails(userPackage, links, certificateId, exam.getExamId());
            } else {
                log.warn("Certificate generation returned empty links for userId={} packageId={}",
                        userPackage.getUserId(), userPackage.getSubPackageId());
            }

            return userPackage;
        } catch (Exception e) {
            log.error("Certificate generation failed for userId={} packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId(), e);
        }
        return null;
    }

    private void saveCertificateDetails(UserSubPackage userPackage, CertificateLinksDTO links, String certificateId, String examId) {
        try {
            // Get current user context for additional details
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();

            UserCertificate userCertificate = UserCertificate.builder()
                    .userId(userPackage.getUserId())
                    .userSubPackageId(userPackage.getId())
                    .clientAdminId(userPackage.getClientAdminId())
                    .username(currentUserContext.getEmail())
                    .fullName(currentUserContext.getFullName())
                    .productName(links.getProductName())
                    .certificateUrl(links.getPdfLink())
                    .expiryDate(Instant.now().plus(CERTIFICATE_VALIDITY_DAYS, ChronoUnit.DAYS))
                    .createdAt(Instant.now())
                    .subPackageId(userPackage.getSubPackageId())

                    .status(CertificateStatus.VALID.name())

                    .certificateLink(links.getPdfLink())
                    .imageCertificateLink(links.getImageLink())
                    .certificateId(certificateId)
                    .examId(examId)
                    .build();

            userCertificateRepository.save(userCertificate);
            log.info("Certificate details saved to user_certificates table for userId={}, packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId());
        } catch (Exception e) {
            log.error("Failed to save certificate details to user_certificates table for userId={}, packageId={}",
                    userPackage.getUserId(), userPackage.getSubPackageId(), e);
        }
    }

    private String getCertificateId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = new Random().nextInt(90000) + 10000;
        return String.format("CRT-%s-%05d", date, seq);
    }

    @Override
    public ExamCreationResponseDto createExamFromSubPackage(ExamCreationRequestDto requestDTO) {
        if (requestDTO == null) {
            throw new NullPointerException("ExamCreationRequestDto cannot be null");
        }

        log.info("Creating exam for sub-package: {}", requestDTO.getSubPackageId());

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();

        preExamValidation(requestDTO);

        try {
            // 1. Validate input parameters
            validateExamCreationRequest(requestDTO);

            // 2. Get exam settings for the client
            ExamSettingsDto examSettings = getExamSettings(userContext);
            log.info("Using exam settings for client: {} - Total Questions: {}, Distribution: {}",
                    requestDTO.getClientId(), examSettings.getTotalQuestions(), examSettings.getDistributionStrategy());

            // 3. Validate sub-package exists
            SubPackage subPackage = subPackageRepository.findById(requestDTO.getSubPackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sub-package not found: " + requestDTO.getSubPackageId()));


            userSubPackageRepository.findByUserIdAndSubPackageId(requestDTO.getUserId(), requestDTO.getSubPackageId())
                    .orElseThrow(() -> new ResourceNotFoundException("User is not assigned to the specified sub-package"));

            // 4. Get topics associated with the subpackage
            List<String> topicIds = subPackage.getTopicId();
            if (topicIds == null || topicIds.isEmpty()) {
                throw new IllegalArgumentException("No topics found for sub-package: " + requestDTO.getSubPackageId());
            }

            List<Topic> topics = topicRepository.findActiveTopicsByIds(topicIds);
            if (topics.isEmpty()) {
                throw new IllegalArgumentException("No active topics found for sub-package: " + requestDTO.getSubPackageId());
            }

            // 5. Get questions for each topic
            Map<String, List<Question>> questionsByTopic = getQuestionsByTopics(topicIds);

            // 6. Calculate question distribution using exam settings
            List<Integer> questionCountsPerTopic = calculateQuestionDistributionFromSettings(
                    examSettings, topics, questionsByTopic);

            // 7. Select questions for the exam
            List<ExamQuestion> examQuestions = selectQuestionsForExam(topics, questionsByTopic, questionCountsPerTopic);
            ClientAdminCountryAndMsp clientAdminCountryAndMsp = clientAdminServiceClient.getClientAdminCountryAndMsp(requestDTO.getClientId());

            // 8. Create and save the exam using exam settings
            Exams exam = createPackageExamFromSettings(requestDTO, subPackage, examSettings, clientAdminCountryAndMsp.getCountryId(), clientAdminCountryAndMsp.getMspId());

            Exams savedExam = examRepository.save(exam);

            // 9. Save exam questions to the new exam_questions table
            saveExamQuestionsToTable(savedExam.getExamId(), examQuestions, topics, questionsByTopic);

            // 10. Build response
            ExamCreationResponseDto response = examMapper.toExamCreationResponseDto(savedExam, subPackage, topics, questionCountsPerTopic);
            response.setCreatedAt(Instant.now());

            // 11. Update topic info with actual question counts
            updateTopicInfoWithQuestionCounts(response, questionsByTopic);

            log.info("Successfully created exam: {} with {} questions using client settings", savedExam.getExamId(), examQuestions.size());
            return response;

        } catch (ResourceNotFoundException | IllegalArgumentException ex) {
            log.warn("Exam creation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while creating exam for sub-package: {}", requestDTO.getSubPackageId(), ex);
            throw new ExamSettingsNotFoundException("Failed to create exam due to internal error", ex);
        }
    }

    private void preExamValidation(ExamCreationRequestDto requestDTO) {
        UserSubPackage userSubPackage = userSubPackageRepository.findByUserIdAndSubPackageId(requestDTO.getUserId(), requestDTO.getSubPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("User is not assigned to the specified sub-package"));

        if (!SubPackageStatus.EXAM.name().equalsIgnoreCase(userSubPackage.getStatus())) {
            throw new ResourceNotAllowed("You are not allowed to take the exam, Complete the topics first");
        }

        // Check if user has already passed the exam (any attempt) by checking Exams table
        if (examRepository.existsByUserIdAndSubPackageIdAndExamPassedTrue(requestDTO.getUserId(), requestDTO.getSubPackageId())) {
            throw new ResourceNotAllowed("You have already passed the exam for this sub-package");
        }

    }

    private Map<String, List<Question>> getQuestionsByTopics(List<String> topicIds) {
        Map<String, List<Question>> questionsByTopic = new HashMap<>();

        for (String topicId : topicIds) {
            List<Question> questions = questionCustomRepository.findAllByTopicIdAndStatus(topicId, QuestionStatus.ACTIVE);
            questionsByTopic.put(topicId, questions);
        }

        return questionsByTopic;
    }


    private List<Integer> calculateEqualDistribution(int totalQuestions, int topicCount) {
        int baseQuestionsPerTopic = totalQuestions / topicCount;
        int remainder = totalQuestions % topicCount;

        List<Integer> distribution = new ArrayList<>();
        for (int i = 0; i < topicCount; i++) {
            int questionsForTopic = baseQuestionsPerTopic + (i < remainder ? 1 : 0);
            distribution.add(questionsForTopic);
        }

        return distribution;
    }


    private List<Integer> calculateWeightedDistribution(int totalQuestions,
                                                        List<Topic> topics,
                                                        Map<String, List<Question>> questionsByTopic) {
        // Calculate total available Question
        int totalAvailableQuestions = topics.stream()
                .mapToInt(topic -> questionsByTopic.getOrDefault(topic.getId(), Collections.emptyList()).size())
                .sum();

        if (totalAvailableQuestions == 0) {
            return calculateEqualDistribution(totalQuestions, topics.size());
        }

        List<Integer> distribution = new ArrayList<>();
        int remainingQuestions = totalQuestions;

        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            int availableQuestions = questionsByTopic.getOrDefault(topic.getId(), Collections.emptyList()).size();

            int questionsForTopic;
            if (i == topics.size() - 1) {
                // The last topic gets all remaining Question
                questionsForTopic = remainingQuestions;
            } else {
                // Proportional distribution based on available Question
                questionsForTopic = (int) Math.round((double) availableQuestions * totalQuestions / totalAvailableQuestions);
                questionsForTopic = Math.min(questionsForTopic, availableQuestions);
                questionsForTopic = Math.min(questionsForTopic, remainingQuestions);
            }

            distribution.add(questionsForTopic);
            remainingQuestions -= questionsForTopic;
        }

        return distribution;
    }

    private List<ExamQuestion> selectQuestionsForExam(List<Topic> topics,
                                                      Map<String, List<Question>> questionsByTopic,
                                                      List<Integer> questionCountsPerTopic) {
        List<ExamQuestion> examQuestions = new ArrayList<>();

        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            List<Question> topicQuestions = questionsByTopic.getOrDefault(topic.getId(), Collections.emptyList());
            int questionsNeeded = questionCountsPerTopic.get(i);

            // Shuffle and select a Question
            Collections.shuffle(topicQuestions);
            List<Question> selectedQuestions = topicQuestions.stream()
                    .limit(questionsNeeded)
                    .toList();

            // Convert to ExamQuestion format
            for (Question question : selectedQuestions) {
                ExamQuestion examQuestion = new ExamQuestion();
                examQuestion.setQuestionId(question.getId());
                examQuestion.setOptions(extractOptionTexts(question));
                examQuestion.setCorrectAnswers(extractCorrectAnswers(question));
                examQuestions.add(examQuestion);
            }
        }

        return examQuestions;
    }

    private List<String> extractOptionTexts(Question question) {
        return question.getOptions().stream()
                .map(QuestionOptionDto::getOptionText)
                .toList();
    }

    private List<String> extractCorrectAnswers(Question question) {
        return question.getOptions().stream()
                .filter(option -> option.getIsCorrect() != null && option.getIsCorrect())
                .map(QuestionOptionDto::getOptionText)
                .toList();
    }


    private void updateTopicInfoWithQuestionCounts(ExamCreationResponseDto response, Map<String, List<Question>> questionsByTopic) {
        response.getTopics().forEach(topicInfo -> {
            List<Question> topicQuestions = questionsByTopic.getOrDefault(topicInfo.getTopicId(), Collections.emptyList());
            topicInfo.setTotalAvailableQuestions(topicQuestions.size());
        });
    }

    private void validateExamCreationRequest(ExamCreationRequestDto requestDTO) {
        if (requestDTO.getClientId() == null || requestDTO.getClientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Client ID is required");
        }

        if (requestDTO.getUserId() == null || requestDTO.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (requestDTO.getSubPackageId() == null || requestDTO.getSubPackageId().trim().isEmpty()) {
            throw new IllegalArgumentException("Sub-package ID is required");
        }
    }

    /**
     * Calculate question distribution using exam settings instead of request DTO
     */
    private List<Integer> calculateQuestionDistributionFromSettings(ExamSettingsDto examSettings,
                                                                    List<Topic> topics,
                                                                    Map<String, List<Question>> questionsByTopic) {
        int totalQuestions = examSettings.getTotalQuestions();
        int topicCount = topics.size();

        final var distributionStrategy = DistributionStrategy.fromString(examSettings.getDistributionStrategy());

        return switch (distributionStrategy) {
            case CUSTOM ->
                    calculateCustomDistributionFromSettings(examSettings.getCustomDistribution(), topics, totalQuestions);
            case WEIGHTED -> calculateWeightedDistribution(totalQuestions, topics, questionsByTopic);
            default -> calculateEqualDistribution(totalQuestions, topicCount);
        };
    }

    /**
     * Calculate custom distribution using exam settings
     */
    private List<Integer> calculateCustomDistributionFromSettings(List<ExamSettingsDto.TopicQuestionDistributionDto> customDistribution,
                                                                  List<Topic> topics,
                                                                  int totalQuestions) {
        if (customDistribution == null || customDistribution.isEmpty()) {
            // Fallback to equal distribution if custom distribution is not provided
            return calculateEqualDistribution(totalQuestions, topics.size());
        }

        Map<String, Integer> customMap = customDistribution.stream()
                .collect(Collectors.toMap(
                        ExamSettingsDto.TopicQuestionDistributionDto::getTopicId,
                        ExamSettingsDto.TopicQuestionDistributionDto::getQuestionCount));

        return topics.stream()
                .map(topic -> customMap.getOrDefault(topic.getId(), 0))
                .toList();
    }

    /**
     * Create PackageExam using exam settings instead of request DTO
     */
    private Exams createPackageExamFromSettings(ExamCreationRequestDto examCreateDto,
                                                SubPackage subPackage,
                                                ExamSettingsDto examSettings, String countryId, String mspId) {
        Exams exam = new Exams();
        exam.setExamId(UUID.randomUUID().toString());
        exam.setUserId(examCreateDto.getUserId());
        exam.setClientAdminId(examCreateDto.getClientId());
        exam.setSubPackageId(subPackage.getId());
        exam.setTitle(examCreateDto.getExamTitle() == null ? subPackage.getName() : examCreateDto.getExamTitle());
        exam.setExamDetails(examCreateDto.getExamDescription());
        exam.setPassingScore(examSettings.getPassingScore().doubleValue());
        exam.setTotalQuestions(examSettings.getTotalQuestions());
        exam.setCreatedAt(Instant.now());
        
        // Initialize exam result fields
        exam.setExamCompleted(false);
        exam.setExamScore(0.0);
        exam.setExamPassed(false);
        exam.setCorrectAnswers(0);
        exam.setIncorrectAnswers(0);
        exam.setExamAttempts(0);
        exam.setCountryId(countryId);
        exam.setMspId(mspId);
        
        return exam;
    }

    @Override
    public TopicListResponseDto getTopicsBySubPackageId(String subPackageId) {
        log.info("Getting topics for sub-package: {}", subPackageId);

        try {
            // 1. Validate sub-package exists
            SubPackage subPackage = subPackageRepository.findById(subPackageId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sub-package not found: " + subPackageId));

            // 2. Get topics associated with the subpackage
            List<String> topicIds = subPackage.getTopicId();
            if (topicIds == null || topicIds.isEmpty()) {
                return TopicListResponseDto.builder()
                        .subPackageId(subPackageId)
                        .subPackageName(subPackage.getName())
                        .topics(Collections.emptyList())
                        .build();
            }

            // 3. Get topic details
            List<Topic> topics = topicRepository.findActiveTopicsByIds(topicIds);

            // 4. Get question counts for each topic
            Map<String, List<Question>> questionsByTopic = getQuestionsByTopics(topicIds);

            // 5. Build response
            List<TopicListResponseDto.TopicInfo> topicInfos = topics.stream()
                    .map(topic -> {
                        List<Question> topicQuestions = questionsByTopic.getOrDefault(topic.getId(), Collections.emptyList());
                        return TopicListResponseDto.TopicInfo.builder()
                                .topicId(topic.getId())
                                .topicName(topic.getTopicName())
                                .description(topic.getDescription())
                                .totalQuestions(topicQuestions.size())
                                .build();
                    })
                    .toList();

            return TopicListResponseDto.builder()
                    .subPackageId(subPackageId)
                    .subPackageName(subPackage.getName())
                    .topics(topicInfos)
                    .build();

        } catch (ResourceNotFoundException ex) {
            log.warn("Sub-package not found: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while getting topics for sub-package: {}", subPackageId, ex);
            throw new ExamSettingsNotFoundException("Failed to get topics due to internal error", ex);
        }
    }

    @Override
    public ExamSettingsDto getExamSettingsForClient(String clientAdminId) {
        log.info("Getting exam settings for client admin: {}", clientAdminId);
        return examSettingsService.getExamSettingForClient(clientAdminId);
    }

    @Override
    public QuestionAssessmentResponseDTO submitIndividualQuestion(IndividualQuestionSubmissionDto requestDTO) {
        log.info("Submitting individual question for exam: {}, user: {}, question: {}",
                requestDTO.getExamId(), requestDTO.getUserId(), requestDTO.getQuestionId());

        try {
            // 1. Validate exam exists
            Exams exam = fetchExam(requestDTO.getExamId());

            // 2. Check if a question exists in exam using new table
            ExamQuestionEntity examQuestionEntity = examQuestionRepository.findByExamIdAndQuestionId(
                            exam.getExamId(), requestDTO.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found in exam"));

            // 3. Get the actual question details
            Question question = questionRepository.findById(examQuestionEntity.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + requestDTO.getQuestionId()));

            // 4. Validate the answer
            Set<String> correctAnswers = new HashSet<>(extractCorrectAnswers(question));
            Set<String> providedAnswers = new HashSet<>(requestDTO.getSubmittedAnswers());
            boolean isCorrect = correctAnswers.equals(providedAnswers);

            ExamQuestionsAttempt attempt = examAttemptRepository
                    .findByExamIdAndUserIdAndQuestionId(requestDTO.getExamId(), requestDTO.getUserId(), requestDTO.getQuestionId())
                    .map(existing -> {
                        existing.setSubmittedAnswers(requestDTO.getSubmittedAnswers());
                        existing.setCorrect(isCorrect);
                        existing.setSubmittedAt(Instant.now());
                        existing.setUpdatedAt(Instant.now());
                        return existing;
                    })
                    .orElseGet(() -> ExamQuestionsAttempt.builder()
                            .id(UUID.randomUUID().toString())
                            .examId(requestDTO.getExamId())
                            .userId(requestDTO.getUserId())
                            .questionId(requestDTO.getQuestionId())
                            .submittedAnswers(requestDTO.getSubmittedAnswers())
                            .isCorrect(isCorrect)
                            .submittedAt(Instant.now())
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build());

            examAttemptRepository.save(attempt);

            log.info("Individual question submitted successfully. Correct: {}", isCorrect);

            return QuestionAssessmentResponseDTO.builder()
                    .correct(isCorrect)
                    .build();

        } catch (ResourceNotFoundException ex) {
            log.warn("Individual question submission failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while submitting individual question", ex);
            throw new ExamSettingsNotFoundException("Failed to submit question due to internal error", ex);
        }
    }

    @Override
    public ExamSubmissionResponseDTO submitFinalExam(FinalExamSubmissionDto requestDTO) {
        log.info("Submitting final exam for exam: {}, user: {}", requestDTO.getExamId(), requestDTO.getUserId());

        try {
            // 1. Validate exam exists
            Exams exam = fetchExam(requestDTO.getExamId());

            // 2. Get all attempts for this exam and user
            List<ExamQuestionsAttempt> attempts = examAttemptRepository.findByExamIdAndUserId(requestDTO.getExamId(), requestDTO.getUserId());

            // 3. Calculate results from attempts
            int totalQuestions = (int) examQuestionRepository.countByExamId(requestDTO.getExamId());
            int correctCount = (int) attempts.stream().filter(ExamQuestionsAttempt::isCorrect).count();
            int incorrectCount = attempts.size() - correctCount;
            double percentage = getPercentage(totalQuestions, correctCount);

            // 4. Get exam settings to determine passing score
            ExamSettingsDto examSettings = examSettingsService.getExamSettingForClient(exam.getClientAdminId()); // You might want to get client-specific settings
            Integer passingScore = examSettings.getPassingScore();
            boolean passed = percentage >= passingScore;

            // 5. Update exam with results and save (sync)
            String packageName = getPackageName(exam.getSubPackageId());
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            exam.setFullName(currentUserContext.getFullName());
            exam.setProductName(packageName);
            updateExamAfterEvaluation(exam, correctCount, incorrectCount, percentage, passed);
            examRepository.save(exam);

            // 6. Build payload and trigger async: user package update, certificate, dashboard, notification
            ExamSubmissionAsyncPayload asyncPayload = buildAsyncPayload(requestDTO.getUserId(), exam.getExamId(), exam.getSubPackageId(), passed, exam.getClientAdminId());
            examSubmissionAsyncProcessor.processAfterSubmission(asyncPayload);

            // 7. Build response (certificateLink null when passed – available after async completes)
            ExamSubmissionResponseDTO response = buildExamSubmissionResponse(
                    exam,
                    packageName,
                    totalQuestions,
                    correctCount,
                    incorrectCount,
                    percentage,
                    passed,
                    passingScore
            );

            log.info("Final exam submitted successfully. Score: {}%, Passed: {}", percentage, passed);
            return response;

        } catch (ResourceNotFoundException ex) {
            log.warn("Final exam submission failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while submitting final exam", ex);
            throw new ExamSettingsNotFoundException("Failed to submit final exam due to internal error", ex);
        }
    }

    private double getPercentage(int totalQuestions, int correctCount) {
        return totalQuestions > 0 ? (int) Math.round((correctCount * 10000.0) / totalQuestions) / 100.00 : 0.00;
    }

    /**
     * Build payload for async processing (captures context before request thread ends).
     * Uses current user context when available; falls back to minimal payload so async still runs and does not leave flow incomplete.
     */
    private ExamSubmissionAsyncPayload buildAsyncPayload(String userId, String examId, String subPackageId, boolean passed, String clientAdminId) {
        String courseTitle = getPackageName(subPackageId);
        try {
            CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
            return ExamSubmissionAsyncPayload.builder()
                    .userId(userId)
                    .examId(examId)
                    .subPackageId(subPackageId)
                    .passed(passed)
                    .userEmail(ctx.getEmail())
                    .userFullName(ctx.getFullName())
                    .adminEmail(ctx.getClientAdminEmail())
                    .adminName(ctx.getClientAdminFullName())
                    .clientAdminId(ctx.getClientAdminId())
                    .courseTitle(courseTitle)
                    .build();
        } catch (Exception e) {
            log.warn("Could not get current user context for async payload, using minimal payload for userId={}, subPackageId={}", userId, subPackageId, e);
            return ExamSubmissionAsyncPayload.builder()
                    .userId(userId)
                    .examId(examId)
                    .subPackageId(subPackageId)
                    .passed(passed)
                    .userEmail("")
                    .userFullName("")
                    .adminEmail("")
                    .adminName("")
                    .clientAdminId(clientAdminId != null ? clientAdminId : "")
                    .courseTitle(courseTitle)
                    .build();
        }
    }

    /**
     * Send certificate issued notification to user and admin
     */
    private void sendCertificateIssuedNotification(String userId, String subPackageId) {
        try {
            log.info("Sending certificate issued notification for userId: {}, subPackageId: {}", userId, subPackageId);
            userCertificateRepository.findByUserIdAndSubPackageId(userId, subPackageId).ifPresent(cert -> {
                // Get user and admin details
                CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
                String userEmail = userContext.getEmail();
                String userName = userContext.getFullName();

                String adminEmail = userContext.getClientAdminEmail();
                String adminName = userContext.getClientAdminFullName();

                String courseTitle = cert.getProductName();
                String issueDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

                boolean notificationSent = notificationClient.sendCertificateIssuedNotification(
                        new CertificateNotificationRequest(
                                userEmail,
                                userId,
                                userName,
                                courseTitle,
                                issueDate,
                                adminEmail,
                                adminName,
                                userContext.getClientAdminId()
                        ));

                if (notificationSent) {
                    log.info("Certificate issued notification sent successfully to user: {} and admin: {}", userEmail, adminEmail);
                } else {
                    log.error("Failed to send certificate issued notification to user: {} and admin: {}", userEmail, adminEmail);
                }
            });
        } catch (Exception e) {
            log.error("Error while sending certificate issued notification for userId: {}, subPackageId: {}", userId, subPackageId, e);
        }


    }

    @Override
    public ExamProgressResponseDto getExamProgress(String examId, String userId) {
        log.info("Getting exam progress for exam: {}, user: {}", examId, userId);

        try {
            List<ExamQuestionsAttempt> attempts = examAttemptRepository.findByExamIdAndUserId(examId, userId);

            int totalQuestions = (int) examQuestionRepository.countByExamId(examId);
            int questionsAttempted = attempts.size();
            int questionsLeft = Math.max(0, totalQuestions - questionsAttempted);
            double submissionPercentage = totalQuestions > 0
                    ? (questionsAttempted * 100.0) / totalQuestions
                    : 0.0;

            int correctAnswers = (int) attempts.stream().filter(ExamQuestionsAttempt::isCorrect).count();
            int incorrectAnswers = questionsAttempted - correctAnswers;
            boolean examCompleted = questionsAttempted >= totalQuestions;

            log.info("Exam progress retrieved. Attempted: {}/{} ({}%)", questionsAttempted, totalQuestions, submissionPercentage);

            return ExamProgressResponseDto.builder()
                    .examId(examId)
                    .userId(userId)
                    .totalQuestions(totalQuestions)
                    .questionsAttempted(questionsAttempted)
                    .questionsLeft(questionsLeft)
                    .submissionPercentage(submissionPercentage)
                    .correctAnswers(correctAnswers)
                    .incorrectAnswers(incorrectAnswers)
                    .examCompleted(examCompleted)
                    .build();

        } catch (ResourceNotFoundException ex) {
            log.warn("Exam progress retrieval failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while getting exam progress", ex);
            throw new ExamSettingsNotFoundException("Failed to get exam progress due to internal error", ex);
        }
    }

    @Override
    public QuestionNavigationResponseDto getQuestionByNumber(String examId, int questionNumber) {
        log.info("Getting question {} for exam: {}", questionNumber, examId);

        try {
            // 1. Validate exam exists
            Exams exam = fetchExam(examId);

            // 2. Get exam questions from the new exam_questions table
            List<ExamQuestionEntity> examQuestions = examQuestionRepository.findByExamIdOrderByQuestionOrder(exam.getExamId());

            if (examQuestions.isEmpty()) {
                throw new ResourceNotFoundException("No questions found for exam: " + examId);
            }

            // 3. Validate question number
            if (questionNumber < 1 || questionNumber > examQuestions.size()) {
                throw new IllegalArgumentException("Question number " + questionNumber + " is out of range. Valid range: 1-" + examQuestions.size());
            }

            // 4. Get the specific question
            ExamQuestionEntity examQuestion = examQuestions.get(questionNumber - 1); // Convert to 0-based index

            // 5. Get the actual question details
            Question question = questionRepository.findById(examQuestion.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + examQuestion.getQuestionId()));


            // 7. Build navigation info
            boolean hasNext = questionNumber < examQuestions.size();
            boolean hasPrevious = questionNumber > 1;
            Integer nextQuestionNumber = hasNext ? questionNumber + 1 : null;
            Integer previousQuestionNumber = hasPrevious ? questionNumber - 1 : null;

            // 8. Build response
            QuestionNavigationResponseDto response = QuestionNavigationResponseDto.builder()
                    .examId(examId)
                    .questionId(question.getId())
                    .currentQuestionNumber(questionNumber)
                    .totalQuestions(examQuestions.size())
                    .questionText(question.getQuestionText())
                    .options(extractOptionTexts(question))
                    .questionType(question.getQuestionType())
                    .hasNext(hasNext)
                    .hasPrevious(hasPrevious)
                    .nextQuestionNumber(nextQuestionNumber)
                    .previousQuestionNumber(previousQuestionNumber)
                    .build();

            log.info("Question {} retrieved successfully for exam: {}", questionNumber, examId);
            return response;

        } catch (ResourceNotFoundException | IllegalArgumentException ex) {
            log.warn("Question navigation failed: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while getting question by number", ex);
            throw new ExamSettingsNotFoundException("Failed to get question due to internal error", ex);
        }
    }

    /**
     * Save exam questions to the new exam_questions table
     */
    private void saveExamQuestionsToTable(String examId, List<ExamQuestion> examQuestions,
                                          List<Topic> topics, Map<String, List<Question>> questionsByTopic) {
        log.info("Saving {} questions to exam_questions table for exam: {}", examQuestions.size(), examId);

        List<ExamQuestionEntity> examQuestionEntities = new ArrayList<>();
        int questionOrder = 1;

        for (ExamQuestion examQuestion : examQuestions) {
            // Find which topic this question belongs to
            String topicId = findTopicIdForQuestion(examQuestion.getQuestionId(), topics, questionsByTopic);

            ExamQuestionEntity examQuestionEntity = ExamQuestionEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .examId(examId)
                    .questionId(examQuestion.getQuestionId())
                    .questionOrder(questionOrder++)
                    .topicId(topicId)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            examQuestionEntities.add(examQuestionEntity);
        }

        examQuestionRepository.saveAll(examQuestionEntities);
        log.info("Successfully saved {} questions to exam_questions table", examQuestionEntities.size());
    }

    /**
     * Find the topic ID for a given question
     */
    private String findTopicIdForQuestion(String questionId, List<Topic> topics, Map<String, List<Question>> questionsByTopic) {
        for (Topic topic : topics) {
            List<Question> topicQuestions = questionsByTopic.getOrDefault(topic.getId(), Collections.emptyList());
            boolean questionExists = topicQuestions.stream()
                    .anyMatch(q -> q.getId().equals(questionId));
            if (questionExists) {
                return topic.getId();
            }
        }
        return null; // Question not found in any topic
    }
}
