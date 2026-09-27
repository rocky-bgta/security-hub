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
@Schema(description = "Response DTO for next step")
public class NextStepResponseDTO {

    @Schema(description = "Next step ID", example = "nextstep-123")
    private String id;

    @Schema(description = "Next step name", example = "Follow up with finance team")
    private String name;
}

