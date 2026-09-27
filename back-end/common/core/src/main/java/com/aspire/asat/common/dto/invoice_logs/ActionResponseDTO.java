package com.aspire.asat.common.dto.invoice_logs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for action")
public class ActionResponseDTO {

    @Schema(description = "Action ID", example = "action-123")
    private String id;

    @Schema(description = "Action name", example = "Payment in review")
    private String name;
}

