package com.aspire.asat.auth.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encrypting/decrypting sensitive data (e.g., TOTP secrets)
 * Uses AES encryption
 */
@Slf4j
@UtilityClass
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final int KEY_SIZE = 128; // 128-bit key

    /**
     * Encrypt a string value
     *
     * @param plainText Text to encrypt
     * @param secretKey Secret key (should be 16 bytes for AES-128)
     * @return Base64 encoded encrypted string
     */
    public static String encrypt(String plainText, String secretKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error encrypting data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt a string value
     *
     * @param encryptedText Base64 encoded encrypted string
     * @param secretKey     Secret key (should be 16 bytes for AES-128)
     * @return Decrypted plain text
     */
    public static String decrypt(String encryptedText, String secretKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generate a random secret key for encryption
     * Note: In production, use a fixed key from configuration
     *
     * @return Base64 encoded secret key
     */
    public static String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Error generating secret key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate secret key", e);
        }
    }

    /**
     * Ensure secret key is exactly 16 bytes (required for AES-128)
     * Pads or truncates as needed
     *
     * @param key Input key
     * @return 16-byte key
     */
    public static String normalizeKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Encryption key cannot be null or empty");
        }

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[16];

        if (keyBytes.length >= 16) {
            System.arraycopy(keyBytes, 0, normalized, 0, 16);
        } else {
            System.arraycopy(keyBytes, 0, normalized, 0, keyBytes.length);
            // Pad with zeros if needed
            for (int i = keyBytes.length; i < 16; i++) {
                normalized[i] = 0;
            }
        }

        return new String(normalized, StandardCharsets.UTF_8);
    }
}

