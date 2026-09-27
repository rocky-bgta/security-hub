package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.controller.ActivityLogController;
import com.aspire.asat.common.dto.activitylog.ActivityLogDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ActivityLogResponseDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogResponseDto;
import com.aspire.asat.common.dto.activitylog.CreateActivityLogDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.ActivityLog;
import com.aspire.asat.common.util.UserTypeInferenceUtil;
import com.aspire.asat.registration.service.ActivityLogService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.time.Instant;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ActivityLogControllerImpl implements ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<ClientAdminActivityLogResponseDto>> getClientAdminActivityLogs(
            ClientAdminActivityLogRequestDto requestDto) {
        try {
            log.info("Retrieving activity logs for client admin.");

            ClientAdminActivityLogResponseDto response = activityLogService.getClientAdminActivityLogs(requestDto);

            return ResponseEntity.ok(new ApiResponseDto<>("Activity logs retrieved successfully", 200, response));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving activity logs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve activity logs: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientAdminActivityLogDto>> getClientAdminActivityLogById(String activityId) {
        try {
            log.info("Retrieving activity log with ID: {}", activityId);

            ClientAdminActivityLogDto activityLog = activityLogService.getClientAdminActivityLogById(activityId);

            return ResponseEntity.ok(new ApiResponseDto<>("Activity log retrieved successfully", 200, activityLog));

        } catch (RegistrationServiceException e) {
            log.error("Activity log not found with ID: {}", activityId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving activity log with ID: {}", activityId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve activity log: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ActivityLogResponseDto>> getActivityLogs(
            ActivityLogRequestDto requestDto) {
        try {
            log.info("Retrieving activity logs with role-based access control.");

            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            if (currentUserContext == null || currentUserContext.getUserId() == null) {
                log.error("Missing user context");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>("Unauthorized: Missing user context", 403, null));
            }

            ActivityLogResponseDto response = activityLogService.getActivityLogs(requestDto, currentUserContext);

            return ResponseEntity.ok(new ApiResponseDto<>("Activity logs retrieved successfully", 200, response));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving activity logs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve activity logs: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ActivityLogDto>> createActivityLog(
            CreateActivityLogDto requestDto) {
        try {
            log.info("Creating new activity log for user: {}", requestDto.getUserId());

            // Infer userType when null so ASPIRE_ADMIN/MSP are not saved as SYSTEM_USER (e.g. from auth/login/logout)
            UserType userType = requestDto.getUserType();
            if (userType == null && StringUtils.hasText(requestDto.getUserId())) {
                userType = UserTypeInferenceUtil.inferUserTypeFromContext(
                        requestDto.getUserId(), requestDto.getMspId(), requestDto.getClientAdminId());
            }
            if (userType == null) {
                userType = UserType.SYSTEM_USER;
            }

            // Convert DTO to entity
            ActivityLog activityLog = ActivityLog.builder()
                    .userId(requestDto.getUserId())
                    .countryId(requestDto.getCountryId())
                    .aspireAdminId(requestDto.getAspireAdminId())
                    .mspId(requestDto.getMspId())
                    .clientAdminId(requestDto.getClientAdminId())
                    .email(requestDto.getEmail())
                    .username(requestDto.getUsername())
                    .fullName(requestDto.getFullName())
                    .userType(userType)
                    .activityType(requestDto.getActivityType())
                    .activityStatus(requestDto.getActivityStatus())
                    .description(requestDto.getDescription())
                    .oldValue(requestDto.getOldValue())
                    .newValue(requestDto.getNewValue())
                    .ipAddress(requestDto.getIpAddress())
                    .createdAt(requestDto.getCreatedAt() != null ? requestDto.getCreatedAt() : Instant.now())
                    .build();

            // Save the activity log
            ActivityLog savedActivityLog = activityLogService.logActivity(activityLog);

            // Convert entity to DTO for response
            ActivityLogDto responseDto = ActivityLogDto.builder()
                    .activityId(savedActivityLog.getId())
                    .userId(savedActivityLog.getUserId())
                    .countryId(savedActivityLog.getCountryId())
                    .aspireAdminId(savedActivityLog.getAspireAdminId())
                    .mspId(savedActivityLog.getMspId())
                    .clientAdminId(savedActivityLog.getClientAdminId())
                    .userType(savedActivityLog.getUserType())
                    .userName(savedActivityLog.getUsername())
                    .userEmail(savedActivityLog.getEmail())
                    .fullName(savedActivityLog.getFullName())
                    .activityType(savedActivityLog.getActivityType())
                    .activityDescription(savedActivityLog.getDescription())
                    .activityStatus(savedActivityLog.getActivityStatus())
                    .timestamp(savedActivityLog.getCreatedAt())
                    .ipAddress(savedActivityLog.getIpAddress())
                    .description(savedActivityLog.getDescription())
                    .oldValue(savedActivityLog.getOldValue())
                    .newValue(savedActivityLog.getNewValue())
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>("Activity log created successfully", 200, responseDto));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error creating activity log: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to create activity log: " + e.getMessage(), 500, null));
        }
    }

}

