package com.aspire.asat.universal.supportTicket.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for adding a comment to a support ticket")
public class SupportTicketCommentRequestDto {

    @NotBlank(message = "Comment is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    @Schema(description = "Comment text to add to the ticket", example = "Issue resolved after clearing browser cache", required = true)
    private String comment;
}

