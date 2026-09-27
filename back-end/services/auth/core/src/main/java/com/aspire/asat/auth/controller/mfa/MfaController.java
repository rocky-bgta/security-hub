package com.aspire.asat.auth.controller.mfa;

import com.aspire.asat.auth.dto.TokenShortResponse;
import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.model.mfa.*;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.AccessTokenService;
import com.aspire.asat.auth.service.MfaIdentityService;
import com.aspire.asat.auth.service.MfaService;
import com.aspire.asat.auth.service.TemporaryTokenService;
import com.aspire.asat.auth.service.TotpService;
import com.aspire.asat.auth.service.impl.PasswordResetServiceImpl;
import com.aspire.asat.auth.util.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.aspire.asat.auth.constant.WebApiUrlConstants.API_BASE_URL;

/**
 * Controller for Multi-Factor Authentication (MFA) endpoints
 */
@Tag(name = "MFA", description = "Endpoints for Multi-Factor Authentication")
@RestController
@RequestMapping(value = API_BASE_URL + "/mfa", produces = "application/json")
@RequiredArgsConstructor
public class MfaController extends com.aspire.asat.auth.controller.base.BaseController {

    private final MfaService mfaService;
    private final TotpService totpService;
    private final TemporaryTokenService temporaryTokenService;
    private final AccessTokenService accessTokenService;
    private final UserRepository userRepository;
    private final PasswordResetServiceImpl passwordResetService;
    private final MfaIdentityService mfaIdentityService;

    /**
     * Resolve user from tempToken (login MFA), gateway CurrentContext, or Bearer access JWT.
     */
    private UUID getCurrentUserId(String tempToken) {
        return mfaIdentityService.resolveUserId(tempToken);
    }

    /**
     * Get enabled MFA methods globally for the system
     */
    @GetMapping("/methods")
    @Operation(summary = "Get MFA Methods", description = "Get list of MFA methods enabled globally for the system")
    public ResponseEntity<ApiResponse<MfaMethodsResponse>> getMfaMethods() {
        List<String> methods = new ArrayList<>();

        if (mfaService.isMethodEnabled(MfaMethod.EMAIL)) {
            methods.add("EMAIL");
        }
        if (mfaService.isMethodEnabled(MfaMethod.AUTHENTICATOR)) {
            methods.add("AUTHENTICATOR");
        }
        if (mfaService.isMethodEnabled(MfaMethod.SMS)) {
            methods.add("SMS");
        }
        if (mfaService.isMethodEnabled(MfaMethod.PHONE_CALL)) {
            methods.add("PHONE_CALL");
        }

        MfaMethodsResponse response = MfaMethodsResponse.builder()
                .methods(methods)
                .build();

        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), response);
    }

    /**
     * Generate OTP for SMS or EMAIL
     */
    @PostMapping("/generate-otp")
    @Operation(summary = "Generate OTP", description = "Generate OTP code for SMS or EMAIL MFA method")
    public ResponseEntity<ApiResponse<GenerateOtpResponse>> generateOtp(
            @RequestBody @Valid GenerateOtpRequest request) {
        UUID userId = getCurrentUserId(request.getTempToken());
        GenerateOtpResponse response = mfaService.generateOtp(request, userId);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), response);
    }

    /**
     * Resend OTP for SMS, EMAIL, or PHONE_CALL
     */
    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", description = "Resend OTP code for SMS, EMAIL, or PHONE_CALL MFA method.")
    public ResponseEntity<ApiResponse<GenerateOtpResponse>> resendOtp(
            @RequestBody @Valid ResendOtpRequest request) {
        UUID userId = getCurrentUserId(request.getTempToken());
        GenerateOtpResponse response = mfaService.resendOtp(request, userId);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(resolveResendOtpSuccessMessage(request.getMethod())), response);
    }

    private ResponseMessage resolveResendOtpSuccessMessage(MfaMethod method) {
        return switch (method) {
            case SMS -> ResponseMessage.MFA_OTP_RESEND_SMS_SUCCESSFUL;
            case EMAIL -> ResponseMessage.MFA_OTP_RESEND_EMAIL_SUCCESSFUL;
            case PHONE_CALL -> ResponseMessage.MFA_OTP_RESEND_PHONE_CALL_SUCCESSFUL;
            default -> throw new BadRequestException("Resend OTP is only available for SMS, EMAIL, and PHONE_CALL methods");
        };
    }

    /**
     * Verify OTP code
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP code for SMS, EMAIL, or AUTHENTICATOR MFA method. Returns access token after successful verification.")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
            @RequestBody @Valid VerifyOtpRequest request,
            HttpServletResponse response) {
        UUID userId = getCurrentUserId(request.getTempToken());
        boolean verified;
        VerifyOtpResponse verifyResponse;

        // Handle AUTHENTICATOR method separately
        if (request.getMethod() == MfaMethod.AUTHENTICATOR) {
            // AUTHENTICATOR method doesn't require sessionId
            verified = totpService.verifyTotpCode(userId, request.getCode());
            verifyResponse = VerifyOtpResponse.builder()
                    .verified(verified)
                    .message(verified ? "TOTP verified successfully" : "Invalid TOTP code")
                    .build();
        } else {
            // Handle SMS/EMAIL methods - sessionId is required for these methods
            if (request.getSessionId() == null) {
                throw new BadRequestException("Session ID is required for " + request.getMethod() + " method");
            }
            verifyResponse = mfaService.verifyOtp(request, userId);
            verified = Boolean.TRUE.equals(verifyResponse.getVerified());
        }

        // If verification successful and tempToken provided, generate access token
        if (verified && request.getTempToken() != null && !request.getTempToken().trim().isEmpty()) {
            // Get user details
            AspireUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedResourceException("User not found"));

            // Generate access token (same as login response)
            // Note: DeviceInfo is null here as it's not available in MFA flow
            // You may want to store device info during login and retrieve it here
            TokenShortResponse tokenResponse = accessTokenService.createAccessTokenResponseAfterMfa(user);

//            if (!tokenResponse.isCredentialChangeNeeded()) {
                // Revoke temporary token
             temporaryTokenService.revokeTemporaryToken(request.getTempToken());
//            }

           /* if (tokenResponse.isCredentialChangeNeeded() && tokenResponse.getTempToken() != null) {
                passwordResetService.savePasswordResetToken(user.getUsername(), tokenResponse.getTempToken());
            }*/

            // Set cookies when access token is returned (same as login flow)
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                setSignedCookies(response);
            }

            // Add token response to verification response
            verifyResponse.setTokenResponse(tokenResponse);
        }

        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), verifyResponse);
    }

    /**
     * Setup authenticator app (generate QR code)
     */
    @PostMapping("/authenticator/setup")
    @Operation(summary = "Setup Authenticator", description = "Generate TOTP secret and QR code for authenticator app setup")
    public ResponseEntity<ApiResponse<AuthenticatorSetupResponse>> setupAuthenticator(
            @RequestBody(required = false) AuthenticatorSetupRequest request) {
        // Extract tempToken from request if provided
        String tempToken = request != null ? request.getTempToken() : null;
        UUID userId = getCurrentUserId(tempToken);
        AuthenticatorSetupResponse response = totpService.setupAuthenticator(userId);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), response);
    }

    /**
     * Verify authenticator setup and complete registration
     */
    @PostMapping("/authenticator/verify-setup")
    @Operation(summary = "Verify Authenticator Setup", description = "Verify TOTP code and complete authenticator app setup. Returns access token after successful verification if tempToken is provided.")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyAuthenticatorSetup(
            @RequestBody @Valid VerifyAuthenticatorSetupRequest request,
            HttpServletResponse response) {
        UUID userId = getCurrentUserId(request.getTempToken());

        // Verify authenticator setup
        totpService.verifyAuthenticatorSetup(userId, request.getCode());

        // Build verification response
        VerifyOtpResponse verifyResponse = VerifyOtpResponse.builder()
                .verified(true)
                .message("Authenticator setup verified successfully")
                .build();

        // If tempToken provided, generate access token response
        if (request.getTempToken() != null && !request.getTempToken().trim().isEmpty()) {
            // Get user details
            AspireUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedResourceException("User not found"));

            // Generate access token (same as login response)
            TokenShortResponse tokenResponse = accessTokenService.createAccessTokenResponseAfterMfa(user);

            // Revoke temporary token
            temporaryTokenService.revokeTemporaryToken(request.getTempToken());

            // Set cookies when access token is returned (same as login flow)
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                setSignedCookies(response);
            }

            // Add token response to verification response
            verifyResponse.setTokenResponse(tokenResponse);
        }

        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), verifyResponse);
    }

    /**
     * List MFA methods enrolled for the authenticated user. JWT required (not public).
     */
    @GetMapping("/user-methods")
    @Operation(summary = "List enrolled MFA methods", description = "List MFA methods set up for the current user, including which is the default")
    public ResponseEntity<ApiResponse<UserMfaMethodsResponse>> getUserMfaMethods() {
        UUID userId = getCurrentUserId(null);
        UserMfaMethodsResponse response = mfaService.listUserMethods(userId);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), response);
    }

    /**
     * Set the default MFA method used by the frontend to auto-select at verification.
     * JWT required (not public). Does not prevent verifying with another enrolled method.
     */
    @PutMapping("/default-method")
    @Operation(summary = "Set default MFA method", description = "Set which enrolled MFA method the frontend should auto-select for verification")
    public ResponseEntity<ApiResponse<UserMfaMethodsResponse>> setDefaultMfaMethod(
            @RequestBody @Valid SetDefaultMfaMethodRequest request) {
        UUID userId = getCurrentUserId(null);
        mfaService.setDefaultMethod(userId, request.getMethod());
        UserMfaMethodsResponse response = mfaService.listUserMethods(userId);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), response);
    }

    /**
     * Remove an enrolled MFA method. JWT required (not public). Last method cannot be removed.
     */
    @DeleteMapping("/user-methods/{method}")
    @Operation(summary = "Remove MFA method", description = "Remove an enrolled MFA method. The last remaining method cannot be removed.")
    public ResponseEntity<ApiResponse<UserMfaMethodsResponse>> removeMfaMethod(
            @PathVariable MfaMethod method) {
        UUID userId = getCurrentUserId(null);
        mfaService.removeMethod(userId, method);
        UserMfaMethodsResponse response = mfaService.listUserMethods(userId);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), response);
    }
}

