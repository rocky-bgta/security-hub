package com.aspire.asat.auth.controller.password;

import com.aspire.asat.auth.controller.base.BaseController;
import com.aspire.asat.auth.dto.AdminPasswordResetRequestDto;
import com.aspire.asat.auth.dto.ChangePasswordDto;
import com.aspire.asat.auth.dto.PasswordHistoryEntryDto;
import com.aspire.asat.auth.dto.PasswordResetRequestDto;
import com.aspire.asat.auth.dto.ResetPasswordDto;
import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import com.aspire.asat.auth.entity.password.PasswordResetToken;
import com.aspire.asat.auth.service.ChangePasswordService;
import com.aspire.asat.auth.service.PasswordResetService;
import com.aspire.asat.auth.util.ResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.aspire.asat.common.message.MessageKeys;
import static com.aspire.asat.auth.dto.enums.ResponseMessage.OPERATION_SUCCESSFUL;

@RestController
@RequiredArgsConstructor
public class PasswordControllerImpl extends BaseController implements PasswordController {

    private final PasswordResetService passwordResetService;
    private final ChangePasswordService changePasswordService;

    @Override
    public ResponseEntity<ApiResponse<Boolean>> requestPasswordReset(@Valid PasswordResetRequestDto requestDto) {
        passwordResetService.requestPasswordReset(requestDto);
        return ResponseUtils.createSuccessResponseObject(getMessage(OPERATION_SUCCESSFUL), true);
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(@Valid ResetPasswordDto resetPasswordDto) {
        passwordResetService.resetPassword(resetPasswordDto);
        return ResponseUtils.createSuccessResponseObject(getMessage(OPERATION_SUCCESSFUL), true);
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> changePassword(@Valid ChangePasswordDto changePasswordDto) {
        changePasswordService.changePassword(changePasswordDto);
        return ResponseUtils.createSuccessResponseObject(messageService.get(MessageKeys.AUTH_PASSWORD_UPDATED), true);
    }

    @Override
    public ResponseEntity<ApiResponse<List<PasswordHistoryEntryDto>>> getPasswordHistory() {
        List<PasswordHistoryEntryDto> history = changePasswordService.getPasswordHistory();
        return ResponseUtils.createSuccessResponseObject(getMessage(OPERATION_SUCCESSFUL), history);
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> resetPasswordByAdmin(@Valid AdminPasswordResetRequestDto requestDto) {
        changePasswordService.resetPasswordByAdmin(requestDto);
        return ResponseUtils.createSuccessResponseObject(getMessage(OPERATION_SUCCESSFUL), true);
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> resetClientAdminPasswordByMsp(@Valid AdminPasswordResetRequestDto requestDto) {
        changePasswordService.resetClientAdminPasswordByMsp(requestDto);
        return ResponseUtils.createSuccessResponseObject(getMessage(OPERATION_SUCCESSFUL), true);
    }

    @Override
    public ResponseEntity<ApiResponse<PasswordResetToken>> generatePasswordResetToken(@Valid PasswordResetRequestDto requestDto) {
        PasswordResetToken resetToken = passwordResetService.generatePasswordResetToken(requestDto);
        return ResponseUtils.createSuccessResponseObject(getMessage(OPERATION_SUCCESSFUL), resetToken);
    }
}
