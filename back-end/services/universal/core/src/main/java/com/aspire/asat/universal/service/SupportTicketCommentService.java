package com.aspire.asat.universal.service;

import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentDto;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentRequestDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;

import java.util.List;

public interface SupportTicketCommentService {
    
    /**
     * Get all comments for a support ticket (with threaded replies)
     * Returns root comments with nested replies
     * @deprecated Use getCommentsForTicketWithPagination instead
     */
    @Deprecated
    List<SupportTicketCommentDto> getCommentsForTicket(String ticketId);
    
    /**
     * Get paginated comments for a support ticket (with threaded replies)
     * Returns paginated root comments with nested replies
     * Pagination is applied to root comments only; all replies are included for each root comment
     */
    OffsetPageDto<SupportTicketCommentDto> getCommentsForTicketWithPagination(String ticketId, int offset, int pageSize);
    
    /**
     * Create a new comment on a support ticket
     * If parentCommentId is provided, creates a reply to that comment
     */
    SupportTicketCommentDto createComment(String ticketId, SupportTicketCommentRequestDto request);
    
    /**
     * Delete a comment by ID
     */
    void deleteComment(String commentId);
}

