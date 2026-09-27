package com.aspire.asat.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMfaMethodItem implements Serializable {

    private String method;
    private Boolean isDefault;
    private Instant createdAt;
    /** SMS / PHONE_CALL destination; null for EMAIL and AUTHENTICATOR. */
    private String phoneNumber;
}
