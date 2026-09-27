package com.aspire.asat.auth.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

/**
 * Utility class for generating OTP codes
 */
@UtilityClass
public class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a random numeric OTP code
     *
     * @param length Length of the OTP code (typically 6)
     * @return Generated OTP code as string
     */
    public static String generateOtp(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("OTP length must be between 1 and 10");
        }

        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int otp = RANDOM.nextInt(max - min + 1) + min;

        return String.valueOf(otp);
    }

    /**
     * Generate a 6-digit OTP code (default)
     *
     * @return 6-digit OTP code as string
     */
    public static String generateOtp() {
        return generateOtp(6);
    }
}

