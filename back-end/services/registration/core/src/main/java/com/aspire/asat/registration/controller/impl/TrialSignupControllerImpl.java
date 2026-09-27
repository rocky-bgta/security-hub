package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.registration.controller.TrialSignupController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.buynow.request.BuyNowPasswordCreationRequestDto;
import com.aspire.asat.registration.data.buynow.request.BuyNowSignupRequestDto;
import com.aspire.asat.registration.data.trial.request.AssignTrialProductsRequestDto;
import com.aspire.asat.registration.data.trial.request.EmailVerificationRequestDto;
import com.aspire.asat.registration.data.trial.request.PasswordCreationRequestDto;
import com.aspire.asat.registration.data.trial.request.TrialSignupRequestDto;
import com.aspire.asat.registration.data.trial.response.EmailVerificationResponseDto;
import com.aspire.asat.registration.data.trial.response.TrialSignupResponseDto;
import com.aspire.asat.registration.service.TrialSignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TrialSignupControllerImpl implements TrialSignupController {

    private final TrialSignupService trialSignupService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> submitAccountDetails(
            TrialSignupRequestDto request) {
        log.info("Received trial signup request for email: {}", request.getEmail());
        EmailVerificationResponseDto response = trialSignupService.submitAccountDetails(request);
        String message = hasAnyExistingTrail(response)
                ? "Trial product status checked successfully"
                : "Verification code sent successfully";
        return ResponseEntity.ok(new ApiResponseDto<>(message, 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> submitBuyNowAccountDetails(
            BuyNowSignupRequestDto request) {
        log.info("Received buy now signup request for email: {}", request.getEmail());
        EmailVerificationResponseDto response = trialSignupService.submitBuyNowAccountDetails(request);
        return ResponseEntity.ok(new ApiResponseDto<>("Verification code sent successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> verifyEmail(
            EmailVerificationRequestDto request) {
        log.info("Received email verification request for: {}", request.getEmail());
        EmailVerificationResponseDto response = trialSignupService.verifyEmail(request);
        return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.AUTH_EMAIL_VERIFIED), 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> resendVerificationCode(String email) {
        log.info("Received resend verification code request for: {}", email);
        EmailVerificationResponseDto response = trialSignupService.resendVerificationCode(email);
        return ResponseEntity.ok(new ApiResponseDto<>("Verification code resent successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TrialSignupResponseDto>> createPassword(
            PasswordCreationRequestDto request) {
        log.info("Received password creation request for: {}", request.getEmail());
        TrialSignupResponseDto response = trialSignupService.createPassword(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Trial signup completed successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TrialSignupResponseDto>> createBuyNowPassword(
            BuyNowPasswordCreationRequestDto request) {
        log.info("Received buy now password creation request for: {}", request.getEmail());
        TrialSignupResponseDto response = trialSignupService.createBuyNowPassword(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Buy now signup completed successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<TrialSignupResponseDto>> assignTrialProducts(
            AssignTrialProductsRequestDto request) {
        log.info("Received assign trial products request for: {}", request.getEmail());
        TrialSignupResponseDto response = trialSignupService.assignTrialProducts(request);
        return ResponseEntity.ok(new ApiResponseDto<>("Trial products assigned successfully", 200, response));
    }

    private boolean hasAnyExistingTrail(EmailVerificationResponseDto response) {
        if (response == null || response.getProductsData() == null) {
            return false;
        }
        return response.getProductsData().stream()
                .anyMatch(product -> Boolean.TRUE.equals(product.getExistingTrail()));
    }
}

