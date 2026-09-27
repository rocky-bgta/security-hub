package com.aspire.asat.auth.service.impl;

import com.aspire.asat.auth.client.AuthNotificationClient;
import com.aspire.asat.auth.dto.PasswordResetRequestDto;
import com.aspire.asat.auth.dto.ResetPasswordDto;
import com.aspire.asat.auth.dto.enums.UserStatus;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.password.PasswordResetToken;
import com.aspire.asat.auth.entity.password.UserPasswordHistory;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.repository.EndUserPackageRepository;
import com.aspire.asat.auth.repository.PasswordResetTokenRepository;
import com.aspire.asat.auth.repository.UserPasswordHistoryRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.OrgTimezoneService;
import com.aspire.asat.auth.service.PasswordResetService;
import com.aspire.asat.auth.util.PasswordPolicyUtil;
import com.aspire.asat.common.client.ActivityLogClient;
import com.aspire.asat.common.dto.activitylog.CreateActivityLogDto;
import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.validation.PasswordValidationContext;
import com.aspire.asat.common.validation.PasswordValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    static final String INACTIVE_USER_RESET_MESSAGE =
            "Your account is inactive and you cannot reset your password. Please contact your administrator for assistance.";
    static final String NO_COURSE_ASSIGNED_RESET_MESSAGE =
            "You are not allowed to reset your password as you have not been assigned any course yet. Please contact your administrator for assistance.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserPasswordHistoryRepository passwordHistoryRepository;
    private final EndUserPackageRepository endUserPackageRepository;
    private final AuthNotificationClient authNotificationClient;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogClient activityLogClient;
    private final OrgTimezoneService orgTimezoneService;

    @Value("${auth.password-reset-token-expiration-second: 3600}")
    private Long tokenExpirySeconds;

    @Value("${auth.password-set-token-expiration-second: 28800}")
    private Long setTokenExpirySeconds;

    @Value("${auth.password-reset: http://localhost:3000/reset-password}")
    private String resetBaseUrl;

    @Value("${auth.password-history-count:10}")
    private int passwordHistoryCount;

    @Value("${auth.password-history-message-count:5}")
    private int passwordHistoryMessageCount;

    @Override
    public void requestPasswordReset(PasswordResetRequestDto requestDto) {
        var username = requestDto.getUsername().trim();
        AspireUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        assertEligibleForPasswordReset(user);

        Optional<PasswordResetToken> existing = tokenRepository.findByUsernameAndUsedFalse(username);
        existing.ifPresent(t -> {
            t.setUsed(true);
            t.setUpdatedAt(Instant.now());
            t.setUpdatedBy(user.getUserId() != null ? user.getUserId().toString() : null);
            tokenRepository.save(t);
        });

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(tokenExpirySeconds);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .username(username)
                .createdAt(now)
                .updatedAt(now)
                .expiryDate(expiry)
                .used(false)
                .createdBy(user.getUserId() != null ? user.getUserId().toString() : null)
                .updatedBy(user.getUserId() != null ? user.getUserId().toString() : null)
                .build();

        tokenRepository.save(resetToken);

        // Save activity log for forgot password (password reset requested)
        savePasswordResetRequestedActivityLog(user);

        // Send password reset notification
        sendPasswordResetNotification(user, resetToken);
    }

    @Override
    public PasswordResetToken generatePasswordResetToken(PasswordResetRequestDto requestDto) {
        var username = requestDto.getUsername().trim();
        AspireUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<PasswordResetToken> existing = tokenRepository.findByUsernameAndUsedFalse(username);
        existing.ifPresent(t -> {
            t.setUsed(true);
            t.setUpdatedAt(Instant.now());
            t.setUpdatedBy(user.getUserId() != null ? user.getUserId().toString() : null);
            tokenRepository.save(t);
        });

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(setTokenExpirySeconds);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .username(username)
                .createdAt(now)
                .updatedAt(now)
                .expiryDate(expiry)
                .used(false)
                .createdBy(user.getUserId() != null ? user.getUserId().toString() : null)
                .updatedBy(user.getUserId() != null ? user.getUserId().toString() : null)
                .build();

        tokenRepository.save(resetToken);

        // Save activity log for set password token generated
        saveSetPasswordTokenGeneratedActivityLog(user);

        return resetToken;
    }

    public void savePasswordResetToken(String userName, String tempToken) {
        var username = userName.trim();
        AspireUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<PasswordResetToken> existing = tokenRepository.findByUsernameAndUsedFalse(username);
        existing.ifPresent(t -> {
            t.setUsed(true);
            t.setUpdatedAt(Instant.now());
            t.setUpdatedBy(user.getUserId() != null ? user.getUserId().toString() : null);
            tokenRepository.save(t);
        });

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(setTokenExpirySeconds);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tempToken)
                .username(username)
                .createdAt(now)
                .updatedAt(now)
                .expiryDate(expiry)
                .used(false)
                .createdBy(user.getUserId() != null ? user.getUserId().toString() : null)
                .updatedBy(user.getUserId() != null ? user.getUserId().toString() : null)
                .build();

        tokenRepository.save(resetToken);

        // Save activity log for set password token generated
        saveSetPasswordTokenGeneratedActivityLog(user);
    }

    @Override
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        String token = resetPasswordDto.getToken();
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Token already used");
        }

        if (resetToken.getExpiryDate() == null || Instant.now().isAfter(resetToken.getExpiryDate())) {
            throw new BadRequestException("Token expired");
        }

        AspireUser user = userRepository.findByUsernameIgnoreCase(resetToken.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        assertEligibleForPasswordReset(user);

        String newPassword = resetPasswordDto.getNewPassword();
        // Enforce password policy with context
        PasswordValidationContext context = PasswordValidationContext.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
        PasswordValidationResult validation = PasswordPolicyUtil.validate(newPassword, context);
        if (!validation.isValid()) {
            throw new BadRequestException(validation.getCombinedMessage());
        }

        // Do not reuse last N passwords
        String userIdStr = user.getUserId() != null ? user.getUserId().toString() : user.getId() != null ? user.getId().toString() : null;
        if (userIdStr != null) {
            List<UserPasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(userIdStr, PageRequest.of(0, passwordHistoryCount));
            for (UserPasswordHistory h : history) {
                if (passwordEncoder.matches(newPassword, h.getPasswordHash())) {
                    throw new BadRequestException("You cannot use any of your last " + passwordHistoryMessageCount + " passwords, please choose a different password.");
                }
            }
        }

        // Update password
        String encodedNew = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNew);
//        user.setIsDefault(false);
//        user.setTempPasswordExpiry(null);
        userRepository.save(user);

        if (userIdStr != null) {
            passwordHistoryRepository.save(UserPasswordHistory.builder()
                    .userId(userIdStr)
                    .passwordHash(encodedNew)
                    .createdAt(Instant.now())
                    .build());
        }

        // Mark token used
        resetToken.setUsed(true);
        resetToken.setUpdatedAt(Instant.now());
        resetToken.setUpdatedBy(user.getUserId() != null ? user.getUserId().toString() : null);
        tokenRepository.save(resetToken);

        // Save activity log for password reset via token
        savePasswordResetActivityLog(user);

        // Send password reset confirmation notification using password change notification
        sendPasswordResetConfirmationNotification(user);
    }

    /**
     * Forgot-password eligibility: account must be ACTIVE; end users must have an active course assigned.
     * Does not apply to generatePasswordResetToken (onboarding / admin set-password).
     */
    void assertEligibleForPasswordReset(AspireUser user) {
        if (user.getStatus() == null
                || UserStatus.INACTIVE.name().equalsIgnoreCase(user.getStatus().trim()) || UserStatus.TEMPORARY_BLOCKED.name().equalsIgnoreCase(user.getStatus().trim())) {
            throw new BadRequestException(INACTIVE_USER_RESET_MESSAGE);
        }

        if (UserType.USER.getValue().equalsIgnoreCase(user.getUserType())) {
            String userId = user.getUserId() != null
                    ? user.getUserId().toString()
                    : (user.getId() != null ? user.getId().toString() : null);
            if (!StringUtils.hasText(userId)
                    || !endUserPackageRepository.existsByUserIdAndActiveTrue(userId)) {
                throw new BadRequestException(NO_COURSE_ASSIGNED_RESET_MESSAGE);
            }
        }
    }

    /**
     * Send password reset notification to user
     *
     * @param user       the user requesting password reset
     * @param resetToken the password reset token
     */
    private void sendPasswordResetNotification(AspireUser user, PasswordResetToken resetToken) {
        try {
            // Build reset URL
            String resetUrl = resetBaseUrl + "?token=" + resetToken.getToken();

            // Calculate expiry time in human-readable format
            String expiryTime = formatExpiryTime(tokenExpirySeconds);

            // Get a username
            String userName = getUserName(user);

            // Get user email
            String userEmail = user.getEmail() != null ? user.getEmail() : user.getUsername();

            // Get user ID
            String userId = user.getUserId() != null ? user.getUserId().toString() : null;

            // Send notification
            authNotificationClient.sendPasswordResetNotification(userEmail, userId, user.getClientAdminId(), userName, resetUrl, expiryTime);

            log.info("Password reset notification sent successfully to user: {}", userName);

        } catch (Exception e) {
            log.error("Failed to send password reset notification: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to send password reset notification");
        }
    }

    private String getUserName(AspireUser user) {
        return user.getFirstName() != null && user.getLastName() != null
                ? (user.getFirstName() + " " + user.getLastName()).trim()
                : user.getUsername();
    }

    /**
     * Send password reset confirmation notification to user
     * Uses the password change notification to inform user of successful reset
     *
     * @param user the user whose password was reset
     */
    private void sendPasswordResetConfirmationNotification(AspireUser user) {
        try {
            Instant eventInstant = Instant.now();
            String changeTimestamp = orgTimezoneService.formatForEmail(eventInstant, user);

            // Get a username
            String userName = getUserName(user);

            // Get user email
            String userEmail = user.getEmail() != null ? user.getEmail() : user.getUsername();

            // Get user ID
            String userId = user.getUserId() != null ? user.getUserId().toString() : null;

            // Send password change notification to the user
            authNotificationClient.sendPasswordChangeNotificationToUserAndAdmin(
                    new AuthNotificationClient.PasswordChangeNotificationData(
                            userEmail,
                            userId,
                            userName,
                            null, // no admin email for password reset
                            null, // no admin ID
                            null, // no admin name
                            changeTimestamp,
                            eventInstant.toString(),
                            null
                    )
            );

            log.info("Password reset confirmation notification sent successfully to user: {}", userName);

        } catch (Exception e) {
            log.error("Failed to send password reset confirmation notification: {}", e.getMessage(), e);
            // Don't throw exception - password reset was successful
        }
    }

    /**
     * Format expiry time in human-readable format
     *
     * @param seconds number of seconds
     * @return formatted string (e.g., "1 hour", "30 minutes")
     */
    private String formatExpiryTime(Long seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        } else {
            return minutes == 1 ? "1 minute" : minutes + " minutes";
        }
    }

    private void savePasswordResetActivityLog(AspireUser user) {
        try {
            String userIdStr = user.getUserId() != null ? user.getUserId().toString()
                    : user.getId() != null ? user.getId().toString() : null;
            if (userIdStr == null) {
                return;
            }

            String ipAddress = ""; // IP not available in reset flow

            UserType userType = null;
            String userTypeStr = user.getUserType();
            if (userTypeStr != null) {
                try {
                    userType = UserType.fromString(userTypeStr);
                } catch (Exception ignored) {
                }
            }

            String aspireAdminId = null;
            String mspId = null;
            String clientAdminId = null;

            if (userType == UserType.USER) {
                clientAdminId = user.getClientAdminId();
                mspId = user.getMspId();
            } else if (userType == UserType.ASPIRE_ADMIN
                    || userType == UserType.SUPER_ADMIN
                    || userType == UserType.SYSTEM_USER) {
                aspireAdminId = userIdStr;
            } else if (userType == UserType.MSP) {
                mspId = user.getMspId() != null ? user.getMspId() : userIdStr;
            } else if (userType == UserType.CLIENT_ADMIN) {
                clientAdminId = user.getClientAdminId() != null ? user.getClientAdminId() : userIdStr;
            }

            String fullName = getUserName(user);

            CreateActivityLogDto dto = CreateActivityLogDto.builder()
                    .activityType(ActivityType.PASSWORD_RESET)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .userId(userIdStr)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .fullName(fullName)
                    .userType(userType != null ? userType : UserType.USER)
                    .description("User " + user.getUsername() + " reset password via token")
                    .ipAddress(ipAddress)
                    .countryId(user.getCountry())
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .createdAt(Instant.now())
                    .build();

            activityLogClient.createActivityLog(dto);
        } catch (Exception e) {
            log.error("Failed to save password reset activity log for user {}: {}", user.getUsername(), e.getMessage());
        }
    }

    /**
     * Save activity log when user requests password reset (forgot password flow).
     */
    private void savePasswordResetRequestedActivityLog(AspireUser user) {
        savePasswordRelatedActivityLog(user, ActivityType.PASSWORD_RESET_REQUESTED,
                "User " + user.getUsername() + " requested password reset (forgot password)");
    }

    /**
     * Save activity log when a set-password token is generated (e.g. for new user or admin-triggered set password).
     */
    private void saveSetPasswordTokenGeneratedActivityLog(AspireUser user) {
        savePasswordRelatedActivityLog(user, ActivityType.PASSWORD_SET_TOKEN_GENERATED,
                "Set password token/link generated for user " + user.getUsername());
    }

    private void savePasswordRelatedActivityLog(AspireUser user, ActivityType activityType, String description) {
        try {
            String userIdStr = user.getUserId() != null ? user.getUserId().toString()
                    : user.getId() != null ? user.getId().toString() : null;
            if (userIdStr == null) {
                return;
            }

            String ipAddress = "";

            UserType userType = null;
            String userTypeStr = user.getUserType();
            if (userTypeStr != null) {
                try {
                    userType = UserType.fromString(userTypeStr);
                } catch (Exception ignored) {
                }
            }

            String aspireAdminId = null;
            String mspId = null;
            String clientAdminId = null;

            if (userType == UserType.USER) {
                clientAdminId = user.getClientAdminId();
                mspId = user.getMspId();
            } else if (userType == UserType.ASPIRE_ADMIN
                    || userType == UserType.SUPER_ADMIN
                    || userType == UserType.SYSTEM_USER) {
                aspireAdminId = userIdStr;
            } else if (userType == UserType.MSP) {
                mspId = user.getMspId() != null ? user.getMspId() : userIdStr;
            } else if (userType == UserType.CLIENT_ADMIN) {
                clientAdminId = user.getClientAdminId() != null ? user.getClientAdminId() : userIdStr;
            }

            String fullName = getUserName(user);

            CreateActivityLogDto dto = CreateActivityLogDto.builder()
                    .activityType(activityType)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .userId(userIdStr)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .fullName(fullName)
                    .userType(userType != null ? userType : UserType.USER)
                    .description(description)
                    .ipAddress(ipAddress)
                    .countryId(user.getCountry())
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .createdAt(Instant.now())
                    .build();

            activityLogClient.createActivityLog(dto);
        } catch (Exception e) {
            log.error("Failed to save activity log for user {}: {}", user.getUsername(), e.getMessage());
        }
    }
}

