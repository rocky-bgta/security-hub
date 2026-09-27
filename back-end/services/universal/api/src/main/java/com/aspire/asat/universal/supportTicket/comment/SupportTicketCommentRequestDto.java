package com.aspire.asat.universal.supportTicket.comment;

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
@Schema(description = "Request DTO for creating a comment on a support ticket")
public class SupportTicketCommentRequestDto {

    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 2000, message = "Comment text must be between 1 and 2000 characters")
    @Schema(description = "Comment text content", example = "Issue resolved after clearing browser cache", required = true)
    private String commentText;

    @Schema(description = "Parent comment ID if this is a reply to another comment", example = "comment-123")
    private String parentCommentId;

    @Schema(description = "Optional attachment URL for the comment", example = "https://example.com/attachment.pdf")
    private String attachmentUrl;
}

