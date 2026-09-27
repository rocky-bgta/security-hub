package com.aspire.asat.registration.controller.microsoft;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.config.MicrosoftEntraConfig;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.microsoft.*;
import com.aspire.asat.registration.service.microsoft.MicrosoftIntegrationService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MicrosoftIntegrationControllerImpl implements MicrosoftIntegrationController {

    private final MicrosoftIntegrationService integrationService;
    private final MicrosoftEntraConfig entraConfig;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponse<String>> getAuthorizationUrl() {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Getting auth URL for clientAdminId: {}", clientAdminId);

        String authUrl = integrationService.getAuthorizationUrl(clientAdminId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Authorization URL generated", 200, authUrl));
    }

    @Override
    public void handleCallback(String code, String state, String error, String errorDescription, HttpServletResponse response)
            throws Exception {
        log.info("Handling Microsoft OAuth callback");
        // Check for OAuth errors first
        if (error != null) {
            log.error("OAuth error received: {} - {}", error, errorDescription);
            String redirectUrl = entraConfig.getFrontendRedirectUrl() +
                    "?connected=false&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8) +
                    (errorDescription != null ? "&error_description=" + URLEncoder.encode(errorDescription, StandardCharsets.UTF_8) : "");
            response.sendRedirect(redirectUrl);
            return;
        }

        // Validate required parameters
        if (code == null || state == null) {
            log.error("Missing required OAuth parameters. Code: {}, State: {}", code != null, state != null);
            String redirectUrl = entraConfig.getFrontendRedirectUrl() +
                    "?connected=false&error=missing_parameters&error_description=Missing required OAuth parameters";
            response.sendRedirect(redirectUrl);
            return;
        }

        integrationService.handleCallback(code, state);

        // Redirect to React frontend
        String redirectUrl = entraConfig.getFrontendRedirectUrl() + "?connected=true";
        response.sendRedirect(redirectUrl);
    }

    @Override
    public ResponseEntity<ApiResponse<ConnectionStatusDto>> getConnectionStatus() {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Getting connection status for clientAdminId: {}", clientAdminId);

        ConnectionStatusDto status = integrationService.getConnectionStatus(clientAdminId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Connection status fetched", 200, status));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> disconnect() {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Disconnecting clientAdminId: {}", clientAdminId);

        integrationService.disconnect(clientAdminId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Disconnected successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponse<List<MicrosoftGroupDto>>> getGroups() {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Fetching groups for clientAdminId: {}", clientAdminId);

        List<MicrosoftGroupDto> groups = integrationService.getGroups(clientAdminId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Groups fetched successfully", 200, groups));
    }

    @Override
    public ResponseEntity<ApiResponse<List<MicrosoftUserDto>>> getGroupMembers(String groupId) {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Fetching members of group: {}", groupId);

        List<MicrosoftUserDto> members = integrationService.getGroupMembers(clientAdminId, groupId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Members fetched successfully", 200, members));
    }

    @Override
    public ResponseEntity<ApiResponse<ImportResultDto>> importUsers(ImportRequestDto request) {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Starting import for clientAdminId: {}", clientAdminId);

        ImportResultDto result = integrationService.importUsers(clientAdminId, request);

        return ResponseEntity.ok(new ApiResponse<>(
                "Import started", 200, result));
    }

    @Override
    public ResponseEntity<ApiResponse<ImportResultDto>> getImportJobStatus(String jobId) {
        log.info("Getting import job status: {}", jobId);

        ImportResultDto status = integrationService.getImportJobStatus(jobId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Job status fetched", 200, status));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ImportResultDto>>> getImportHistory() {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Getting import history for clientAdminId: {}", clientAdminId);

        List<ImportResultDto> history = integrationService.getImportHistory(clientAdminId);

        return ResponseEntity.ok(new ApiResponse<>(
                "Import history fetched", 200, history));
    }

    @Override
    public ResponseEntity<ApiResponse<ImportUsersResponseDto>> importUsersFromGroups(ImportUsersRequestDto request) {
        String clientAdminId = getClientAdminIdFromContext();
        log.info("Importing users from groups for clientAdminId: {}", clientAdminId);

        ImportUsersResponseDto result = integrationService.importUsersFromGroups(clientAdminId, request);

        return ResponseEntity.ok(new ApiResponse<>(
                "Users imported successfully", 200, result));
    }

    private String getClientAdminIdFromContext() {
        CurrentUserContext currentUser = userCurrentContextService.getCurrentUserContext();
        String userType = currentUser.getUserType();

        // If user is CLIENT_ADMIN, use their userId; otherwise use clientAdminId
        if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(userType)) {
            return currentUser.getUserId();
        }
        return currentUser.getClientAdminId();
    }
}

