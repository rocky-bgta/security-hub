package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Enrolled MFA method for a user. One document per user per method type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "user_mfa_methods")
@CompoundIndex(name = "userId_method_unique", def = "{'userId': 1, 'method': 1}", unique = true)
public class UserMfaMethod {

    @Id
    private UUID id;

    @Indexed
    private UUID userId;

    /** EMAIL, SMS, AUTHENTICATOR (or PHONE_CALL if previously enrolled). */
    private String method;

    /** AES-encrypted TOTP secret; null for SMS/EMAIL. */
    private String secret;

    /** Destination used for SMS / PHONE_CALL OTP. Not the user profile phone. */
    private String phoneNumber;

    private Boolean isDefault;

    private Instant createdAt;

    private Instant updatedAt;
}
