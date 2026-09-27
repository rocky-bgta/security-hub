package com.aspire.asat.phishing.service.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encrypts and decrypts sensitive credentials at rest using AES-256-GCM.
 * Falls back to legacy Base64 values for backward compatibility on decrypt.
 */
@Slf4j
@Service
public class CredentialEncryptionService {

    private static final String PREFIX = "ENC:";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CredentialEncryptionService(
            @Value("${phishing.credentials.encryption-key:}") String encryptionKeyBase64) {
        if (encryptionKeyBase64 != null && !encryptionKeyBase64.isBlank()) {
            byte[] keyBytes = Base64.getDecoder().decode(encryptionKeyBase64.trim());
            if (keyBytes.length != 32) {
                throw new IllegalStateException(
                        "PHISHING_CREDENTIALS_ENCRYPTION_KEY must decode to 32 bytes for AES-256");
            }
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } else {
            log.warn("phishing.credentials.encryption-key not set; generating ephemeral key (dev only)");
            byte[] keyBytes = new byte[32];
            secureRandom.nextBytes(keyBytes);
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        }
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt credential", e);
        }
    }

    public String decrypt(String stored) {
        if (stored == null || stored.isEmpty()) {
            return stored;
        }
        if (stored.startsWith(PREFIX)) {
            try {
                byte[] payload = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
                ByteBuffer buffer = ByteBuffer.wrap(payload);
                byte[] iv = new byte[GCM_IV_LENGTH];
                buffer.get(iv);
                byte[] ciphertext = new byte[buffer.remaining()];
                buffer.get(ciphertext);

                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
                byte[] plaintext = cipher.doFinal(ciphertext);
                return new String(plaintext, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Failed to decrypt credential", e);
                throw new IllegalStateException("Failed to decrypt credential", e);
            }
        }
        // Legacy Base64-encoded passwords
        try {
            return new String(Base64.getDecoder().decode(stored), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return stored;
        }
    }

    public boolean isEncrypted(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }
}
