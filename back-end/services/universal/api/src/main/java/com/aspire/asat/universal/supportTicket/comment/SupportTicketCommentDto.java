package com.aspire.asat.universal.supportTicket.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for support ticket comment")
public class SupportTicketCommentDto {

    @Schema(description = "Unique comment identifier", example = "comment-123")
    private String id;

    @Schema(description = "Support ticket ID this comment belongs to", example = "ticket-456")
    private String ticketId;

    @Schema(description = "Parent comment ID if this is a reply", example = "comment-122")
    private String parentCommentId;

    @Schema(description = "Comment text content", example = "Issue resolved after clearing browser cache")
    private String commentText;

    @Schema(description = "User ID who created the comment", example = "user-789")
    private String author;

    @Schema(description = "Username of the author", example = "john.doe")
    private String authorName;

    @Schema(description = "Optional attachment URL", example = "https://example.com/attachment.pdf")
    private String attachmentUrl;

    @Schema(description = "Timestamp when the comment was created")
    private Instant commentedAt;

    @Schema(description = "List of reply comments (nested structure)")
    @Builder.Default
    private List<SupportTicketCommentDto> replies = new ArrayList<>();
}

