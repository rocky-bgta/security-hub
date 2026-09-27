package com.aspire.asat.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single addressable contact (used for the Aspire Admin list, where there can be
 * more than one active recipient for a given role).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecipientContactDto {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String phoneCode;
}
