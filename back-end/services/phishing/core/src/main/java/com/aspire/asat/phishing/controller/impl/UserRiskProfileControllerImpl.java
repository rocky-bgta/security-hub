package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.UserRiskProfileController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.UserRiskProfileSaveRequestDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.service.UserRiskProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserRiskProfileControllerImpl implements UserRiskProfileController {

    private final UserRiskProfileService userRiskProfileService;

    @Override
    public ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> save(UserRiskProfileSaveRequestDto request) {
        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("Request body is required", 400, null));
        }
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("clientId is required", 400, null));
        }
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("userId is required", 400, null));
        }
        request.setFromTrainingContext(true);
        try {
            UserRiskSummaryDto saved = userRiskProfileService.save(request);
            return ResponseEntity.ok(new ApiResponseDto<>("User risk profile saved", 200, saved));
        } catch (Exception e) {
            log.error("Error saving user risk profile for clientId={}, userId={}", request.getClientId(), request.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to save: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> getById(String id) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("id is required", 400, null));
        }
        UserRiskSummaryDto profile = userRiskProfileService.getById(id);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>("User risk profile not found", 404, null));
        }
        return ResponseEntity.ok(new ApiResponseDto<>("OK", 200, profile));
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> updateById(String id, UserRiskProfileSaveRequestDto request) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("id is required", 400, null));
        }
        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("Request body is required", 400, null));
        }
        UserRiskSummaryDto updated = userRiskProfileService.updateById(id, request);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>("User risk profile not found", 404, null));
        }
        return ResponseEntity.ok(new ApiResponseDto<>("User risk profile updated", 200, updated));
    }

    @Override
    public ResponseEntity<AllResponseDto<List<UserRiskSummaryDto>>> getList(String clientAdminId, String department,
                                                                            String search, int offset, int pageSize,
                                                                            String sortBy, String sortOrder) {
        UserRiskProfileService.PageResult<UserRiskSummaryDto> result =
                userRiskProfileService.getList(clientAdminId, department, search, offset, pageSize, sortBy, sortOrder);
        AllResponseDto<List<UserRiskSummaryDto>> body = new AllResponseDto<>(
                result.offset(), result.pageSize(), result.total(), result.items());
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<byte[]> exportCsv(String clientAdminId, String department, String search) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        byte[] csv = userRiskProfileService.exportCsv(clientAdminId, department, search);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"user-risk-profiles.csv\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .contentLength(csv.length)
                .body(csv);
    }

}
