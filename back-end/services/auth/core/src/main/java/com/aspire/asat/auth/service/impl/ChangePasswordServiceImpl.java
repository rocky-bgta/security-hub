package com.aspire.asat.auth.service.impl;

import com.aspire.asat.auth.client.AuthNotificationClient;
import com.aspire.asat.auth.dto.AdminPasswordResetRequestDto;
import com.aspire.asat.auth.dto.ChangePasswordDto;
import com.aspire.asat.auth.dto.PasswordHistoryEntryDto;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.password.UserPasswordHistory;
import com.aspire.asat.auth.entity.ClientAdmin;
import com.aspire.asat.auth.exception.BadRequestException;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.repository.ClientAdminRepository;
import com.aspire.asat.auth.repository.UserPasswordHistoryRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.auth.service.BaseService;
import com.aspire.asat.auth.service.ChangePasswordService;
import com.aspire.asat.auth.service.OrgTimezoneService;
import com.aspire.asat.auth.util.PasswordPolicyUtil;
import com.aspire.asat.common.client.ActivityLogClient;
import com.aspire.asat.common.dto.activitylog.CreateActivityLogDto;
import com.aspire.asat.common.dto.files.CurrentUserContext;
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

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordServiceImpl extends BaseService implements ChangePasswordService {

    private final UserRepository userRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthNotificationClient authNotificationClient;
    private final UserPasswordHistoryRepository passwordHistoryRepository;
    private final ActivityLogClient activityLogClient;
    private final OrgTimezoneService orgTimezoneService;

    @Value("${auth.password-history-count:10}")
    private int passwordHistoryCount;

    @Value("${auth.password-history-message-count:5}")
    private int passwordHistoryMessageCount;

    @Override
    public void changePassword(ChangePasswordDto dto) {
        CurrentUserContext ctx = getCurrentUserContext();
        if (ctx == null || ctx.getUsername() == null) {
            throw new BadRequestException("Unable to resolve current user");
        }

        String username = ctx.getUsername();

        AspireUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Confirm new passwords match
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Enforce password policy (with context to reject personal info)
        PasswordValidationContext context = PasswordValidationContext.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
        PasswordValidationResult validation = PasswordPolicyUtil.validate(dto.getNewPassword(), context);
        if (!validation.isValid()) {
            throw new BadRequestException(validation.getCombinedMessage());
        }

        // Do not reuse last N passwords
        String userIdStr = user.getUserId() != null ? user.getUserId().toString() : user.getId() != null ? user.getId().toString() : null;
        if (userIdStr != null) {
            List<UserPasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(userIdStr, PageRequest.of(0, passwordHistoryCount));
            for (UserPasswordHistory h : history) {
                if (passwordEncoder.matches(dto.getNewPassword(), h.getPasswordHash())) {
                    throw new BadRequestException("You cannot use any of your last " + passwordHistoryMessageCount + " passwords, please choose a different password.");
                }
            }
        }

        // Update password
        String encodedNew = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encodedNew);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(ctx.getUserId());
        user.setIsDefault(false);
        user.setTempPasswordExpiry(null);

        userRepository.save(user);

        // Append to password history for reuse check
        if (userIdStr != null) {
            passwordHistoryRepository.save(UserPasswordHistory.builder()
                    .userId(userIdStr)
                    .passwordHash(encodedNew)
                    .createdAt(Instant.now())
                    .build());
        }

        log.info("Password changed for user: {}", username);

        // Save activity log for password change
        savePasswordChangedActivityLog(user, ctx);

        // Send password change notification to both user and admin
        sendPasswordChangeNotification(user, ctx);
    }

    @Override
    public List<PasswordHistoryEntryDto> getPasswordHistory() {
        CurrentUserContext ctx = getCurrentUserContext();

        List<UserPasswordHistory> history = passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(ctx.getUserId(), PageRequest.of(0, passwordHistoryCount));
        return history.stream()
                .map(h -> PasswordHistoryEntryDto.builder().changedAt(h.getCreatedAt()).build())
                .collect(Collectors.toList());
    }

    /**
     * Send password change notification to the user and their admin (if applicable)
     * @param user the user whose password was changed
     */
    private void sendPasswordChangeNotification(AspireUser user, CurrentUserContext ctx) {
        try {
            Instant eventInstant = Instant.now();
            String changeTimestamp = orgTimezoneService.formatForEmail(eventInstant, user);

            // Prepare user information
            String userName = (user.getFirstName() + " " + user.getLastName()).trim();
            if (userName.isEmpty()) {
                userName = user.getEmail();
            }

            String adminEmail = ctx.getClientAdminEmail();
            String adminId = ctx.getClientAdminId();
            String adminName = ctx.getClientAdminFullName();

            // Send notifications
            authNotificationClient.sendPasswordChangeNotificationToUserAndAdmin( new AuthNotificationClient.PasswordChangeNotificationData(
                    user.getEmail(),
                    user.getUserId() != null ? user.getUserId().toString() : null,
                    userName,
                    adminEmail,
                    adminId,
                    adminName,
                    changeTimestamp,
                    eventInstant.toString(),
                    user.getUserType() != null ? user.getUserType() : UserType.USER.name()
            ));

        } catch (Exception e) {
            log.error("Failed to send password change notification: {}", e.getMessage(), e);
        }
    }

    @Override
    public void resetPasswordByAdmin(AdminPasswordResetRequestDto requestDto) {
        CurrentUserContext adminContext = getCurrentUserContext();
        if (adminContext == null || adminContext.getUserId() == null) {
            throw new BadRequestException("Unable to resolve admin user context");
        }

        // Validate userId
        UUID userId;
        try {
            userId = UUID.fromString(requestDto.getUserId());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid userId format: " + requestDto.getUserId());
        }

        // Find the user by userId (try both id and userId fields)
        Optional<AspireUser> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            // Fallback to findById in case userId field is not set
            userOpt = userRepository.findById(userId);
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found with userId: " + requestDto.getUserId());
        }

        AspireUser user = userOpt.get();

        // Generate 8-character password
        String newPassword = generateRandomPassword(8);
        log.info("Generated new password for user: {}", user.getEmail());

        // Encode and update password
        String encodedNew = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNew);
        user.setUpdatedAt(Instant.now());
        user.setUpdatedBy(adminContext.getUserId());
        // Only force next-login password change for scoped admin types; leave end users unchanged.
        if (isForcedCredentialChangeUserType(user.getUserType())) {
            user.setIsDefault(true);
            user.setTempPasswordExpiry(null);
        }
        userRepository.save(user);

        // Append to password history for reuse check
        String userIdStr = user.getUserId() != null ? user.getUserId().toString() : user.getId() != null ? user.getId().toString() : null;
        if (userIdStr != null) {
            passwordHistoryRepository.save(UserPasswordHistory.builder()
                    .userId(userIdStr)
                    .passwordHash(encodedNew)
                    .createdAt(Instant.now())
                    .build());
        }

        log.info("Password reset by admin for user: {}", user.getEmail());

        // Save activity log for admin password reset
        saveAdminPasswordResetActivityLog(user, adminContext);

        // Get client admin information if user has clientAdminId
        AspireUser clientAdmin = null;
        if (user.getClientAdminId() != null && !user.getClientAdminId().isBlank()) {
            try {
                UUID clientAdminUuid = UUID.fromString(user.getClientAdminId());
                Optional<AspireUser> clientAdminOpt = userRepository.findById(clientAdminUuid);
                if (clientAdminOpt.isPresent()) {
                    clientAdmin = clientAdminOpt.get();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch client admin for userId: {}, error: {}", user.getClientAdminId(), e.getMessage());
            }
        }

        // Send notifications
        sendAdminPasswordResetNotifications(user, clientAdmin, newPassword, adminContext);
    }

    @Override
    public void resetClientAdminPasswordByMsp(AdminPasswordResetRequestDto requestDto) {
        CurrentUserContext mspContext = getCurrentUserContext();
        if (mspContext == null || mspContext.getUserId() == null) {
            throw new BadRequestException("Unable to resolve MSP user context");
        }

        UserType callerType = resolveUserType(mspContext.getUserType());
        if (callerType != UserType.MSP) {
            throw new UnauthorizedResourceException("Only MSP admin users can reset client admin passwords");
        }

        AspireUser clientAdminUser = findAspireUserById(requestDto.getUserId());
        UserType targetType = resolveUserType(clientAdminUser.getUserType());
        if (targetType != UserType.CLIENT_ADMIN) {
            throw new BadRequestException("Password reset is only allowed for client admin users");
        }

        String newPassword = generateRandomPassword(8);
        log.info("Generated new password for client admin: {}", clientAdminUser.getEmail());

        String encodedNew = passwordEncoder.encode(newPassword);
        clientAdminUser.setPassword(encodedNew);
        clientAdminUser.setUpdatedAt(Instant.now());
        clientAdminUser.setUpdatedBy(mspContext.getUserId());
        clientAdminUser.setIsDefault(true); // CLIENT_ADMIN must change temp password on next login
        clientAdminUser.setTempPasswordExpiry(null);
        userRepository.save(clientAdminUser);

        String userIdStr = clientAdminUser.getUserId() != null ? clientAdminUser.getUserId().toString()
                : clientAdminUser.getId() != null ? clientAdminUser.getId().toString() : null;
        if (userIdStr != null) {
            passwordHistoryRepository.save(UserPasswordHistory.builder()
                    .userId(userIdStr)
                    .passwordHash(encodedNew)
                    .createdAt(Instant.now())
                    .build());
        }

        log.info("Password reset by MSP admin for client admin: {}", clientAdminUser.getEmail());
        saveAdminPasswordResetActivityLog(clientAdminUser, mspContext);
        sendSingleAdminPasswordResetNotification(clientAdminUser, newPassword);
    }

    private AspireUser findAspireUserById(String userIdValue) {
        UUID userId;
        try {
            userId = UUID.fromString(userIdValue);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid userId format: " + userIdValue);
        }

        Optional<AspireUser> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findById(userId);
        }
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found with userId: " + userIdValue);
        }
        return userOpt.get();
    }

    private void sendSingleAdminPasswordResetNotification(AspireUser user, String newPassword) {
        try {
            String userName = buildFullName(user.getFirstName(), user.getLastName());
            if (userName.isEmpty()) {
                userName = user.getEmail();
            }

            authNotificationClient.sendAdminPasswordResetNotification(
                    user.getEmail(),
                    user.getUserId() != null ? user.getUserId().toString() : null,
                    user.getClientAdminId(),
                    userName,
                    newPassword
            );

            log.info("Password reset notification sent to client admin: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset notification: {}", e.getMessage(), e);
        }
    }

    private UserType resolveUserType(String userTypeStr) {
        if (userTypeStr == null || userTypeStr.isBlank()) {
            return null;
        }
        try {
            return UserType.fromString(userTypeStr);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Admin-style accounts that must change a temporary password on next login.
     * Intentionally excludes USER / CLIENT / SYSTEM_USER and others.
     */
    private boolean isForcedCredentialChangeUserType(String userType) {
        if (userType == null || userType.isBlank()) {
            return false;
        }
        return UserType.MSP.getValue().equalsIgnoreCase(userType)
                || UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.SYSTEM_USER.getValue().equalsIgnoreCase(userType);
    }

    private String resolveMspId(CurrentUserContext context) {
        if (context.getMspId() != null && !context.getMspId().isBlank()) {
            return context.getMspId();
        }
        return context.getUserId();
    }

    /**
     * Generate a random password that meets policy (min 8 chars, upper, lower, digit, special).
     * @param length the length of the password (min 8)
     * @return generated password
     */
    private String generateRandomPassword(int length) {
        if (length < 8) length = 8;
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        String all = upper + lower + digits + special;
        for (int i = 4; i < length; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        // Shuffle
        for (int i = sb.length() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char t = sb.charAt(i);
            sb.setCharAt(i, sb.charAt(j));
            sb.setCharAt(j, t);
        }
        return sb.toString();
    }

    /**
     * Send password reset notifications to user and client admin
     * @param user the user whose password was reset
     * @param clientAdmin the client admin (if applicable)
     * @param newPassword the new password (plain text)
     * @param adminContext the admin context who performed the reset
     */
    private void sendAdminPasswordResetNotifications(AspireUser user, AspireUser clientAdmin, 
                                                     String newPassword, CurrentUserContext adminContext) {
        try {
            String userName = buildFullName(user.getFirstName(), user.getLastName());
            if (userName.isEmpty()) {
                userName = user.getEmail();
            }

            // Send notification to user with new password
            authNotificationClient.sendAdminPasswordResetNotification(
                    user.getEmail(),
                    user.getUserId() != null ? user.getUserId().toString() : null,
                    user.getClientAdminId(),
                    userName,
                    newPassword
            );

            // Send in-app notification to client admin only if user type is USER
            if (clientAdmin != null && UserType.USER.getValue().equals(user.getUserType())) {
                String adminName = buildFullName(clientAdmin.getFirstName(), clientAdmin.getLastName());
                if (adminName.isEmpty()) {
                    adminName = clientAdmin.getEmail();
                }

                authNotificationClient.sendAdminPasswordResetNotificationToClientAdmin(
                        clientAdmin.getEmail(),
                        clientAdmin.getUserId() != null ? clientAdmin.getUserId().toString() : null,
                        user.getClientAdminId(),
                        adminName,
                        userName,
                        user.getEmail()
                );
            }

            log.info("Password reset notifications sent successfully for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset notifications: {}", e.getMessage(), e);
            // Don't throw exception - password is already reset
        }
    }

    /**
     * Build full name from first and last name
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName != null ? lastName : "";
        }
        if (lastName == null) {
            return firstName;
        }
        return (firstName + " " + lastName).trim();
    }

    private void savePasswordChangedActivityLog(AspireUser user, CurrentUserContext ctx) {
        try {
            String userIdStr = user.getUserId() != null ? user.getUserId().toString()
                    : user.getId() != null ? user.getId().toString() : null;
            if (userIdStr == null) {
                return;
            }

            String ipAddress = getRemoteIPAddress();

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
            String countryId = ctx != null ? ctx.getCountryId() : null;

            if (userType == UserType.USER) {
                if (ctx != null) {
                    clientAdminId = ctx.getClientAdminId();
                    mspId = ctx.getMspId();
                }
            } else if (userType == UserType.ASPIRE_ADMIN
                    || userType == UserType.SUPER_ADMIN
                    || userType == UserType.SYSTEM_USER) {
                aspireAdminId = userIdStr;
            } else if (userType == UserType.MSP) {
                mspId = user.getMspId() != null ? user.getMspId() : userIdStr;
            } else if (userType == UserType.CLIENT_ADMIN) {
                clientAdminId = user.getClientAdminId() != null ? user.getClientAdminId() : userIdStr;
            }

            String fullName = buildFullName(user.getFirstName(), user.getLastName());

            CreateActivityLogDto dto = CreateActivityLogDto.builder()
                    .activityType(ActivityType.PASSWORD_CHANGED)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .userId(userIdStr)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .fullName(fullName)
                    .userType(userType != null ? userType : UserType.USER)
                    .description("User " + user.getUsername() + " changed password")
                    .ipAddress(ipAddress)
                    .countryId(countryId)
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .createdAt(Instant.now())
                    .build();

            activityLogClient.createActivityLog(dto);
        } catch (Exception e) {
            log.error("Failed to save password changed activity log for user {}: {}", user.getUsername(), e.getMessage());
        }
    }

    private void saveAdminPasswordResetActivityLog(AspireUser user, CurrentUserContext adminCtx) {
        try {
            String targetUserId = user.getUserId() != null ? user.getUserId().toString()
                    : user.getId() != null ? user.getId().toString() : null;
            if (targetUserId == null || adminCtx == null || adminCtx.getUserId() == null) {
                return;
            }

            String ipAddress = getRemoteIPAddress();

            String adminUserTypeStr = adminCtx.getUserType();
            UserType adminUserType = null;
            if (adminUserTypeStr != null) {
                try {
                    adminUserType = UserType.fromString(adminUserTypeStr);
                } catch (Exception ignored) {
                }
            }

            String aspireAdminId = null;
            String mspId = adminCtx.getMspId();
            String clientAdminId = adminCtx.getClientAdminId();
            String countryId = adminCtx.getCountryId();

            if (adminUserType == UserType.ASPIRE_ADMIN
                    || adminUserType == UserType.SUPER_ADMIN
                    || adminUserType == UserType.SYSTEM_USER) {
                aspireAdminId = adminCtx.getUserId();
            } else if (adminUserType == UserType.MSP) {
                if (mspId == null || mspId.isBlank()) {
                    mspId = adminCtx.getUserId();
                }
            } else if (adminUserType == UserType.CLIENT_ADMIN) {
                if (clientAdminId == null || clientAdminId.isBlank()) {
                    clientAdminId = adminCtx.getUserId();
                }
            }

            String fullName = buildFullName(user.getFirstName(), user.getLastName());

            CreateActivityLogDto dto = CreateActivityLogDto.builder()
                    .activityType(ActivityType.PASSWORD_RESET)
                    .activityStatus(ActivityStatus.SUCCESS)
                    .userId(targetUserId)
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .fullName(fullName)
                    .userType(adminUserType != null ? adminUserType : UserType.ASPIRE_ADMIN)
                    .description("Password reset by admin " + adminCtx.getUsername() + " for user " + user.getUsername())
                    .ipAddress(ipAddress)
                    .countryId(countryId)
                    .aspireAdminId(aspireAdminId)
                    .mspId(mspId)
                    .clientAdminId(clientAdminId)
                    .createdAt(Instant.now())
                    .build();

            activityLogClient.createActivityLog(dto);
        } catch (Exception e) {
            log.error("Failed to save admin password reset activity log for user {}: {}", user.getUsername(), e.getMessage());
        }
    }
}

