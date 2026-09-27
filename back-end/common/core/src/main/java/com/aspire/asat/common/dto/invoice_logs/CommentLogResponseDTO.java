package com.aspire.asat.common.dto.invoice_logs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for comment log")
public class CommentLogResponseDTO {

    @Schema(description = "Comment log ID", example = "commentlog-123")
    private String id;

    @Schema(description = "Invoice ID", example = "INV-1234567890")
    private String invoiceId;

    @Schema(description = "Date of the comment", example = "2025-12-01T10:30:00Z")
    private Instant date;

    @Schema(description = "Username who created the comment", example = "john.doe@example.com")
    private String userName;

    @Schema(description = "User role who created the comment", example = "Finance Admin")
    private String userRole;

    @Schema(description = "Comment text", example = "Payment initiated, awaiting approval from finance team.")
    private String comment;

    @Schema(description = "Action taken ID", example = "action-123")
    private String actionTakenId;

    @Schema(description = "Action taken name", example = "Payment in review")
    private String actionName;

    @Schema(description = "Next step ID", example = "nextstep-123")
    private String nextStepId;

    @Schema(description = "Next step name", example = "Follow up with finance team")
    private String nextStepName;

    @Schema(description = "Approval status", example = "false")
    private Boolean approved;

    @Schema(description = "Creation timestamp", example = "2025-12-01T10:30:00Z")
    private Instant createdAt;
}

