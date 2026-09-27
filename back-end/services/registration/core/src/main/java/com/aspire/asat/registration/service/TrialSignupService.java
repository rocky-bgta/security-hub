package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.buynow.request.BuyNowPasswordCreationRequestDto;
import com.aspire.asat.registration.data.buynow.request.BuyNowSignupRequestDto;
import com.aspire.asat.registration.data.trial.request.AssignTrialProductsRequestDto;
import com.aspire.asat.registration.data.trial.request.EmailVerificationRequestDto;
import com.aspire.asat.registration.data.trial.request.PasswordCreationRequestDto;
import com.aspire.asat.registration.data.trial.request.TrialSignupRequestDto;
import com.aspire.asat.registration.data.trial.response.EmailVerificationResponseDto;
import com.aspire.asat.registration.data.trial.response.TrialSignupResponseDto;

public interface TrialSignupService {
    
    /**
     * Step 1: Submit account details and send verification code
     */
    EmailVerificationResponseDto submitAccountDetails(TrialSignupRequestDto request);
    
    /**
     * Step 1 (Buy Now): Submit account details for buy now (firstName and lastName will be empty)
     */
    EmailVerificationResponseDto submitBuyNowAccountDetails(BuyNowSignupRequestDto request);
    
    /**
     * Step 2: Verify email with OTP code (used for both trial and buy now)
     */
    EmailVerificationResponseDto verifyEmail(EmailVerificationRequestDto request);
    
    /**
     * Step 3: Create password and complete signup
     */
    TrialSignupResponseDto createPassword(PasswordCreationRequestDto request);
    
    /**
     * Step 3 (Buy Now): Create password and complete buy now signup (no product assignment)
     */
    TrialSignupResponseDto createBuyNowPassword(BuyNowPasswordCreationRequestDto request);
    
    /**
     * Resend verification code (used for both trial and buy now)
     */
    EmailVerificationResponseDto resendVerificationCode(String email);

    /**
     * Assign additional trial products to an existing trial account (no password / OTP).
     */
    TrialSignupResponseDto assignTrialProducts(AssignTrialProductsRequestDto request);
}

