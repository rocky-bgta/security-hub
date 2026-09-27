package com.aspire.asat.auth.service;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.UserMfaMethod;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.MfaDisabledException;
import com.aspire.asat.auth.exception.MfaMethodNotEnabledException;
import com.aspire.asat.auth.exception.OtpInvalidException;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.model.mfa.AuthenticatorSetupResponse;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.util.Base32Util;
import com.aspire.asat.auth.util.EncryptionUtil;
import com.aspire.asat.auth.util.TotpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for TOTP/Authenticator app MFA operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TotpService extends BaseService {

    private final MfaConfig mfaConfig;
    private final UserRepository userRepository;
    private final MfaService mfaService;

    // Temporary storage for setup secrets (in-memory, could use Redis in production)
    // Key: userId, Value: secret
    private final Map<UUID, String> tempSecrets = new ConcurrentHashMap<>();

    /**
     * Generate TOTP secret and QR code for authenticator setup
     */
    public AuthenticatorSetupResponse setupAuthenticator(UUID userId) {
        // Validate MFA is enabled
        if (!isMfaEnabled()) {
            throw new MfaDisabledException("MFA is not enabled");
        }

        // Validate authenticator method is enabled
        if (!isMethodEnabled()) {
            throw new MfaMethodNotEnabledException("Authenticator MFA method is not enabled");
        }

        // Get user
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if authenticator is already set up for this user
        if (mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR)) {
            log.warn("Authenticator is already set up for user {}. Setup is not required.", userId);
            throw new BadRequestException("Authenticator is already set up for this user. Please use verify-otp endpoint for authentication.");
        }

        // Generate random secret (20 bytes = 160 bits, standard for TOTP)
        byte[] secretBytes = new byte[20];
        new SecureRandom().nextBytes(secretBytes);
        String secret = Base32Util.encode(secretBytes);

        // Store temporarily (will be saved after verification)
        tempSecrets.put(userId, secret);

        // Generate otpauth URI
        String issuer = "ASAT Platform"; // Could be configurable
        String accountName = user.getEmail() != null ? user.getEmail() : user.getUsername();
        String otpauthUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, accountName, secret, issuer);

        // Generate QR code
        String qrCodeUrl = generateQrCodeUrl(otpauthUri);

        log.info("Generated TOTP secret for user {} (not yet saved)", userId);

        return AuthenticatorSetupResponse.builder()
                .secret(secret)
                .otpauthUri(otpauthUri)
                .qrCodeUrl(qrCodeUrl)
                .build();
    }

    /**
     * Verify TOTP code and complete authenticator setup
     * without replacing other enrolled methods.
     */
    @Transactional
    public void verifyAuthenticatorSetup(UUID userId, String code) {
        // Get temporary secret
        String secret = tempSecrets.get(userId);
        if (secret == null) {
            throw new OtpInvalidException("No authenticator setup in progress. Please start setup first.");
        }

        // Verify TOTP code
        boolean isValid = TotpUtil.verifyTotp(secret, code);
        if (!isValid) {
            throw new OtpInvalidException("Invalid TOTP code. Please try again.");
        }

        // Get user
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if authenticator is already set up (prevent re-setup)
        if (mfaService.isMethodEnrolled(user, MfaMethod.AUTHENTICATOR)) {
            log.warn("Attempt to re-setup authenticator for user {} who already has it configured", userId);
            throw new BadRequestException("Authenticator is already set up for this user. Re-setup is not allowed.");
        }

        // Encrypt and save secret
        String encryptionKey = getEncryptionKey();
        String encryptedSecret = EncryptionUtil.encrypt(secret, encryptionKey);

        mfaService.enrollMethod(user, MfaMethod.AUTHENTICATOR, encryptedSecret);

        // Remove temporary secret
        tempSecrets.remove(userId);

        log.info("Authenticator setup completed successfully for user {}", userId);
    }

    /**
     * Verify TOTP code during login against the enrolled AUTHENTICATOR method.
     */
    public boolean verifyTotpCode(UUID userId, String code) {
        // Get user
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user has authenticator MFA enabled
        UserMfaMethod authenticator = mfaService.getEnrolledMethod(user, MfaMethod.AUTHENTICATOR);
        if (authenticator == null || !StringUtils.hasText(authenticator.getSecret())) {
            throw new OtpInvalidException("Authenticator MFA is not set up for this user");
        }

        // Decrypt secret
        String encryptionKey = getEncryptionKey();
        String secret;
        try {
            secret = EncryptionUtil.decrypt(authenticator.getSecret(), encryptionKey);
        } catch (Exception e) {
            log.error("Error decrypting TOTP secret for user {}: {}", userId, e.getMessage(), e);
            throw new OtpInvalidException("Failed to verify TOTP code");
        }

        // Verify TOTP code
        return TotpUtil.verifyTotp(secret, code);
    }

    /**
     * Check if MFA is enabled globally
     */
    private boolean isMfaEnabled() {
        return Boolean.TRUE.equals(mfaConfig.getEnabled());
    }

    /**
     * Check if authenticator method is enabled
     */
    private boolean isMethodEnabled() {
        return Boolean.TRUE.equals(mfaConfig.getAuthenticatorEnabled());
    }

    /**
     * Get encryption key (normalized to 16 bytes)
     */
    private String getEncryptionKey() {
        String key = mfaConfig.getEncryptionKey();
        if (key == null || key.trim().isEmpty()) {
            // Fallback to a default key (should be set in production)
            log.warn("MFA encryption key not configured, using default (NOT SECURE FOR PRODUCTION)");
            key = "ASAT-MFA-DEFAULT-KEY-16-BYTES"; // 32 chars = 32 bytes, will be normalized to 16
        }
        return EncryptionUtil.normalizeKey(key);
    }

    /**
     * Generate QR code URL (Base64 data URL)
     * Note: This is a simple implementation. In production, use a proper QR code library like ZXing
     */
    private String generateQrCodeUrl(String otpauthUri) {
        try {
            // Simple QR code generation using a basic approach
            // In production, use a library like com.google.zxing:core and com.google.zxing:javase
            // For now, return the otpauth URI as a data URL placeholder
            // The frontend can generate the QR code using a JavaScript library

            // Placeholder: return a simple text representation
            // In production, generate actual QR code image and convert to base64
            String base64Qr = Base64.getEncoder().encodeToString(otpauthUri.getBytes());
            return "data:text/plain;base64," + base64Qr;

            // TODO: Replace with actual QR code generation using ZXing library
            // Example:
            // QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // BitMatrix bitMatrix = qrCodeWriter.encode(otpauthUri, BarcodeFormat.QR_CODE, 200, 200);
            // BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            // ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // ImageIO.write(qrImage, "PNG", baos);
            // byte[] imageBytes = baos.toByteArray();
            // return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Error generating QR code: {}", e.getMessage(), e);
            // Return otpauth URI as fallback
            return otpauthUri;
        }
    }
}
