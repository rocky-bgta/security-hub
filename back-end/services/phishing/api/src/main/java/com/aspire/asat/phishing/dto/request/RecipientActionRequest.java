package com.aspire.asat.phishing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for recipient breach actions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientActionRequest {

    private String notes;

    private boolean sendEmail;

    private String customMessage;
}
