package com.aspire.asat.registration.controller.userActivity;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.userActivity.UserLoginHistoryResponseDTO;
import com.aspire.asat.registration.data.userActivity.UserLoginStatisticsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for User Activity Reports API
 */
@RequestMapping(WebApiUrlConstants.USER_ACTIVITY_API)
@Tag(name = "User Activity Reports", description = "Retrieve user activity statistics and reports")
public interface UserActivityController {

    @Operation(
            summary = "Get User Login Statistics",
            description = "Retrieves user login activities based on filter type. " +
                    "7Day: Returns last 7 days with daily data. " +
                    "1Month: Returns last 1 month with weekly data (5 weeks). " +
                    "12Month: Returns last 12 months with monthly data (12 months)."
    )
    @GetMapping("/login-statistics")
    ResponseEntity<ApiResponseDto<List<UserLoginStatisticsResponseDTO>>> getUserLoginStatistics(
            @Parameter(description = "Filter type for the report", example = "7Day",
                    schema = @Schema(allowableValues = {"7Day", "1Month", "12Month"}))
            @RequestParam(defaultValue = "7Day") String filterType
    );

    @Operation(
            summary = "Get User Activity History",
            description = "Handles all user activity tracking. End users can view their own activity; " +
                    "client admins can view their own and their users' activity; " +
                    "Aspire admins can view activity for all users."
    )
    @GetMapping("/history")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<UserLoginHistoryResponseDTO>>>> getUserLoginHistory(
            @Parameter(description = "Action type filter", example = "LOGIN",
                    schema = @Schema(allowableValues = {"LOGIN", "LOGOUT"}))
            @RequestParam(required = false) String actionType,

            @Parameter(description = "Start date filter (YYYY-MM-DD format)", example = "2024-01-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "End date filter (YYYY-MM-DD format)", example = "2024-12-31")
            @RequestParam(required = false) LocalDate endDate,

            @Parameter(description = "Username search filter", example = "user@example.com")
            @RequestParam(required = false) String username,

            @Parameter(description = "Page offset (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int offset,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );
}
