package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.buynow.request.BuyNowPasswordCreationRequestDto;
import com.aspire.asat.registration.data.buynow.request.BuyNowSignupRequestDto;
import com.aspire.asat.registration.data.trial.request.AssignTrialProductsRequestDto;
import com.aspire.asat.registration.data.trial.request.EmailVerificationRequestDto;
import com.aspire.asat.registration.data.trial.request.PasswordCreationRequestDto;
import com.aspire.asat.registration.data.trial.request.TrialSignupRequestDto;
import com.aspire.asat.registration.data.trial.response.EmailVerificationResponseDto;
import com.aspire.asat.registration.data.trial.response.TrialSignupResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trial Signup", description = "Endpoints for 30-day free trial signup flow")
@RequestMapping(value = "/api/v1/trial", produces = "application/json")
public interface TrialSignupController {

    @Operation(summary = "Step 1: Submit account details", 
               description = "Submit account details and selected trial products. Sends an email verification code only when none of the selected products already have a trial for this email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/signup")
    ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> submitAccountDetails(
            @Valid @RequestBody TrialSignupRequestDto request);

    @Operation(summary = "Step 1 (Buy Now): Submit account details for buy now", 
               description = "Submit account details for buy now option. First name and last name will be empty.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/buy-now/signup")
    ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> submitBuyNowAccountDetails(
            @Valid @RequestBody BuyNowSignupRequestDto request);

    @Operation(summary = "Step 3 (Buy Now): Create password and complete signup", 
               description = "Create password and complete buy now signup. Only creates Client Admin and AspireUser, no product assignment.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buy now signup completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or passwords don't match"),
            @ApiResponse(responseCode = "404", description = "Signup data not found or email not verified"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/buy-now/create-password")
    ResponseEntity<ApiResponseDto<TrialSignupResponseDto>> createBuyNowPassword(
            @Valid @RequestBody BuyNowPasswordCreationRequestDto request);

    @Operation(summary = "Step 2: Verify email", 
               description = "Verify email address with 6-digit verification code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification code"),
            @ApiResponse(responseCode = "404", description = "Verification code not found or expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/verify-email")
    ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequestDto request);

    @Operation(summary = "Resend verification code", 
               description = "Resend verification code to email address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification code resent successfully"),
            @ApiResponse(responseCode = "404", description = "No signup data found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/resend-code")
    ResponseEntity<ApiResponseDto<EmailVerificationResponseDto>> resendVerificationCode(
            @RequestParam @NotBlank @Email String email);

    @Operation(summary = "Step 3: Create password and complete signup", 
               description = "Create password and complete trial signup for a new account. Assigns all products in productsData. Rejects emails that already have a trial account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trial signup completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input, passwords don't match, or account already exists"),
            @ApiResponse(responseCode = "404", description = "Signup data not found or email not verified"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create-password")
    ResponseEntity<ApiResponseDto<TrialSignupResponseDto>> createPassword(
            @Valid @RequestBody PasswordCreationRequestDto request);

    @Operation(summary = "Assign additional trial products",
               description = "Assign new trial products/packages to an existing trial account. Does not create a user or password. No OTP required.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trial products assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or all selected products already have a trial"),
            @ApiResponse(responseCode = "404", description = "Trial account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/assign-products")
    ResponseEntity<ApiResponseDto<TrialSignupResponseDto>> assignTrialProducts(
            @Valid @RequestBody AssignTrialProductsRequestDto request);
}

