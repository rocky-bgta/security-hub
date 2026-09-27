package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachableMomentRequest {

    @NotBlank(message = "Recipient id is required")
    private String recipientId;

    private String message;
}
