package com.aspire.asat.registration.controller.userActivity;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.userActivity.UserLoginHistoryResponseDTO;
import com.aspire.asat.registration.data.userActivity.UserLoginStatisticsResponseDTO;
import com.aspire.asat.registration.enums.LoginHistoryFilterType;
import com.aspire.asat.registration.service.userActivity.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller implementation for User Activity Reports API
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserActivityControllerImpl implements UserActivityController {

    private final UserActivityService userActivityService;

    @Override
    public ResponseEntity<ApiResponseDto<List<UserLoginStatisticsResponseDTO>>> getUserLoginStatistics(String filterType) {
        
        log.info("Received request to get user login statistics with filter type: {}", filterType);

        try {
            // Validate and parse a filter type
            LoginHistoryFilterType filter = LoginHistoryFilterType.fromValue(filterType);
            List<UserLoginStatisticsResponseDTO> statistics = userActivityService.getUserLoginStatistics(filter);

            String message = statistics.isEmpty()
                    ? "No login statistics found for the specified filter type"
                    : "Successfully retrieved login statistics";

            ApiResponseDto<List<UserLoginStatisticsResponseDTO>> response = new ApiResponseDto<>(
                    message,
                    HttpStatus.OK.value(),
                    statistics);

            log.info("Successfully processed user login statistics request with filter {} - Found data for {} periods", 
                    filterType, statistics.size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid filter type provided: {}", filterType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>("Invalid filter type. Valid options are: 7Day, 1Month, 12Month", 400, null));
        } catch (Exception e) {
            log.error("Error getting user login statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get user login statistics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<UserLoginHistoryResponseDTO>>>> getUserLoginHistory(
            String actionType, LocalDate startDate, LocalDate endDate, String username, int offset, int pageSize) {
        
        log.info("Received request to get user login history with actionType: {}, startDate: {}, endDate: {}, username: {}, offset: {}, pageSize: {}", 
                actionType, startDate, endDate, username, offset, pageSize);

        try {
            AllResponseDto<List<UserLoginHistoryResponseDTO>> history = userActivityService.getUserLoginHistory(
                    actionType, startDate, endDate, username, offset, pageSize);

            String message = history.getItems().isEmpty()
                    ? "No login history found for the specified criteria"
                    : "Successfully retrieved user login history";

            ApiResponseDto<AllResponseDto<List<UserLoginHistoryResponseDTO>>> response = new ApiResponseDto<>(
                    message,
                    HttpStatus.OK.value(),
                    history);

            log.info("Successfully processed user login history request - Found {} records, offset: {}, pageSize: {}, total: {}", 
                    history.getItems().size(), history.getOffset(), history.getPageSize(), history.getTotal());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting user login history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get user login history: " + e.getMessage(), 500, null));
        }
    }
}
