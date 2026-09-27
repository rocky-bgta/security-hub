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
@Schema(description = "DTO for creating or updating a next step")
public class NextStepRequestDTO {

    @NotBlank(message = "Next step name is required")
    @Schema(description = "Next step name", example = "Follow up with finance team", required = true)
    private String name;
}

