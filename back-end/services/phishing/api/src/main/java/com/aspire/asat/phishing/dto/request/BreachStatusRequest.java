package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.BreachStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating breach status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachStatusRequest {

    @NotNull(message = "Status is required")
    private BreachStatus status;

    private String notes;
}
