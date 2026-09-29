package com.aspire.asat.auth.service;

import com.aspire.asat.auth.config.MfaConfig;
import com.aspire.asat.auth.constant.OtpMessages;
import com.aspire.asat.auth.dto.enums.MfaAuditEventType;
import com.aspire.asat.auth.dto.enums.MfaCodeStatus;
import com.aspire.asat.auth.dto.enums.MfaMethod;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.MfaCode;
import com.aspire.asat.auth.entity.UserMfaMethod;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.MfaDisabledException;
import com.aspire.asat.auth.exception.MfaMethodNotEnabledException;
import com.aspire.asat.auth.exception.OtpCooldownException;
import com.aspire.asat.auth.exception.OtpExpiredException;
import com.aspire.asat.auth.exception.OtpInvalidException;
import com.aspire.asat.auth.exception.OtpLockedException;
import com.aspire.asat.auth.exception.OtpRateLimitException;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.dto.UserMfaMethodItem;
import com.aspire.asat.auth.model.mfa.GenerateOtpRequest;
import com.aspire.asat.auth.model.mfa.GenerateOtpResponse;
import com.aspire.asat.auth.model.mfa.OtpIssueDecision;
import com.aspire.asat.auth.model.mfa.ResendOtpRequest;
import com.aspire.asat.auth.model.mfa.UserMfaMethodsResponse;
import com.aspire.asat.auth.model.mfa.VerifyOtpRequest;
import com.aspire.asat.auth.model.mfa.VerifyOtpResponse;
import com.aspire.asat.auth.repository.MfaCodeRepository;
import com.aspire.asat.auth.repository.UserMfaMethodRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.otp.MfaAuditService;
import com.aspire.asat.auth.service.otp.OtpSecurityService;
import com.aspire.asat.auth.util.OtpGenerator;
import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationPriority;
import com.aspire.asat.common.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main MFA service handling OTP generation, verification, and enrolled-method management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService extends BaseService {

    private final MfaConfig mfaConfig;
    private final MfaCodeRepository mfaCodeRepository;
    private final UserRepository userRepository;
    private final UserMfaMethodRepository userMfaMethodRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationClient notificationClient;
    private final OtpSecurityService otpSecurityService;
    private final MfaAuditService mfaAuditService;

    @Value("${mfa.otp.console-enabled:false}")
    private boolean otpConsoleEnabled;

    /**
     * Check if MFA is enabled globally
     */
    public boolean isMfaEnabled() {
        return Boolean.TRUE.equals(mfaConfig.getEnabled());
    }

    /**
     * Check if a specific MFA method is enabled
     */
    public boolean isMethodEnabled(MfaMethod method) {
        if (!isMfaEnabled()) {
            return false;
        }

        return switch (method) {
            case SMS -> Boolean.TRUE.equals(mfaConfig.getSmsEnabled());
            case EMAIL -> Boolean.TRUE.equals(mfaConfig.getEmailEnabled());
            case AUTHENTICATOR -> Boolean.TRUE.equals(mfaConfig.getAuthenticatorEnabled());
            case PHONE_CALL -> Boolean.TRUE.equals(mfaConfig.getPhoneCallEnabled());
        };
    }

    /**
     * Generate OTP for SMS, EMAIL, or PHONE_CALL
     */
    @Transactional
    public GenerateOtpResponse generateOtp(GenerateOtpRequest request, UUID userId) {
        AspireUser user = validateOtpRequest(request.getMethod(), userId, request.getTempToken());
        String resolvedPhoneNumber = requirePhoneIfNeeded(
                request.getMethod(), request.getPhoneNumber(), user, isLoginChallenge(request.getTempToken()));
        OtpIssueDecision decision = authorizeIssue(userId, request.getMethod(), request.getTempToken(), false);
        return issueOtp(user, userId, request.getMethod(), resolvedPhoneNumber, decision, false);
    }

    /**
     * Resend OTP for SMS, EMAIL, or PHONE_CALL
     * Only applicable for EMAIL, SMS, and PHONE_CALL methods (not AUTHENTICATOR)
     */
    @Transactional
    public GenerateOtpResponse resendOtp(ResendOtpRequest request, UUID userId) {
        if (request.getMethod() != MfaMethod.SMS && request.getMethod() != MfaMethod.EMAIL && request.getMethod() != MfaMethod.PHONE_CALL) {
            throw new BadRequestException("Resend OTP is only available for SMS, EMAIL, and PHONE_CALL methods");
        }

        AspireUser user = validateOtpRequest(request.getMethod(), userId, request.getTempToken());
        String resolvedPhoneNumber = requirePhoneIfNeeded(
                request.getMethod(), request.getPhoneNumber(), user, isLoginChallenge(request.getTempToken()));
        OtpIssueDecision decision = authorizeIssue(userId, request.getMethod(), request.getTempToken(), true);
        return issueOtp(user, userId, request.getMethod(), resolvedPhoneNumber, decision, true);
    }

    /**
     * Verify OTP code.
     * Enrolls the method on first successful setup/add; does not overwrite other enrolled methods.
     */
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request, UUID userId) {
        if (!isMfaEnabled()) {
            throw new MfaDisabledException("MFA is not enabled");
        }

        String ip = getRemoteIPAddress();
        try {
            otpSecurityService.assertVerifyAllowed(userId, ip);
        } catch (OtpLockedException | OtpRateLimitException ex) {
            auditDenied(ex instanceof OtpLockedException
                            ? MfaAuditEventType.OTP_LOCKED
                            : MfaAuditEventType.OTP_RATE_LIMITED,
                    userId, request.getMethod().name(), request.getSessionId(), ip, ex.getMessage());
            throw ex;
        }

        Instant now = Instant.now();
        MfaCode mfaCode = mfaCodeRepository
                .findByUserIdAndSessionIdAndIsUsedFalseAndExpiresAtAfter(userId, request.getSessionId(), now)
                .orElseGet(() -> mfaCodeRepository
                        .findByUserIdAndSessionIdAndIsUsedFalse(userId, request.getSessionId())
                        .orElseThrow(() -> new OtpInvalidException(OtpMessages.INVALID)));

        if (mfaCode.getExpiresAt().isBefore(now)) {
            mfaCode.setStatus(MfaCodeStatus.EXPIRED.name());
            mfaCodeRepository.save(mfaCode);
            mfaAuditService.record(MfaAuditEventType.OTP_EXPIRED_REJECTED, userId, request.getMethod().name(),
                    request.getSessionId(), ip, MfaAuditService.OUTCOME_DENIED, "expired", Map.of());
            throw new OtpExpiredException(OtpMessages.EXPIRED);
        }

        if (mfaCode.getAttemptCount() >= mfaConfig.getMaxAttempts()) {
            otpSecurityService.lockUser(userId);
            throw locked(userId, request.getMethod().name(), request.getSessionId(), ip);
        }

        boolean isValid = passwordEncoder.matches(request.getCode(), mfaCode.getCodeHash());

        if (!isValid) {
            mfaCode.setAttemptCount(mfaCode.getAttemptCount() + 1);
            mfaCodeRepository.save(mfaCode);

            log.warn("Invalid OTP attempt for user {} (attempt {}/{})", userId, mfaCode.getAttemptCount(), mfaConfig.getMaxAttempts());
            mfaAuditService.record(MfaAuditEventType.OTP_VERIFY_FAILED, userId, request.getMethod().name(),
                    request.getSessionId(), ip, MfaAuditService.OUTCOME_DENIED, "invalid",
                    Map.of("attemptCount", mfaCode.getAttemptCount()));

            if (mfaCode.getAttemptCount() >= mfaConfig.getMaxAttempts()) {
                invalidateCode(mfaCode);
                mfaCodeRepository.save(mfaCode);
                otpSecurityService.lockUser(userId);
                throw locked(userId, request.getMethod().name(), request.getSessionId(), ip);
            }

            throw new OtpInvalidException(OtpMessages.INVALID);
        }

        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getMethod() == MfaMethod.SMS || request.getMethod() == MfaMethod.EMAIL || request.getMethod() == MfaMethod.PHONE_CALL) {
            enrollIfAllowed(user, request.getMethod(), null, request.getTempToken(), mfaCode.getPhoneNumber());
        }

        mfaCode.setIsUsed(true);
        mfaCode.setStatus(MfaCodeStatus.USED.name());
        mfaCodeRepository.save(mfaCode);
        otpSecurityService.recordSuccessfulVerify(userId, request.getMethod());
        mfaAuditService.record(MfaAuditEventType.OTP_VERIFY_SUCCESS, userId, request.getMethod().name(),
                request.getSessionId(), ip, MfaAuditService.OUTCOME_SUCCESS, null, Map.of());

        log.info("OTP verified successfully for user {} with session {}", userId, request.getSessionId());

        return VerifyOtpResponse.builder()
                .verified(true)
                .message("OTP verified successfully")
                .build();
    }

    private AspireUser validateOtpRequest(MfaMethod method, UUID userId, String tempToken) {
        if (!isMfaEnabled()) {
            throw new MfaDisabledException("MFA is not enabled");
        }
        if (!isMethodEnabled(method)) {
            throw new MfaMethodNotEnabledException(
                    String.format("MFA method %s is not enabled", method));
        }
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        assertMethodAllowedForOtp(user, method, tempToken);
        return user;
    }

    private String requirePhoneIfNeeded(MfaMethod method, String requestPhoneNumber, AspireUser user, boolean loginChallenge) {
        String resolvedPhoneNumber = resolvePhoneNumber(method, requestPhoneNumber, user, loginChallenge);
        if ((method == MfaMethod.SMS || method == MfaMethod.PHONE_CALL)
                && ObjectUtils.isEmpty(resolvedPhoneNumber)) {
            throw new BadRequestException(
                    "Phone number is required for " + method
                            + " method and was not found on the user profile");
        }
        return resolvedPhoneNumber;
    }

    private OtpIssueDecision authorizeIssue(UUID userId, MfaMethod method, String tempToken, boolean forceResend) {
        String ip = getRemoteIPAddress();
        String sessionKey = OtpSecurityService.sessionKey(tempToken, userId);
        try {
            return otpSecurityService.authorizeIssue(userId, method, ip, sessionKey, forceResend);
        } catch (OtpLockedException ex) {
            auditDenied(MfaAuditEventType.OTP_LOCKED, userId, method.name(), null, ip, ex.getMessage());
            throw ex;
        } catch (OtpCooldownException ex) {
            auditDenied(MfaAuditEventType.OTP_COOLDOWN_BLOCKED, userId, method.name(), null, ip, ex.getMessage());
            throw ex;
        } catch (OtpRateLimitException ex) {
            auditDenied(MfaAuditEventType.OTP_RATE_LIMITED, userId, method.name(), null, ip, ex.getMessage());
            throw ex;
        }
    }

    private GenerateOtpResponse issueOtp(AspireUser user, UUID userId, MfaMethod method, String resolvedPhoneNumber,
                                         OtpIssueDecision decision, boolean resend) {
        String ip = getRemoteIPAddress();
        int invalidated = invalidateUnusedOtps(userId, method.name());

        String otp = OtpGenerator.generateOtp(mfaConfig.getOtpLength());
        log.info("{} OTP for user {}: {} (length: {})",
                resend ? "Resent" : "Generated", userId, "****", otp.length());

        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();
        MfaCode mfaCode = MfaCode.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .codeHash(passwordEncoder.encode(otp))
                .expiresAt(now.plusSeconds(mfaConfig.getOtpValiditySeconds()))
                .isUsed(false)
                .sessionId(sessionId)
                .generatedBy(method.name())
                .attemptCount(0)
                .createdAt(now)
                .phoneNumber(resolvedPhoneNumber)
                .status(MfaCodeStatus.ACTIVE.name())
                .build();
        mfaCodeRepository.save(mfaCode);
        otpSecurityService.recordActiveSession(userId, method, sessionId);

        sendOtpNotification(user, method, otp, resolvedPhoneNumber);

        boolean countedAsResend = resend || decision.isCountedAsResend();
        mfaAuditService.record(
                countedAsResend ? MfaAuditEventType.OTP_RESENT : MfaAuditEventType.OTP_GENERATED,
                userId, method.name(), sessionId, ip, MfaAuditService.OUTCOME_SUCCESS, null,
                Map.of("invalidatedPrevious", invalidated, "resendCount", decision.getResendCount()));
        if (invalidated > 0) {
            mfaAuditService.record(MfaAuditEventType.OTP_INVALIDATED, userId, method.name(), sessionId, ip,
                    MfaAuditService.OUTCOME_SUCCESS, "previous_otp_invalidated",
                    Map.of("count", invalidated));
        }

        return GenerateOtpResponse.builder()
                .sessionId(sessionId)
                .expiresIn(mfaConfig.getOtpValiditySeconds())
                .phoneNumber(resolvedPhoneNumber)
                .resendCooldownSeconds(decision.getResendCooldownSeconds())
                .resendsRemaining(decision.getResendsRemaining())
                .attemptsRemaining(decision.getAttemptsRemaining())
                .build();
    }

    private int invalidateUnusedOtps(UUID userId, String generatedBy) {
        List<MfaCode> existing = mfaCodeRepository.findByUserIdAndGeneratedByAndIsUsedFalse(userId, generatedBy);
        if (existing.isEmpty()) {
            return 0;
        }
        Instant now = Instant.now();
        existing.forEach(code -> {
            invalidateCode(code);
            code.setInvalidatedAt(now);
            mfaCodeRepository.save(code);
        });
        log.info("Invalidated {} existing unused OTP(s) for user {}", existing.size(), userId);
        return existing.size();
    }

    private static void invalidateCode(MfaCode code) {
        code.setIsUsed(true);
        code.setStatus(MfaCodeStatus.INVALIDATED.name());
        if (code.getInvalidatedAt() == null) {
            code.setInvalidatedAt(Instant.now());
        }
    }

    private OtpLockedException locked(UUID userId, String method, UUID sessionId, String ip) {
        Integer retryAfter = otpSecurityService.remainingLockSeconds(userId).orElse(mfaConfig.getLockDurationSeconds());
        mfaAuditService.record(MfaAuditEventType.OTP_LOCKED, userId, method, sessionId, ip,
                MfaAuditService.OUTCOME_DENIED, "max_attempts", Map.of("retryAfterSeconds", retryAfter));
        return new OtpLockedException(OtpMessages.LOCKED, retryAfter);
    }

    private void auditDenied(MfaAuditEventType eventType, UUID userId, String method, UUID sessionId, String ip, String reason) {
        mfaAuditService.record(eventType, userId, method, sessionId, ip, MfaAuditService.OUTCOME_DENIED, reason, Map.of());
    }

    /**
     * Add-method (no tempToken): request phone, then enrolled MFA phone, then profile.
     * Login challenge (tempToken): enrolled MFA phone, then profile — request phone is ignored.
     */
    private String resolvePhoneNumber(MfaMethod method, String requestPhoneNumber, AspireUser user, boolean loginChallenge) {
        if (method != MfaMethod.SMS && method != MfaMethod.PHONE_CALL) {
            return null;
        }
        if (!loginChallenge && StringUtils.hasText(requestPhoneNumber)) {
            return requestPhoneNumber.trim();
        }
        UserMfaMethod enrolled = getEnrolledMethod(user, method);
        if (enrolled != null && StringUtils.hasText(enrolled.getPhoneNumber())) {
            return enrolled.getPhoneNumber();
        }
        return user.getPhoneNumber();
    }

    /**
     * Send OTP notification via SMS, Email, or Phone Call
     */
    private void sendOtpNotification(AspireUser user, MfaMethod method, String otp, String phoneNumber) {
        try {

            phoneNumber = ObjectUtils.isEmpty(phoneNumber) ? user.getPhoneNumber() : phoneNumber;

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("otp", otp);
            String userName;
            if ("CLIENT_ADMIN".equals(user.getUserType())) {
                userName = user.getFirstName() != null && !user.getFirstName().isEmpty()
                        ? user.getFirstName()
                        : (user.getCompanyName().isEmpty() ? "" : user.getCompanyName());
            } else {
                userName = user.getFirstName() != null ? user.getFirstName() : "";
            }
            templateModel.put("userName", userName.isEmpty() ? "" : " " + userName);

            templateModel.put("expiryMinutes", mfaConfig.getOtpValiditySeconds() / 60);

            // Determine channel and recipient
            NotificationChannel channel;
            String recipient;
            if (method == MfaMethod.SMS) {
                channel = NotificationChannel.SMS;
                recipient = phoneNumber;
                templateModel.put("phoneNumber", phoneNumber);
            } else if (method == MfaMethod.PHONE_CALL) {
                channel = NotificationChannel.PHONE_CALL;
                recipient = phoneNumber;
                templateModel.put("phoneNumber", phoneNumber);
            } else {
                channel = NotificationChannel.EMAIL;
                recipient = user.getEmail();
                logOtpForLocalVerification(recipient, otp);
            }

            NotificationRequestDto.NotificationRequestDtoBuilder requestBuilder = NotificationRequestDto.builder()
                    .to(recipient)
                    .notificationType(NotificationType.SECURITY_ALERTS) // Using generic security alert type
                    .channels(List.of(channel))
                    .templateModel(templateModel)
                    .priority(NotificationPriority.HIGH);

            // Set phoneNumber for SMS and PHONE_CALL channels
            if (method == MfaMethod.SMS || method == MfaMethod.PHONE_CALL) {
                requestBuilder.phoneNumber(phoneNumber);
            }

            NotificationRequestDto requestDto = requestBuilder.build();

            boolean sent = notificationClient.sendNotification(requestDto);
            if (sent) {
                log.info("OTP notification sent successfully via {} to user {}", method, user.getEmail());
            } else {
                log.error("Failed to send OTP notification via {} to user {}", method, user.getEmail());
            }
        } catch (Exception e) {
            log.error("Error sending OTP notification: {}", e.getMessage(), e);
            // Don't throw exception - OTP is already saved, user can request another one
        }
    }

    private void logOtpForLocalVerification(String email, String otp) {
        if (otpConsoleEnabled) {
            log.warn("LOCAL MFA EMAIL OTP for {}: {}", email, otp);
        }
    }

    /**
     * Check if user needs MFA setup.
     * Setup is needed when the user has no enrolled methods (after lazy migration from aspire_user fields).
     */
    public boolean needsMfaSetup(AspireUser user) {
        return ensureMigrated(user).isEmpty();
    }

    /**
     * Check if user needs MFA verification.
     * Verification is needed when the user has at least one enrolled method.
     */
    public boolean needsMfaVerification(AspireUser user) {
        return !ensureMigrated(user).isEmpty();
    }

    public String getDefaultMethodName(AspireUser user) {
        List<UserMfaMethod> methods = ensureMigrated(user);
        return methods.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDefault()))
                .map(UserMfaMethod::getMethod)
                .findFirst()
                .orElse(methods.isEmpty() ? null : methods.get(0).getMethod());
    }

    public List<UserMfaMethodItem> getEnrolledMethods(AspireUser user) {
        return toMethodItems(ensureMigrated(user));
    }

    public UserMfaMethodsResponse listUserMethods(UUID userId) {
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserMfaMethodsResponse.builder()
                .methods(getEnrolledMethods(user))
                .build();
    }

    @Transactional
    public void setDefaultMethod(UUID userId, MfaMethod method) {
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<UserMfaMethod> methods = ensureMigrated(user);

        UserMfaMethod target = methods.stream()
                .filter(m -> method.name().equals(m.getMethod()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "MFA method " + method + " is not set up for this user"));

        Instant now = Instant.now();
        for (UserMfaMethod enrolled : methods) {
            boolean shouldBeDefault = enrolled.getId().equals(target.getId());
            if (!Boolean.valueOf(shouldBeDefault).equals(enrolled.getIsDefault())) {
                enrolled.setIsDefault(shouldBeDefault);
                enrolled.setUpdatedAt(now);
                userMfaMethodRepository.save(enrolled);
            }
        }
        log.info("Default MFA method for user {} set to {}", userId, method);
    }

    @Transactional
    public void removeMethod(UUID userId, MfaMethod method) {
        AspireUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<UserMfaMethod> methods = ensureMigrated(user);

        UserMfaMethod target = methods.stream()
                .filter(m -> method.name().equals(m.getMethod()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MFA method " + method + " is not set up for this user"));

        if (methods.size() <= 1) {
            throw new BadRequestException("Cannot remove the last MFA method. Add another method first.");
        }

        boolean wasDefault = Boolean.TRUE.equals(target.getIsDefault());
        userMfaMethodRepository.delete(target);

        if (wasDefault) {
            List<UserMfaMethod> remaining = methods.stream()
                    .filter(m -> !m.getId().equals(target.getId()))
                    .sorted(Comparator.comparing(UserMfaMethod::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            UserMfaMethod promote = remaining.get(0);
            promote.setIsDefault(true);
            promote.setUpdatedAt(Instant.now());
            userMfaMethodRepository.save(promote);
            log.info("Promoted MFA method {} as default for user {} after removing {}",
                    promote.getMethod(), userId, method);
        }
        log.info("Removed MFA method {} for user {}", method, userId);
    }

    /**
     * Enroll AUTHENTICATOR (or any method) after successful setup verification.
     * First method becomes default; additional methods keep the existing default.
     */
    @Transactional
    public UserMfaMethod enrollMethod(AspireUser user, MfaMethod method, String encryptedSecret) {
        return enrollMethod(user, method, encryptedSecret, null);
    }

    @Transactional
    public UserMfaMethod enrollMethod(AspireUser user, MfaMethod method, String encryptedSecret, String phoneNumber) {
        List<UserMfaMethod> existing = ensureMigrated(user);
        if (existing.stream().anyMatch(m -> method.name().equals(m.getMethod()))) {
            throw new BadRequestException("MFA method " + method + " is already set up for this user");
        }

        boolean isDefault = existing.isEmpty();
        Instant now = Instant.now();
        boolean phoneMethod = method == MfaMethod.SMS || method == MfaMethod.PHONE_CALL;
        UserMfaMethod row = UserMfaMethod.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .method(method.name())
                .secret(encryptedSecret)
                .phoneNumber(phoneMethod ? phoneNumber : null)
                .isDefault(isDefault)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMfaMethodRepository.save(row);

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            user.setMfaEnabled(true);
            user.setUpdatedAt(now);
            userRepository.save(user);
        }
        log.info("Enrolled MFA method {} for user {} (default={})", method, user.getId(), isDefault);
        return row;
    }

    public boolean isMethodEnrolled(AspireUser user, MfaMethod method) {
        return ensureMigrated(user).stream().anyMatch(m -> method.name().equals(m.getMethod()));
    }

    public UserMfaMethod getEnrolledMethod(AspireUser user, MfaMethod method) {
        return ensureMigrated(user).stream()
                .filter(m -> method.name().equals(m.getMethod()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Copy legacy aspire_user MFA fields into user_mfa_methods when the collection has no rows.
     */
    public List<UserMfaMethod> ensureMigrated(AspireUser user) {
        UUID uid = user.getId();
        List<UserMfaMethod> existing = userMfaMethodRepository.findByUserId(uid);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }
        if (Boolean.TRUE.equals(user.getMfaEnabled())
                && StringUtils.hasText(user.getMfaMethod())) {
            Instant now = Instant.now();
            UserMfaMethod migrated = UserMfaMethod.builder()
                    .id(UUID.randomUUID())
                    .userId(uid)
                    .method(user.getMfaMethod())
                    .secret(user.getMfaSecret())
                    .isDefault(true)
                    .createdAt(user.getMfaSetupAt() != null ? user.getMfaSetupAt() : now)
                    .updatedAt(now)
                    .build();
            userMfaMethodRepository.save(migrated);
            log.info("Lazy-migrated MFA method {} for user {}", user.getMfaMethod(), uid);
            return List.of(migrated);
        }
        return existing == null ? List.of() : existing;
    }

    private void assertMethodAllowedForOtp(AspireUser user, MfaMethod method, String tempToken) {
        List<UserMfaMethod> enrolled = ensureMigrated(user);
        boolean loginChallenge = isLoginChallenge(tempToken);
        boolean enrolledMethod = enrolled.stream().anyMatch(m -> method.name().equals(m.getMethod()));

        if (loginChallenge) {
            if (!enrolled.isEmpty() && !enrolledMethod) {
                throw new BadRequestException("MFA method " + method + " is not set up for this user");
            }
        } else if (enrolledMethod) {
            throw new BadRequestException("MFA method " + method + " is already set up for this user");
        }
    }

    private void enrollIfAllowed(AspireUser user, MfaMethod method, String encryptedSecret, String tempToken, String phoneNumber) {
        List<UserMfaMethod> enrolled = ensureMigrated(user);
        boolean alreadyEnrolled = enrolled.stream().anyMatch(m -> method.name().equals(m.getMethod()));
        if (alreadyEnrolled) {
            return;
        }
        if (isLoginChallenge(tempToken) && !enrolled.isEmpty()) {
            throw new BadRequestException("MFA method " + method + " is not set up for this user");
        }
        enrollMethod(user, method, encryptedSecret, phoneNumber);
    }

    private static boolean isLoginChallenge(String tempToken) {
        return StringUtils.hasText(tempToken);
    }

    private static Comparator<UserMfaMethod> defaultFirst() {
        return Comparator.comparing((UserMfaMethod m) -> !Boolean.TRUE.equals(m.getIsDefault()))
                .thenComparing(UserMfaMethod::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private static List<UserMfaMethodItem> toMethodItems(List<UserMfaMethod> methods) {
        return methods.stream()
                .sorted(defaultFirst())
                .map(m -> UserMfaMethodItem.builder()
                        .method(m.getMethod())
                        .isDefault(Boolean.TRUE.equals(m.getIsDefault()))
                        .createdAt(m.getCreatedAt())
                        .phoneNumber(m.getPhoneNumber())
                        .build())
                .toList();
    }
}
