package com.aspire.asat.auth.controller.password;

import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import com.aspire.asat.auth.dto.AdminPasswordResetRequestDto;
import com.aspire.asat.auth.dto.PasswordHistoryEntryDto;
import com.aspire.asat.auth.dto.PasswordResetRequestDto;
import com.aspire.asat.auth.dto.ResetPasswordDto;
import com.aspire.asat.auth.dto.ChangePasswordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static com.aspire.asat.auth.constant.WebApiUrlConstants.API_BASE_URL;

@Tag(name = "Password", description = "Endpoints for password reset flow")
public interface PasswordController {

    @PostMapping(API_BASE_URL + "/password/request-reset")
    @Operation(summary = "Request password reset", description = "Send reset password link to user's email")
    ResponseEntity<ApiResponse<Boolean>> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDto requestDto);

    @PostMapping(API_BASE_URL + "/password/reset")
    @Operation(summary = "Reset password", description = "Reset user's password using token")
    ResponseEntity<ApiResponse<Boolean>> resetPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto);

    @PostMapping(API_BASE_URL + "/password/change")
    @Operation(summary = "Change password", description = "Change current user's password using current password and new password")
    ResponseEntity<ApiResponse<Boolean>> changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto);

    @GetMapping(API_BASE_URL + "/password/history")
    @Operation(summary = "Get password history", description = "Returns the current user's last N password change timestamps (for display when changing/resetting password). No password data is returned.")
    ResponseEntity<ApiResponse<List<PasswordHistoryEntryDto>>> getPasswordHistory();

    @PostMapping(API_BASE_URL + "/password/admin-reset")
    @Operation(summary = "Reset password by admin", description = "Reset user's password by Aspire Admin. Generates an 8-character password and sends it to the user via email. Also sends in-app notification to client admin.")
    ResponseEntity<ApiResponse<Boolean>> resetPasswordByAdmin(@RequestBody @Valid AdminPasswordResetRequestDto requestDto);

    @PostMapping(API_BASE_URL + "/password/msp-reset-client-admin")
    @Operation(summary = "Reset client admin password by MSP admin", description = "Allows an MSP admin to reset the password of a client admin under their MSP. Generates an 8-character password and emails it to the client admin.")
    ResponseEntity<ApiResponse<Boolean>> resetClientAdminPasswordByMsp(@RequestBody @Valid AdminPasswordResetRequestDto requestDto);

    @PostMapping(API_BASE_URL + "/password/generate-token")
    @Operation(summary = "Generate password reset token", description = "Generate a password reset token for the user without sending email notification. Returns the password reset token object.")
    ResponseEntity<ApiResponse<com.aspire.asat.auth.entity.password.PasswordResetToken>> generatePasswordResetToken(@RequestBody @Valid PasswordResetRequestDto requestDto);
}
