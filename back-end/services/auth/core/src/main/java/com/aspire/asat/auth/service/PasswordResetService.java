package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.PasswordResetRequestDto;
import com.aspire.asat.auth.dto.ResetPasswordDto;
import com.aspire.asat.auth.entity.password.PasswordResetToken;

public interface PasswordResetService {
    void requestPasswordReset(PasswordResetRequestDto requestDto);

    void resetPassword(ResetPasswordDto resetPasswordDto);

    /**
     * Generate password reset token without sending notification
     *
     * @param requestDto password reset request containing username
     * @return the generated password reset token
     */
    PasswordResetToken generatePasswordResetToken(PasswordResetRequestDto requestDto);
}

