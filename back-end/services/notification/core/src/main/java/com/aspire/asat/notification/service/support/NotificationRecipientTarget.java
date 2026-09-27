package com.aspire.asat.notification.service.support;

/**
 * A single addressable recipient resolved for one role (User, Client Admin, MSP or Aspire
 * Admin) from a {@code NotificationRecipientBundleDto}. Shared between
 * {@code NotificationDispatchService} (new fan-out entry point) and
 * {@code NotificationEscalationService} (legacy bridge) so both use identical
 * resolution/phone-composition rules.
 *
 * @param userId        in-app identity of the recipient
 * @param name          display name used for greetings (e.g. Dear {{clientAdminName}})
 * @param email         email address
 * @param phoneNumber   composed E.164 phone number when available
 * @param clientAdminId organization id used by the preference resolver for org-level gates
 */
public record NotificationRecipientTarget(String userId, String name, String email, String phoneNumber, String clientAdminId) {
}
