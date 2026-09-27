package com.aspire.asat.notification.service.support;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientContactDto;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves the addressable recipient(s) for a single role out of a
 * {@link NotificationRecipientBundleDto}, and composes E.164 phone numbers from the
 * separately stored phoneCode/phoneNumber pairs used across {@code AspireUser},
 * {@code ClientAdmin} and {@code MspUser}. Shared by the role-based dispatch service and
 * the legacy escalation bridge so both resolve recipients identically.
 */
@Component
public class NotificationRecipientResolver {

    public List<NotificationRecipientTarget> resolveTargets(NotificationRecipientRole role, NotificationRecipientBundleDto bundle) {
        return switch (role) {
            case USER -> singleTargetOrEmpty(bundle.getUserId(), bundle.getUserFullName(), bundle.getEmail(),
                    composePhone(bundle.getPhoneCode(), bundle.getPhoneNumber()), bundle.getClientAdminId());
            case CLIENT_ADMIN -> singleTargetOrEmpty(bundle.getClientAdminUserId(), bundle.getClientAdminName(),
                    bundle.getClientAdminEmail(),
                    composePhone(bundle.getClientAdminPhoneCode(), bundle.getClientAdminPhoneNumber()),
                    bundle.getClientAdminId());
            case MSP -> singleTargetOrEmpty(bundle.getMspUserId(), bundle.getMspName(), bundle.getMspEmail(),
                    composePhone(bundle.getMspPhoneCode(), bundle.getMspPhoneNumber()), null);
            case ASPIRE_ADMIN -> resolveAspireAdminTargets(bundle);
        };
    }

    /**
     * Compose an E.164 phone number from separately stored phoneCode and phoneNumber fields
     * (e.g. {@code MspUser.phoneCode} + {@code MspUser.phoneNumber}). Returns the raw number
     * if it already looks like E.164, or if no phoneCode is available.
     */
    public String composePhone(String phoneCode, String phoneNumber) {
        if (isBlank(phoneNumber)) {
            return null;
        }
        String trimmedNumber = phoneNumber.trim();
        if (trimmedNumber.startsWith("+")) {
            return trimmedNumber;
        }
        if (isBlank(phoneCode)) {
            return trimmedNumber;
        }
        String trimmedCode = phoneCode.trim();
        String normalizedCode = trimmedCode.startsWith("+") ? trimmedCode : "+" + trimmedCode;
        return normalizedCode + trimmedNumber;
    }

    private List<NotificationRecipientTarget> singleTargetOrEmpty(String userId, String name, String email,
                                                                   String phoneNumber, String clientAdminId) {
        if (isBlank(email) && isBlank(userId)) {
            return List.of();
        }
        return List.of(new NotificationRecipientTarget(userId, name, email, phoneNumber, clientAdminId));
    }

    private List<NotificationRecipientTarget> resolveAspireAdminTargets(NotificationRecipientBundleDto bundle) {
        if (bundle.getAspireAdmins() == null || bundle.getAspireAdmins().isEmpty()) {
            return List.of();
        }
        return bundle.getAspireAdmins().stream()
                .filter(admin -> !isBlank(admin.getEmail()) || !isBlank(admin.getUserId()))
                .map(this::toRecipientTarget)
                .toList();
    }

    private NotificationRecipientTarget toRecipientTarget(NotificationRecipientContactDto admin) {
        return new NotificationRecipientTarget(admin.getUserId(), admin.getName(), admin.getEmail(),
                composePhone(admin.getPhoneCode(), admin.getPhoneNumber()), null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
