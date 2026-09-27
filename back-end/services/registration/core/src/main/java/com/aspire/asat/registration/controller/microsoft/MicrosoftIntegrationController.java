package com.aspire.asat.registration.controller.microsoft;


import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.microsoft.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Microsoft Entra Integration")
@RequestMapping(value = "/api/v1/microsoft", produces = "application/json")
public interface MicrosoftIntegrationController {

    @Operation(summary = "Get authorization URL to connect Microsoft Entra")
    @GetMapping("/auth-url")
    ResponseEntity<ApiResponse<String>> getAuthorizationUrl();

    @Operation(summary = "Handle OAuth callback from Microsoft")
    @GetMapping("/callback")
    void handleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletResponse response) throws Exception;

    @Operation(summary = "Get connection status")
    @GetMapping("/status")
    ResponseEntity<ApiResponse<ConnectionStatusDto>> getConnectionStatus();

    @Operation(summary = "Disconnect from Microsoft Entra")
    @DeleteMapping("/disconnect")
    ResponseEntity<ApiResponse<Void>> disconnect();

    @Operation(summary = "Get all groups from Microsoft Entra")
    @GetMapping("/groups")
    ResponseEntity<ApiResponse<List<MicrosoftGroupDto>>> getGroups();

    @Operation(summary = "Get members of a group")
    @GetMapping("/groups/{groupId}/members")
    ResponseEntity<ApiResponse<List<MicrosoftUserDto>>> getGroupMembers(
            @PathVariable("groupId") String groupId);

    @Operation(summary = "Import users from selected groups")
    @PostMapping("/import")
    ResponseEntity<ApiResponse<ImportResultDto>> importUsers(
            @Valid @RequestBody ImportRequestDto request);

    @Operation(summary = "Get import job status")
    @GetMapping("/import/{jobId}/status")
    ResponseEntity<ApiResponse<ImportResultDto>> getImportJobStatus(
            @PathVariable("jobId") String jobId);

    @Operation(summary = "Get import history")
    @GetMapping("/import/history")
    ResponseEntity<ApiResponse<List<ImportResultDto>>> getImportHistory();

    @Operation(summary = "Import users from Microsoft groups and create AspireUser records", 
            description = "Fetches users from specified Microsoft groups, creates AspireUser records with MICROSOFT_ENTRA source and ACTIVE status, and sends welcome email notifications")
    @PostMapping("/import-users")
    ResponseEntity<ApiResponse<ImportUsersResponseDto>> importUsersFromGroups(
            @Valid @RequestBody ImportUsersRequestDto request);
}

