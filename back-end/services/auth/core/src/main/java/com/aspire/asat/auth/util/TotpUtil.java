package com.aspire.asat.auth.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * TOTP (Time-based One-Time Password) utility class
 * Implements RFC 6238 compatible TOTP generation and verification
 */
@Slf4j
@UtilityClass
public class TotpUtil {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP_SECONDS = 30; // Standard TOTP time step
    private static final int CODE_DIGITS = 6; // Standard TOTP code length

    /**
     * Generate TOTP code for a given secret and time
     *
     * @param secret Base32 encoded secret key
     * @param time   Unix timestamp in seconds
     * @return 6-digit TOTP code
     */
    public static String generateTotp(String secret, long time) {
        try {
            byte[] key = Base32Util.decode(secret);
            long timeStep = time / TIME_STEP_SECONDS;
            return generateTotpCode(key, timeStep);
        } catch (Exception e) {
            log.error("Error generating TOTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate TOTP", e);
        }
    }

    /**
     * Generate TOTP code for current time
     *
     * @param secret Base32 encoded secret key
     * @return 6-digit TOTP code
     */
    public static String generateTotp(String secret) {
        return generateTotp(secret, System.currentTimeMillis() / 1000);
    }

    /**
     * Verify TOTP code with ±1 time window tolerance
     *
     * @param secret Base32 encoded secret key
     * @param code   TOTP code to verify
     * @return true if code is valid
     */
    public static boolean verifyTotp(String secret, String code) {
        long currentTime = System.currentTimeMillis() / 1000;
        
        // Check current time window
        if (verifyTotpAtTime(secret, code, currentTime)) {
            return true;
        }
        
        // Check previous time window (-1)
        if (verifyTotpAtTime(secret, code, currentTime - TIME_STEP_SECONDS)) {
            return true;
        }
        
        // Check next time window (+1)
        if (verifyTotpAtTime(secret, code, currentTime + TIME_STEP_SECONDS)) {
            return true;
        }
        
        return false;
    }

    /**
     * Verify TOTP code at a specific time
     */
    private static boolean verifyTotpAtTime(String secret, String code, long time) {
        try {
            String expectedCode = generateTotp(secret, time);
            return expectedCode.equals(code);
        } catch (Exception e) {
            log.error("Error verifying TOTP: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate TOTP code from key and time step
     */
    private static String generateTotpCode(byte[] key, long timeStep) throws NoSuchAlgorithmException, InvalidKeyException {
        // Convert time step to byte array (8 bytes, big-endian)
        byte[] timeStepBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

        // Calculate HMAC-SHA1
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hmac = mac.doFinal(timeStepBytes);

        // Dynamic truncation (RFC 6238)
        int offset = hmac[hmac.length - 1] & 0x0F;
        int binary = ((hmac[offset] & 0x7F) << 24) |
                     ((hmac[offset + 1] & 0xFF) << 16) |
                     ((hmac[offset + 2] & 0xFF) << 8) |
                     (hmac[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_DIGITS);
        return String.format("%0" + CODE_DIGITS + "d", otp);
    }
}

