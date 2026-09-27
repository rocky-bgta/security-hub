package com.aspire.asat.registration.service.microsoft;

import com.aspire.asat.registration.data.microsoft.*;

import java.util.List;

public interface MicrosoftIntegrationService {

    String getAuthorizationUrl(String clientAdminId);

    ConnectionStatusDto handleCallback(String code, String state);

    ConnectionStatusDto getConnectionStatus(String clientAdminId);

    void disconnect(String clientAdminId);

    List<MicrosoftGroupDto> getGroups(String clientAdminId);

    List<MicrosoftUserDto> getGroupMembers(String clientAdminId, String groupId);

    ImportResultDto importUsers(String clientAdminId, ImportRequestDto request);

    ImportResultDto getImportJobStatus(String jobId);

    List<ImportResultDto> getImportHistory(String clientAdminId);

    /**
     * Import users from Microsoft groups, create AspireUser records, and send welcome emails
     *
     * @param clientAdminId Client admin ID from current user context
     * @param request Request containing group IDs
     * @return Import result with statistics
     */
    ImportUsersResponseDto importUsersFromGroups(String clientAdminId, ImportUsersRequestDto request);
}

