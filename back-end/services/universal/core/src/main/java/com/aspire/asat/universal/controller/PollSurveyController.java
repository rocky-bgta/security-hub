package com.aspire.asat.universal.controller;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.universal.pool.*;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import com.aspire.asat.universal.exception.UnprocessableEntityException;
import com.aspire.asat.universal.service.PollSurveyService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import com.aspire.asat.universal.enums.PollSurveyStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollSurveyController {
    private final PollSurveyService pollSurveyService;
    private final MessageService messageService;
    // Lombok @RequiredArgsConstructor will create the constructor for the final field


    @PostMapping
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created poll/survey: #{#req.title != null ? #req.title : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<PollSurveyResponse>> create(@RequestBody PollSurveyCreateRequest req) {
        PollSurveyResponse res = pollSurveyService.createPollSurvey(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(messageService.get(MessageKeys.SURVEY_CREATED), 201, res));
    }

    @PutMapping("/{id}")
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated poll/survey: #{#id}",
            oldValueExpression = "#{#id.toString()}",
            newValueExpression = "#{#req.title != null ? #req.title : #id.toString()}"
    )
    public ResponseEntity<ApiResponseDto<PollSurveyResponse>> update(@PathVariable UUID id, @RequestBody PollSurveyUpdateRequest req) {
        PollSurveyResponse res = pollSurveyService.updatePollSurvey(req, id);
        if (res == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>("Not found", 404, null));
        return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.SURVEY_UPDATED), 200, res));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<OffsetPageDto<PollSurveyResponse>>> list(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "status", required = false) String status) {

        OffsetPageDto<PollSurveyResponse> dto = pollSurveyService.getAllPollsPage(offset, pageSize, search, type, status);
        return ResponseEntity.ok(new ApiResponseDto<>("Polls fetched successfully", 200, dto));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<OffsetPageDto<PollSurveyResponse>>> active(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {

        OffsetPageDto<PollSurveyResponse> dto = pollSurveyService.getActivePollsPage(offset, pageSize);
        return ResponseEntity.ok(new ApiResponseDto<>("End users fetched", 200, dto));
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<ApiResponseDto<String>> vote(@PathVariable UUID pollId,
                                                       @RequestParam UUID questionId,
                                                       @RequestParam UUID answerId) {
        try {
            pollSurveyService.recordVote(pollId, questionId, answerId);
            return ResponseEntity.ok(new ApiResponseDto<>("Vote recorded", 200, null));
        } catch (IllegalStateException ie) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDto<>(ie.getMessage(), 409, null));
        } catch (IllegalArgumentException ae) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(ae.getMessage(), 401, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>("Internal server error", 500, null));
        }
    }

    // Bulk voting endpoint for submitting votes to multiple questions
    // Note: Users can only vote multiple times if poll.allowMultipleSubmissions = true
    @PostMapping("/{pollId}/votes")
    public ResponseEntity<ApiResponseDto<String>> votes(@PathVariable UUID pollId,
                                                        @RequestBody PollSurveyVoteRequest req) {
        try {
            pollSurveyService.recordVotes(pollId, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>("Votes recorded", 201, null));
        } catch (UnprocessableEntityException ue) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiResponseDto<>(ue.getMessage(), 422, null));
        } catch (IllegalArgumentException ae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(ae.getMessage(), 400, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>(e.getMessage(), 500, null));
        }
    }

    // Set status endpoint: activating a poll will deactivate other polls
    @PutMapping("/{pollId}/status")
    public ResponseEntity<ApiResponseDto<PollSurveyResponse>> setStatus(@PathVariable UUID pollId,
                                                                        @RequestParam String status) {
        try {
            PollSurveyStatus s = PollSurveyStatus.valueOf(status.toUpperCase());
            PollSurveyResponse res = pollSurveyService.setPollStatus(pollId, s);
            return ResponseEntity.ok(new ApiResponseDto<>("Status updated", 200, res));
        } catch (IllegalArgumentException iae) {
            // either enum parse failure or service validation (poll not found)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDto<>(iae.getMessage(), 400, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDto<>("Internal server error", 500, null));
        }
    }

    @GetMapping("/{pollId}/summary")
    public ResponseEntity<ApiResponseDto<PollSurveySummaryResponse>> summary(@PathVariable UUID pollId) {
        PollSurveySummaryResponse s = pollSurveyService.getPollSurveySummary(pollId);
        if (s == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponseDto<>("Not found", 404, null));
        return ResponseEntity.ok(new ApiResponseDto<>("OK", 200, s));
    }

    @GetMapping("/{pollId}")
    public ResponseEntity<ApiResponseDto<PollSurveyDetailWithSummaryResponse>> getDetailsWithSummary(@PathVariable UUID pollId) {
        PollSurveyDetailWithSummaryResponse details = pollSurveyService.getPollSurveyDetailWithSummary(pollId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>("Poll/Survey not found", 404, null));
        }
        return ResponseEntity.ok(new ApiResponseDto<>("Poll/Survey details retrieved successfully", 200, details));
    }

    @GetMapping("/latest-active")
    public ResponseEntity<ApiResponseDto<PollSurveyDetailWithSummaryResponse>> getLatestActiveWithUserVotes() {
        PollSurveyDetailWithSummaryResponse details = pollSurveyService.getLatestActivePollWithUserVotes();
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>("No active poll/survey found", 404, null));
        }
        return ResponseEntity.ok(new ApiResponseDto<>("Latest active poll/survey retrieved successfully", 200, details));
    }
}
