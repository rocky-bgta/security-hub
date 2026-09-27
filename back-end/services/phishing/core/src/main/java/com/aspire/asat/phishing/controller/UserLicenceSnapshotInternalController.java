package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.UserLicenceSnapshotRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Internal User Licence Snapshot",
        description = "Service-to-service sync of Registration end-user fields onto phishing_user_licence")
@RequestMapping(value = WebApiUrlConstants.INTERNAL_USER_LICENCE_SNAPSHOT_PATH)
public interface UserLicenceSnapshotInternalController {

    @Operation(summary = "Sync licensed user profile snapshot",
            description = "Updates firstName, lastName, phoneNumber, departmentName, countryName, and active "
                    + "on all phishing_user_licence rows for userId and clientAdminId. "
                    + "Zero matches is success. Requires X-Internal-Service-Key.")
    @PutMapping
    ResponseEntity<ApiResponseDto<Long>> syncUserLicenceSnapshot(
            @Valid @RequestBody UserLicenceSnapshotRequest request);
}
