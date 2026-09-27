package com.aspire.asat.auth.model.mfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for MFA methods endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaMethodsResponse {
    private List<String> methods;
}

