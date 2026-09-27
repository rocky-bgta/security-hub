package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.controller.MspDashboardController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspDashboardResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspTopicCountsResponseDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.service.msp.MspUserService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MspDashboardControllerImpl implements MspDashboardController {

    private final MspUserService mspUserService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<MspDashboardResponseDto>> getMspDashboard(String mspId) {
        try {
            String resolvedMspId = resolveMspId(mspId);
            log.info("Received request to get MSP dashboard totals for mspId: {}", resolvedMspId);

            MspDashboardResponseDto response = mspUserService.getMspDashboard(resolvedMspId);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "MSP dashboard totals retrieved successfully",
                    HttpStatus.OK.value(),
                    response
            ));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving MSP dashboard for mspId {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving MSP dashboard for mspId {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve MSP dashboard totals: " + e.getMessage(), 500, null));
        }
    }

    private String resolveMspId(String requestMspId) {
        if (requestMspId != null && !requestMspId.isBlank()) {
            return requestMspId;
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null && userContext.getUserId() != null && !userContext.getUserId().isBlank()) {
            return userContext.getUserId();
        }

        throw new RegistrationServiceException("mspId is required");
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspTopicCountsResponseDto>> getMspTopicCounts(String mspId) {
        try {
            String resolvedMspId = resolveMspId(mspId);
            log.info("Received request to get MSP topic counts for mspId: {}", resolvedMspId);

            MspTopicCountsResponseDto response = mspUserService.getMspTopicCounts(resolvedMspId);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "MSP topic distribution retrieved successfully",
                    HttpStatus.OK.value(),
                    response
            ));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving MSP topic counts for mspId {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving MSP topic counts for mspId {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve MSP topic distribution: " + e.getMessage(), 500, null));
        }
    }
}
