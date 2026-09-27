package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.AdminPasswordResetRequestDto;
import com.aspire.asat.auth.dto.ChangePasswordDto;
import com.aspire.asat.auth.dto.PasswordHistoryEntryDto;

import java.util.List;

public interface ChangePasswordService {
    void changePassword(ChangePasswordDto dto);

    /**
     * Returns the current user's password history (last N entries, newest first).
     * Only timestamps are returned; no password hashes or other sensitive data.
     *
     * @return list of password history entries (e.g. for display as "last 5 passwords")
     */
    List<PasswordHistoryEntryDto> getPasswordHistory();

    /**
     * Reset user password by Aspire Admin
     * Generates an 8-character password, updates the user's password,
     * and sends notifications to both the user and client admin
     *
     * @param requestDto contains userId of the user whose password needs to be reset
     */
    void resetPasswordByAdmin(AdminPasswordResetRequestDto requestDto);

    /**
     * Reset client admin password by MSP admin.
     * Only MSP users can call this; the target must be a CLIENT_ADMIN under the same MSP.
     *
     * @param requestDto contains userId of the client admin whose password needs to be reset
     */
    void resetClientAdminPasswordByMsp(AdminPasswordResetRequestDto requestDto);
}

