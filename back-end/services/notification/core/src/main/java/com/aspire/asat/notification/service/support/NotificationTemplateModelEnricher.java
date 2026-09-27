package com.aspire.asat.notification.service.support;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationTemplateValue;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.util.DatetimeUtils;
import com.aspire.asat.common.util.TimezoneResolver;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the final template model for a single recipient send by merging the caller's event
 * payload with identity fields from the registration recipient bundle.
 *
 * <p>Merge order:
 * <ol>
 *   <li>Caller {@code templateModel} (event payload such as course title, timestamps)</li>
 *   <li>Registration identity keys (always applied; win over mistaken caller identity fields)</li>
 *   <li>Optional per-role overrides (rare non-identity differences)</li>
 *   <li>Timezone formatting for {@code eventTimestamp} → {@code timestamp}</li>
 * </ol>
 */
@Component
public class NotificationTemplateModelEnricher {

    /**
     * Merge event payload, registration identity and optional role overrides for one recipient.
     */
    public Map<String, Object> merge(Map<String, Object> eventTemplateModel,
                                     Map<NotificationRecipientRole, Map<String, Object>> templateModelByRole,
                                     NotificationRecipientRole role,
                                     NotificationRecipientBundleDto bundle,
                                     NotificationRecipientTarget target) {
        Map<String, Object> merged = new HashMap<>();
        if (eventTemplateModel != null) {
            merged.putAll(eventTemplateModel);
        }

        putIdentityFromBundle(merged, role, bundle, target);

        if (templateModelByRole != null && templateModelByRole.get(role) != null) {
            merged.putAll(templateModelByRole.get(role));
        }

        applyOrganizationTimezoneFormatting(merged, bundle);
        return merged;
    }

    /**
     * Enrich a legacy request's template model with registration identity for an escalated
     * MSP / Aspire Admin send (no per-role overrides).
     */
    public Map<String, Object> enrich(Map<String, Object> eventTemplateModel,
                                      NotificationRecipientRole role,
                                      NotificationRecipientBundleDto bundle,
                                      NotificationRecipientTarget target) {
        return merge(eventTemplateModel, null, role, bundle, target);
    }

    /**
     * When {@code eventTimestamp} (ISO UTC instant) is present, format {@code timestamp}
     * using the organization timezone from the recipient bundle.
     */
    private void applyOrganizationTimezoneFormatting(Map<String, Object> merged,
                                                     NotificationRecipientBundleDto bundle) {
        Object eventTimestamp = merged.get(NotificationTemplateValue.EVENT_TIMESTAMP);
        if (eventTimestamp == null) {
            return;
        }

        Instant instant = parseInstant(eventTimestamp);
        if (instant == null) {
            return;
        }

        ZoneId zone = resolveOrganizationZone(bundle);
        merged.put(NotificationTemplateValue.TIMESTAMP, DatetimeUtils.formatForEmail(instant, zone));
    }

    private static ZoneId resolveOrganizationZone(NotificationRecipientBundleDto bundle) {
        if (bundle == null) {
            return TimezoneResolver.resolve(null);
        }
        return TimezoneResolver.resolve(
                bundle.getOrganizationTimezoneLabel(),
                bundle.getOrganizationTimezoneDisplayName());
    }

    private static Instant parseInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString().trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void putIdentityFromBundle(Map<String, Object> merged,
                                       NotificationRecipientRole role,
                                       NotificationRecipientBundleDto bundle,
                                       NotificationRecipientTarget target) {
        if (bundle == null) {
            return;
        }

        if (!merged.containsKey(NotificationTemplateValue.USER_NAME) && !isBlank(bundle.getUserFullName())) {
            merged.put(NotificationTemplateValue.USER_NAME, bundle.getUserFullName());
        }

        putIfPresent(merged, NotificationTemplateValue.CLIENT_ADMIN_NAME, bundle.getClientAdminName());
        putIfPresent(merged, NotificationTemplateValue.MSP_NAME, bundle.getMspName());
        putIfPresent(merged, NotificationTemplateValue.MSP_EMAIL, bundle.getMspEmail());

        if (!isBlank(bundle.getOrganizationName())) {
            putIfPresent(merged, NotificationTemplateValue.ORGANIZATION_NAME, bundle.getOrganizationName());
            if (isBlank(stringValue(merged.get(NotificationTemplateValue.COMPANY_NAME)))) {
                merged.put(NotificationTemplateValue.COMPANY_NAME, bundle.getOrganizationName());
            }
        }

        if (role == NotificationRecipientRole.ASPIRE_ADMIN && target != null && !isBlank(target.name())) {
            merged.put(NotificationTemplateValue.ASPIRE_ADMIN_NAME, target.name());
        }

        String recipientDisplayName = resolveRecipientDisplayName(role, bundle, target);
        if (!isBlank(recipientDisplayName)) {
            merged.put(NotificationTemplateValue.ADMIN_NAME, recipientDisplayName);
        }
    }

    private static String resolveRecipientDisplayName(NotificationRecipientRole role,
                                                      NotificationRecipientBundleDto bundle,
                                                      NotificationRecipientTarget target) {
        if (role == null) {
            return target != null ? target.name() : null;
        }
        return switch (role) {
            case USER -> firstNonBlank(bundle.getUserFullName(), target != null ? target.name() : null);
            case CLIENT_ADMIN -> firstNonBlank(bundle.getClientAdminName(), target != null ? target.name() : null);
            case MSP -> firstNonBlank(bundle.getMspName(), target != null ? target.name() : null);
            case ASPIRE_ADMIN -> target != null ? target.name() : null;
        };
    }

    private static void putIfPresent(Map<String, Object> merged, String key, String value) {
        if (!isBlank(value)) {
            merged.put(key, value);
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        return isBlank(second) ? null : second;
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
