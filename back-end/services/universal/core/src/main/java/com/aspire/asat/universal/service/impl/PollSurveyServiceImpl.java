package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.enums.PollSurveyStatus;
import com.aspire.asat.universal.enums.QuestionType;
import com.aspire.asat.universal.entity.*;

import com.aspire.asat.universal.pool.*;
import com.aspire.asat.universal.repository.PollSurveyRepository;
import com.aspire.asat.universal.repository.PollSurveyVoteLogRepository;
import com.aspire.asat.universal.repository.PollSurveySubmissionRepository;
import com.aspire.asat.universal.service.PollSurveyService;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class PollSurveyServiceImpl implements PollSurveyService {
    
    private final PollSurveyRepository pollSurveyRepository;
    private final PollSurveySubmissionRepository submissionRepository;
    private final PollSurveyVoteLogRepository voteLogRepository;
    private final UserCurrentContextService userCurrentContextService;


    @Override
    @Transactional
    public PollSurveyResponse createPollSurvey(PollSurveyCreateRequest req) {
        CurrentUserContext user = userCurrentContextService.getCurrentUserContext();

        PollSurvey entity = new PollSurvey();
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setType(req.getType());
        entity.setStatus(req.getStatus());
        entity.setStartDate(req.getStartDate());
        entity.setEndDate(req.getEndDate());
        entity.setShowResultsToUsers(Optional.ofNullable(req.getShowResultsToUsers()).orElse(false));
        entity.setAllowMultipleSubmissions(Optional.ofNullable(req.getAllowMultipleSubmissions()).orElse(false));
        entity.setCreatedBy(user != null ? user.getUserId() : null);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        addQuestionsToEntity(entity, req.getQuestions());

        PollSurvey saved = pollSurveyRepository.save(entity);
        return toResponse(saved);
    }

    // Helper: build and add questions to entity
    private void addQuestionsToEntity(PollSurvey entity, List<PollSurveyQuestionCreateDto> questions) {
        if (questions == null) return;
        for (PollSurveyQuestionCreateDto qdto : questions) {
            PollSurveyQuestion q = buildQuestionFromDto(qdto);
            entity.addQuestion(q);
        }
    }

    // Helper: build a single question with its answers
    private PollSurveyQuestion buildQuestionFromDto(PollSurveyQuestionCreateDto qdto) {
        PollSurveyQuestion q = new PollSurveyQuestion();
        q.setQuestionText(qdto.getQuestionText());
        q.setQuestionType(qdto.getQuestionType() != null ? qdto.getQuestionType() : QuestionType.RADIO);
        if (qdto.getAnswers() != null) {
            for (PollSurveyAnswerCreateDto adto : qdto.getAnswers()) {
                PollSurveyAnswer a = new PollSurveyAnswer();
                a.setAnswerText(adto.getAnswerText());
                q.addAnswer(a);
            }
        }
        return q;
    }

    @Override
    @Transactional
    public PollSurveyResponse updatePollSurvey(PollSurveyUpdateRequest req, UUID id) {
        // keep this public API method minimal to satisfy complexity checks
        return updatePollSurveyInternal(req, id);
    }

    // note: removed @Transactional from the private helper to avoid calling transactional methods via 'this'
    private PollSurveyResponse updatePollSurveyInternal(PollSurveyUpdateRequest req, UUID id) {
        PollSurvey existing = loadExistingIfUpdatable(id);
        if (existing == null) return null;

        // Apply updates and replace questions via small helpers to reduce complexity
        applyUpdateFields(existing, req);
        replaceQuestionsFromRequest(existing, req.getQuestions());

        PollSurvey saved = pollSurveyRepository.save(existing);
        return toResponse(saved);
    }

    // Helper: load existing poll and validate it's updatable
    private PollSurvey loadExistingIfUpdatable(UUID id) {
        Optional<PollSurvey> opt = pollSurveyRepository.findById(id);
        if (opt.isEmpty()) return null;
        if (voteLogRepository.existsByPollSurveyId(id)) {
            throw new UnprocessableEntityException("Cannot update poll/survey because it already has votes/responses");
        }
        return opt.get();
    }

    // Helper: update basic fields and timestamps
    private void applyUpdateFields(PollSurvey existing, PollSurveyUpdateRequest req) {
        CurrentUserContext user = userCurrentContextService.getCurrentUserContext();
        existing.setTitle(req.getTitle());
        existing.setDescription(req.getDescription());
        existing.setType(req.getType());
        existing.setStatus(req.getStatus());
        existing.setStartDate(req.getStartDate());
        existing.setEndDate(req.getEndDate());
        existing.setShowResultsToUsers(Optional.ofNullable(req.getShowResultsToUsers()).orElse(false));
        existing.setAllowMultipleSubmissions(Optional.ofNullable(req.getAllowMultipleSubmissions()).orElse(false));
        existing.setUpdatedBy(user != null ? user.getUserId() : null);
        existing.setUpdatedAt(LocalDateTime.now());
    }

    // Helper: replace questions completely from request
    private void replaceQuestionsFromRequest(PollSurvey existing, List<PollSurveyQuestionCreateDto> questions) {
        existing.clearQuestions();
        addQuestionsToEntity(existing, questions);
    }

    @Override
    public Page<PollSurveyResponse> getAllPolls(Pageable pageable) {
        Page<PollSurvey> page = pollSurveyRepository.findAll(pageable);
        // use Stream.toList() to follow modern API and immutability guidance
        List<PollSurveyResponse> dtos = page.getContent().stream().map(this::toResponse).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public Page<PollSurveyResponse> getActivePollsOrSurveys(Pageable pageable) {
        Page<PollSurvey> page = pollSurveyRepository.findByStatus(PollSurveyStatus.ACTIVE, pageable);
        List<PollSurveyResponse> dtos = page.getContent().stream().map(this::toResponse).toList();
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }


    @Override
    public OffsetPageDto<PollSurveyResponse> getAllPollsPage(int offset, int pageSize, String search, String type, String status) {
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize);

        Page<PollSurvey> page = pollSurveyRepository.findAllWithFilters(search, type, status, pageable);
        List<PollSurveyResponse> dtos = page.getContent().stream().map(this::toResponse).toList();

        return new OffsetPageDto<>(offset, pageSize, page.getTotalElements(), dtos);
    }

    // New: offset/pageSize -> OffsetPageDto for active polls
    @Override
    public OffsetPageDto<PollSurveyResponse> getActivePollsPage(int offset, int pageSize) {
        // Treat `offset` as the page number (0-based)
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize);

        // Get only the requested page of data
        Page<PollSurvey> page = pollSurveyRepository.findByStatus(PollSurveyStatus.ACTIVE, pageable);
        List<PollSurveyResponse> dtos = page.getContent().stream().map(this::toResponse).toList();
        long total = pollSurveyRepository.countByStatus(PollSurveyStatus.ACTIVE);

        return new OffsetPageDto<>(offset, pageSize, total, dtos);
    }

    @Override
    @Transactional
    public void recordVote(UUID pollSurveyId, UUID questionId, UUID answerId) {
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
        String userId = ctx != null ? ctx.getUserId() : null;
        if (userId == null) {
            throw new IllegalArgumentException("User context not found");
        }
        // fetch poll to validate type and existence
        Optional<PollSurvey> pollOpt = pollSurveyRepository.findById(pollSurveyId);
        PollSurvey poll = pollOpt.orElseThrow(() -> new IllegalArgumentException("Poll not found"));
        // validate survey type for single vote endpoint
        if (poll.getType() != null && poll.getType().name().equalsIgnoreCase("SURVEY")) {
            throw new IllegalArgumentException("Use bulk /votes endpoint for surveys (multiple answers)");
        }
        Boolean allowMultiple = poll.getAllowMultipleSubmissions();
        // If allowMultiple is false or null, enforce single submission per user
        if (!Boolean.TRUE.equals(allowMultiple)) {
            // create a submission document to ensure single submission per user+poll (DB uniqueness)
            PollSurveySubmission submission = PollSurveySubmission.builder()
                    .userId(userId)
                    .pollSurveyId(pollSurveyId)
                    .build();
            try {
                submissionRepository.insert(submission);
            } catch (DuplicateKeyException dke) {
                throw new UnprocessableEntityException("You have already voted for this poll");
            }
        }
        // If allowMultiple is true, skip the check and allow multiple submissions

        // Always store the vote in the vote log
        PollSurveyVoteLog voteLog = PollSurveyVoteLog.builder()
                .userId(userId)
                .pollSurveyId(pollSurveyId)
                .questionId(questionId)
                .answerId(answerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        voteLogRepository.save(voteLog);
    }


    @Override
    @Transactional
    public void recordVotes(UUID pollSurveyId, PollSurveyVoteRequest req) {
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
        String userId = ctx != null ? ctx.getUserId() : null;
        log.debug("Recording votes for poll: {}, user: {}", pollSurveyId, userId);

        PollSurvey poll = pollSurveyRepository.findById(pollSurveyId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found"));
        log.debug("Poll found, questions count: {}", poll.getQuestions().size());

        PollSurveySubmission submission = validateAndCreateSubmission(userId, pollSurveyId, poll.getAllowMultipleSubmissions());

        List<PollSurveyVoteLog> voteLogs = buildVoteLogsFromRequest(userId, pollSurveyId, req, poll);

        persistVoteLogs(voteLogs, submission);
    }

    // Helper: validate submission eligibility and create submission record
    private PollSurveySubmission validateAndCreateSubmission(String userId, UUID pollSurveyId, Boolean allowMultiple) {
        if (!Boolean.TRUE.equals(allowMultiple)) {
            boolean hasSubmitted = submissionRepository.existsByUserIdAndPollSurveyId(userId, pollSurveyId);
            if (hasSubmitted) {
                throw new UnprocessableEntityException("You have already voted for this poll/survey");
            }
        } else {
            log.debug("Multiple submissions allowed - skipping submission check");
        }

        PollSurveySubmission submission = PollSurveySubmission.builder()
                .userId(userId)
                .pollSurveyId(pollSurveyId)
                .build();
        try {
            submissionRepository.insert(submission);
            log.debug("Submission record created - single submission enforced");
            return submission;
        } catch (DuplicateKeyException dke) {
            throw new UnprocessableEntityException("You have already voted for this poll/survey");
        }
    }

    // Helper: build all vote logs from the request
    private List<PollSurveyVoteLog> buildVoteLogsFromRequest(String userId, UUID pollSurveyId,
                                                              PollSurveyVoteRequest req, PollSurvey poll) {
        List<PollSurveyVoteLog> voteLogs = new ArrayList<>();
        log.debug("Request votes: {}", (req.getVotes() != null ? req.getVotes().size() : "NULL"));

        if (req.getVotes() == null) {
            return voteLogs;
        }

        for (PollSurveyVoteRequest.QuestionVote qv : req.getVotes()) {
            processQuestionVote(userId, pollSurveyId, qv, poll, voteLogs);
        }

        return voteLogs;
    }

    // Helper: process a single question vote
    private void processQuestionVote(String userId, UUID pollSurveyId,
                                     PollSurveyVoteRequest.QuestionVote qv,
                                     PollSurvey poll, List<PollSurveyVoteLog> voteLogs) {
        UUID qid = qv.getQuestionId();
        log.debug("Processing questionId: {}", qid);
        log.debug("AnswerIds: {}", qv.getAnswerIds());
        log.debug("TextAnswer: {}", qv.getTextAnswer());

        PollSurveyQuestion question = findQuestionById(poll, qid);
        if (question == null) {
            log.warn("Question NOT found in poll: {}", qid);
            return;
        }

        log.debug("Question found, type: {}", question.getQuestionType());
        QuestionType qType = question.getQuestionType() != null ? question.getQuestionType() : QuestionType.RADIO;

        if (isTextQuestion(qType)) {
            handleTextQuestionVote(userId, pollSurveyId, qid, qv.getTextAnswer(), voteLogs);
        } else {
            handleChoiceQuestionVote(userId, pollSurveyId, qid, qv.getAnswerIds(), qType, voteLogs);
        }
    }

    // Helper: find question by ID
    private PollSurveyQuestion findQuestionById(PollSurvey poll, UUID questionId) {
        return poll.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElse(null);
    }

    // Helper: check if question type is text-based
    private boolean isTextQuestion(QuestionType qType) {
        return qType == QuestionType.SHORT_TEXT || qType == QuestionType.LONG_TEXT;
    }

    // Helper: handle text question vote
    private void handleTextQuestionVote(String userId, UUID pollSurveyId, UUID questionId,
                                       String textAnswer, List<PollSurveyVoteLog> voteLogs) {
        if (textAnswer != null && !textAnswer.trim().isEmpty()) {
            PollSurveyVoteLog voteLog = PollSurveyVoteLog.builder()
                    .userId(userId)
                    .pollSurveyId(pollSurveyId)
                    .questionId(questionId)
                    .textAnswer(textAnswer)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            voteLogs.add(voteLog);
            log.debug("Added text vote");
        }
    }

    // Helper: handle choice-based question vote (RADIO, MCQ, RATING)
    private void handleChoiceQuestionVote(String userId, UUID pollSurveyId, UUID questionId,
                                         List<UUID> answerIds, QuestionType qType,
                                         List<PollSurveyVoteLog> voteLogs) {
        if (answerIds == null || answerIds.isEmpty()) {
            log.debug("AnswerIds is null or empty");
            return;
        }

        log.debug("Processing {} answers", answerIds.size());
        validateAnswerCount(qType, answerIds.size());

        for (UUID aid : answerIds) {
            PollSurveyVoteLog voteLog = PollSurveyVoteLog.builder()
                    .userId(userId)
                    .pollSurveyId(pollSurveyId)
                    .questionId(questionId)
                    .answerId(aid)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            voteLogs.add(voteLog);
            log.debug("Added choice vote for answerId: {}", aid);
        }
    }

    // Helper: validate answer count based on question type
    private void validateAnswerCount(QuestionType qType, int answerCount) {
        if ((qType == QuestionType.RADIO || qType == QuestionType.RATING) && answerCount > 1) {
            throw new IllegalArgumentException("Only one answer allowed for " + qType + " questions");
        }
    }

    // Helper: persist vote logs with cleanup on error
    private void persistVoteLogs(List<PollSurveyVoteLog> voteLogs, PollSurveySubmission submission) {
        log.debug("Total votes to save: {}", voteLogs.size());

        if (voteLogs.isEmpty()) {
            log.warn("WARNING - No votes to save!");
            return;
        }

        try {
            voteLogRepository.saveAll(voteLogs);
            log.info("Successfully saved {} votes to database", voteLogs.size());
        } catch (Exception e) {
            log.error("ERROR saving votes: {}", e.getMessage(), e);
            cleanupSubmissionOnError(submission);
            throw e;
        }
    }

    // Helper: cleanup submission record on error
    private void cleanupSubmissionOnError(PollSurveySubmission submission) {
        if (submission != null) {
            try {
                submissionRepository.deleteById(submission.getId());
            } catch (Exception ignored) {
                // Ignore cleanup errors
            }
        }
    }

    @Override
    @Transactional
    public PollSurveyResponse setPollStatus(UUID pollSurveyId, PollSurveyStatus status) {
        Optional<PollSurvey> opt = pollSurveyRepository.findById(pollSurveyId);
        if (opt.isEmpty()) return null;
        PollSurvey target = opt.get();
        if (status == PollSurveyStatus.ACTIVE) {
            // deactivate other polls
            List<PollSurvey> all = pollSurveyRepository.findAll();
            for (PollSurvey p : all) {
                if (!p.getId().equals(pollSurveyId) && p.getStatus() == PollSurveyStatus.ACTIVE) {
                    p.setStatus(PollSurveyStatus.INACTIVE);
                }
            }
            // persist others
            pollSurveyRepository.saveAll(all);
            target.setStatus(PollSurveyStatus.ACTIVE);
            PollSurvey saved = pollSurveyRepository.save(target);
            return toResponse(saved);
        } else {
            // set to inactive only
            target.setStatus(PollSurveyStatus.INACTIVE);
            PollSurvey saved = pollSurveyRepository.save(target);
            return toResponse(saved);
        }
    }

    @Override
    public PollSurveySummaryResponse getPollSurveySummary(UUID pollSurveyId) {
        Optional<PollSurvey> opt = pollSurveyRepository.findById(pollSurveyId);
        if (opt.isEmpty()) return null;
        PollSurvey ps = opt.get();
        PollSurveySummaryResponse summary = new PollSurveySummaryResponse();
        summary.setPollSurveyId(ps.getId());
        summary.setTitle(ps.getTitle());
        List<PollSurveySummaryResponse.QuestionSummary> qsummaries = new ArrayList<>();

        // aggregate counts
        List<Object[]> counts = voteLogRepository.countVotesByPollSurveyId(pollSurveyId);
        Map<UUID, Long> answerCounts = new HashMap<>();
        for (Object[] row : counts) {
            UUID aid = (UUID) row[0];
            Long cnt = (Long) row[1];
            answerCounts.put(aid, cnt != null ? cnt : 0L);
        }

        for (PollSurveyQuestion q : ps.getQuestions()) {
            PollSurveySummaryResponse.QuestionSummary qs = new PollSurveySummaryResponse.QuestionSummary();
            qs.setQuestionId(q.getId());
            qs.setQuestionText(q.getQuestionText());

            // Calculate total votes for this question
            long totalVotesForQuestion = 0L;
            for (PollSurveyAnswer a : q.getAnswers()) {
                totalVotesForQuestion += answerCounts.getOrDefault(a.getId(), 0L);
            }
            qs.setTotalVotes(totalVotesForQuestion);

            // Build answer summaries with percentage
            List<PollSurveySummaryResponse.AnswerSummary> ans = new ArrayList<>();
            for (PollSurveyAnswer a : q.getAnswers()) {
                PollSurveySummaryResponse.AnswerSummary as = new PollSurveySummaryResponse.AnswerSummary();
                as.setAnswerId(a.getId());
                as.setAnswerText(a.getAnswerText());
                long voteCount = answerCounts.getOrDefault(a.getId(), 0L);
                as.setVoteCount(voteCount);

                // Calculate percentage (avoid division by zero)
                double percentage = totalVotesForQuestion > 0
                        ? (voteCount * 100.0) / totalVotesForQuestion
                        : 0.0;
                as.setPercentage(Math.round(percentage * 100.0) / 100.0); // Round to 2 decimal places

                ans.add(as);
            }
            qs.setAnswers(ans);
            qsummaries.add(qs);
        }
        summary.setQuestions(qsummaries);
        return summary;
    }


    private PollSurveyResponse toResponse(PollSurvey ps) {
        PollSurveyResponse r = new PollSurveyResponse();
        r.setId(ps.getId());
        r.setTitle(ps.getTitle());
        r.setDescription(ps.getDescription());
        r.setType(ps.getType());
        r.setStatus(ps.getStatus());
        r.setStartDate(ps.getStartDate());
        r.setEndDate(ps.getEndDate());
        r.setShowResultsToUsers(ps.getShowResultsToUsers());
        r.setAllowMultipleSubmissions(ps.getAllowMultipleSubmissions());
        List<PollSurveyQuestionDto> qdtos = ps.getQuestions().stream().map(q -> {
            PollSurveyQuestionDto qd = new PollSurveyQuestionDto();
            qd.setId(q.getId());
            qd.setQuestionText(q.getQuestionText());
            qd.setQuestionType(q.getQuestionType());
            List<PollSurveyAnswerDto> adtos = q.getAnswers().stream().map(a -> {
                PollSurveyAnswerDto ad = new PollSurveyAnswerDto();
                ad.setId(a.getId());
                ad.setAnswerText(a.getAnswerText());
                return ad;
            }).toList();
            qd.setAnswers(adtos);
            return qd;
        }).toList();
        r.setQuestions(qdtos);
        return r;
    }

    @Override
    public PollSurveyDetailWithSummaryResponse getPollSurveyDetailWithSummary(UUID pollSurveyId) {
        Optional<PollSurvey> opt = pollSurveyRepository.findById(pollSurveyId);
        if (opt.isEmpty()) return null;

        PollSurvey ps = opt.get();

        // Get poll/survey details using existing toResponse method
        PollSurveyResponse pollDetails = toResponse(ps);

        // Get summary using existing getPollSurveySummary method
        PollSurveySummaryResponse summary = getPollSurveySummary(pollSurveyId);

        // Create combined response
        return PollSurveyDetailWithSummaryResponse.builder()
                .id(pollDetails.getId())
                .title(pollDetails.getTitle())
                .description(pollDetails.getDescription())
                .type(pollDetails.getType())
                .status(pollDetails.getStatus())
                .startDate(pollDetails.getStartDate())
                .endDate(pollDetails.getEndDate())
                .showResultsToUsers(pollDetails.getShowResultsToUsers())
                .allowMultipleSubmissions(pollDetails.getAllowMultipleSubmissions())
                .questions(pollDetails.getQuestions())
                .summary(summary)
                .build();
    }

    @Override
    public PollSurveyDetailWithSummaryResponse getLatestActivePollWithUserVotes() {
        // Get current user context
        CurrentUserContext ctx = userCurrentContextService.getCurrentUserContext();
        String userId = ctx != null ? ctx.getUserId() : null;

        // Find the latest active poll/survey
        List<PollSurvey> activePolls = pollSurveyRepository.findByStatusOrderByCreatedAtDesc(
                PollSurveyStatus.ACTIVE,
                PageRequest.of(0, 1)
        );

        if (activePolls.isEmpty()) {
            return null;
        }

        PollSurvey latestActivePoll = activePolls.get(0);
        UUID pollId = latestActivePoll.getId();

        // Get poll/survey details
        PollSurveyResponse pollDetails = toResponse(latestActivePoll);

        // Get summary only if showResultsToUsers is true
        PollSurveySummaryResponse summary = null;
        Boolean showResults = latestActivePoll.getShowResultsToUsers();
        if (showResults != null && showResults) {
            summary = getPollSurveySummary(pollId);
        }

        // Get user's votes if user is authenticated
        List<UUID> userSelectedAnswerIds = new ArrayList<>();
        if (userId != null) {
            List<PollSurveyVoteLog> userVotes = voteLogRepository.findByUserIdAndPollSurveyId(userId, pollId);
            userSelectedAnswerIds = userVotes.stream()
                    .map(PollSurveyVoteLog::getAnswerId)
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Create combined response with user votes
        return PollSurveyDetailWithSummaryResponse.builder()
                .id(pollDetails.getId())
                .title(pollDetails.getTitle())
                .description(pollDetails.getDescription())
                .type(pollDetails.getType())
                .status(pollDetails.getStatus())
                .startDate(pollDetails.getStartDate())
                .endDate(pollDetails.getEndDate())
                .showResultsToUsers(pollDetails.getShowResultsToUsers())
                .allowMultipleSubmissions(pollDetails.getAllowMultipleSubmissions())
                .questions(pollDetails.getQuestions())
                .summary(summary)
                .userSelectedAnswerIds(userSelectedAnswerIds)
                .build();
    }
}
