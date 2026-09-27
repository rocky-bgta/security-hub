package com.aspire.asat.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full recipient hierarchy for an end user, returned by the registration service's
 * {@code GET /api/v1/end-user/notification-data} endpoint. Used by the notification
 * service to fan a single event out to the user, their Client Admin, their MSP and
 * the platform's Aspire Admins.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientBundleDto {

    // End user
    private String userId;
    private String userFullName;
    private String email;
    private String phoneNumber;
    private String phoneCode;

    // Client Admin (organization)
    private String clientAdminId;
    private String clientAdminUserId; // AspireUser.userId for the client admin's login identity (IN_APP)
    private String clientAdminName;
    private String clientAdminEmail;
    private String clientAdminPhoneNumber;
    private String clientAdminPhoneCode;

    // MSP
    private String mspId;
    private String mspUserId; // AspireUser.userId for the MSP's login identity (IN_APP)
    private String mspName;
    private String mspEmail;
    private String mspPhoneNumber;
    private String mspPhoneCode;

    private String organizationName;

    /**
     * Organization timezone reference stored on client_admins ({@code timezones} document id).
     */
    private String organizationTimezoneRef;

    /**
     * Registration {@code timezoneId} label, e.g. {@code WAT (UTC+01:00)}.
     */
    private String organizationTimezoneLabel;

    /**
     * Human-readable timezone display name, e.g. {@code West Africa Time}.
     */
    private String organizationTimezoneDisplayName;

    // Aspire Admin(s) - platform administrators, may be more than one
    private List<NotificationRecipientContactDto> aspireAdmins;
}
