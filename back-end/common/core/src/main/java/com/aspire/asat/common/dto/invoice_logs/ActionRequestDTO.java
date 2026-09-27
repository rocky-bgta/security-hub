package com.aspire.asat.common.dto.invoice_logs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating or updating an action")
public class ActionRequestDTO {

    @NotBlank(message = "Action name is required")
    @Schema(description = "Action name", example = "Payment in review", required = true)
    private String name;
}

