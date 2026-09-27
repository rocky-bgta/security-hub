package com.aspire.asat.universal.controller.leaderboard.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.universal.leaderboard.LeaderboardRequestDto;
import com.aspire.asat.universal.leaderboard.LeaderboardResponseDto;
import com.aspire.asat.universal.leaderboard.LeaderboardStatusUpdateDto;
import com.aspire.asat.universal.leaderboard.LeaderboardSummaryDto;
import com.aspire.asat.universal.controller.leaderboard.LeaderboardController;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.service.leaderboard.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LeaderboardControllerImpl implements LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> createLeaderboard(LeaderboardRequestDto request) {
        log.info("Creating leaderboard with name: {}", request.getName());
        
        LeaderboardResponseDto response = leaderboardService.createLeaderboard(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(messageService.get(MessageKeys.LEADERBOARD_ENTRY_CREATED), 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<LeaderboardResponseDto>>>> getAllLeaderboards(int offset, int pageSize, String search) {
        log.info("Fetching all leaderboards for admin/client panel with search: {}, offset: {}, pageSize: {}", search, offset, pageSize);

        AllResponseDto<List<LeaderboardResponseDto>> response = leaderboardService.getAllLeaderboards(offset, pageSize, search);

        return ResponseEntity.ok(
                new ApiResponseDto<>("Leaderboards fetched successfully", 200, response)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<LeaderboardResponseDto>>> getActiveLeaderboardsForWeb() {
        log.info("Fetching exactly 2 active leaderboards for web");

        List<LeaderboardResponseDto> response = leaderboardService.getActiveLeaderboardsForWeb();

        return ResponseEntity.ok(
                new ApiResponseDto<>("Active leaderboards for web fetched successfully", 200, response)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> getLeaderboardById(UUID id) {
        log.info("Fetching leaderboard by id: {}", id);
        
        LeaderboardResponseDto response = leaderboardService.getLeaderboardById(id);

        return ResponseEntity.ok(
                new ApiResponseDto<>("Leaderboard fetched successfully", 200, response)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> updateLeaderboard(UUID id, LeaderboardRequestDto request) {
        log.info("Updating leaderboard with id: {}", id);
        
        LeaderboardResponseDto response = leaderboardService.updateLeaderboard(id, request);

        return ResponseEntity.ok(
                new ApiResponseDto<>(messageService.get(MessageKeys.LEADERBOARD_ENTRY_UPDATED), 200, response)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteLeaderboard(UUID id) {
        log.info("Deleting leaderboard with id: {}", id);
        
        leaderboardService.deleteLeaderboard(id);

        return ResponseEntity.ok(
                new ApiResponseDto<>(messageService.get(MessageKeys.LEADERBOARD_ENTRY_REMOVED), 200, "Deleted")
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> updateLeaderboardStatus(LeaderboardStatusUpdateDto request) {
        log.info("Updating leaderboard status for id: {}", request.getId());
        
        LeaderboardResponseDto response = leaderboardService.updateLeaderboardStatus(request);

        return ResponseEntity.ok(
                new ApiResponseDto<>("Leaderboard status updated successfully", 200, response)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<LeaderboardSummaryDto>> getLeaderboardSummary() {
        log.info("Fetching leaderboard summary");
        
        LeaderboardSummaryDto response = leaderboardService.getLeaderboardSummary();

        return ResponseEntity.ok(
                new ApiResponseDto<>("Leaderboard summary fetched successfully", 200, response)
        );
    }
}
