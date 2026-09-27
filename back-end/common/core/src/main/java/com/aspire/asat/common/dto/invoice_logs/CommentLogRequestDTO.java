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
@Schema(description = "DTO for creating or updating a comment log")
public class CommentLogRequestDTO {

    @Schema(description = "Invoice ID to which this comment log belongs (optional - will be set by billing service if not provided)", example = "INV-1234567890", required = false)
    private String invoiceId;

    @NotBlank(message = "Comment is required")
    @Schema(description = "Comment text", example = "Payment initiated, awaiting approval from finance team.", required = true)
    private String comment;

    @NotBlank(message = "Action taken ID is required")
    @Schema(description = "ID of the action taken", example = "action-123", required = true)
    private String actionTakenId;

    @NotBlank(message = "Next step ID is required")
    @Schema(description = "ID of the next step", example = "nextstep-123", required = true)
    private String nextStepId;

    @Schema(description = "Username who created the comment (optional - populated from CurrentUserContext if not provided)", example = "john.doe@example.com")
    private String userName;

    @Schema(description = "User role who created the comment (optional - populated from CurrentUserContext if not provided)", example = "CLIENT_ADMIN")
    private String userRole;

    @Schema(description = "Approval status (optional - defaults to false if not provided)", example = "false")
    private Boolean approved;
}

