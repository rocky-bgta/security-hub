package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.UserLicenceSnapshotInternalController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.UserLicenceSnapshotRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.service.UserLicenceSnapshotInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserLicenceSnapshotInternalControllerImpl implements UserLicenceSnapshotInternalController {

    private final UserLicenceSnapshotInternalService userLicenceSnapshotInternalService;

    @Override
    public ResponseEntity<ApiResponseDto<Long>> syncUserLicenceSnapshot(UserLicenceSnapshotRequest request) {
        try {
            long matched = userLicenceSnapshotInternalService.syncUserSnapshot(request);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Licensed user snapshot synced successfully", 200, matched));
        } catch (PhishingValidationException e) {
            log.warn("Invalid licence snapshot sync request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Failed to sync licence snapshot for userId={}, clientAdminId={}",
                    request != null ? request.getUserId() : null,
                    request != null ? request.getClientAdminId() : null,
                    e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to sync licensed user snapshot", 500, null));
        }
    }
}
