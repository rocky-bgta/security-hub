package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for admin-only domain registration as pre-verified.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDomainAddRequest {

    @NotBlank(message = "Domain is required")
    private String domain;
}
