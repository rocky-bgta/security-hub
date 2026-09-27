package com.aspire.asat.registration.controller.leaderboard;

import com.aspire.asat.leaderboard.LeaderboardRequestDto;
import com.aspire.asat.leaderboard.LeaderboardResponseDto;
import com.aspire.asat.leaderboard.LeaderboardStatusUpdateDto;
import com.aspire.asat.leaderboard.LeaderboardSummaryDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Leaderboard Management", description = "APIs for managing leaderboards")
@RequestMapping(value = "/api/v1/leaderboards", produces = "application/json")
public interface LeaderboardController {

    @Operation(summary = "Create leaderboard", description = "Admin can create for anyone or as default. Client can create for themselves")
    @PostMapping
    ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> createLeaderboard(
            @Valid @RequestBody LeaderboardRequestDto request);

    @Operation(summary = "Get all leaderboards (For admin/client panel)", description = "Admin sees all, clients see their own + admin defaults")
    @GetMapping
    ResponseEntity<ApiResponseDto<List<LeaderboardResponseDto>>> getAllLeaderboards();

    @Operation(summary = "Get up to 2 active leaderboards for web", description = "Returns up to 2 active leaderboards. HIGH PRIORITY: client's active first, then admin defaults")
    @GetMapping("/active-web")
    ResponseEntity<ApiResponseDto<List<LeaderboardResponseDto>>> getActiveLeaderboardsForWeb();

    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> getLeaderboardById(@PathVariable UUID id);

    @Operation(summary = "Update leaderboard", description = "Admin can update anyone's. Client can only update their own")
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> updateLeaderboard(
            @PathVariable UUID id,
            @Valid @RequestBody LeaderboardRequestDto request);

    @Operation(summary = "Delete leaderboard", description = "Admin can delete anyone's. Client can only delete their own")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<String>> deleteLeaderboard(@PathVariable UUID id);

    @Operation(summary = "Update leaderboard status", description = "Admin can update anyone's status. Client can only update their own")
    @PatchMapping("/status")
    ResponseEntity<ApiResponseDto<LeaderboardResponseDto>> updateLeaderboardStatus(
            @Valid @RequestBody LeaderboardStatusUpdateDto request);

    @GetMapping("/summary")
    ResponseEntity<ApiResponseDto<LeaderboardSummaryDto>> getLeaderboardSummary();
}
