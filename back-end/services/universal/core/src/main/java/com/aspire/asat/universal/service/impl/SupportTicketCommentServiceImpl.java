package com.aspire.asat.universal.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.entity.SupportTicket;
import com.aspire.asat.universal.entity.SupportTicketComment;
import com.aspire.asat.universal.exception.ResourceNotFoundException;
import com.aspire.asat.universal.exception.UniversalServiceException;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.universal.repository.SupportTicketCommentRepository;
import com.aspire.asat.universal.repository.SupportTicketRepository;
import com.aspire.asat.universal.service.SupportTicketCommentService;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentDto;
import com.aspire.asat.universal.supportTicket.comment.SupportTicketCommentRequestDto;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketCommentServiceImpl implements SupportTicketCommentService {

    private final SupportTicketCommentRepository commentRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public List<SupportTicketCommentDto> getCommentsForTicket(String ticketId) {
        log.info("Fetching all comments for ticket ID: {}", ticketId);
        
        // Verify ticket exists
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Support ticket not found with ID: " + ticketId);
        }
        
        // Get root comments (no parent)
        List<SupportTicketComment> rootComments = commentRepository
                .findByTicketIdAndParentCommentIdIsNullOrderByCommentedAtAsc(ticketId);
        
        List<SupportTicketCommentDto> result = new ArrayList<>();
        for (SupportTicketComment comment : rootComments) {
            SupportTicketCommentDto dto = toDto(comment);
            // Recursively fetch replies
            dto.setReplies(getReplies(comment.getId()));
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public OffsetPageDto<SupportTicketCommentDto> getCommentsForTicketWithPagination(String ticketId, int offset, int pageSize) {
        log.info("Fetching paginated comments for ticket ID: {}, offset: {}, pageSize: {}", ticketId, offset, pageSize);
        
        // Validate and normalize pagination parameters
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        
        // Verify ticket exists
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Support ticket not found with ID: " + ticketId);
        }
        
        // Create pageable - treat offset as page number (0-based)
        Pageable pageable = PageRequest.of(offset, pageSize);
        
        // Get paginated root comments (no parent)
        Page<SupportTicketComment> rootCommentsPage = commentRepository
                .findByTicketIdAndParentCommentIdIsNullOrderByCommentedAtAsc(ticketId, pageable);
        
        // Convert to DTOs with nested replies
        List<SupportTicketCommentDto> result = new ArrayList<>();
        for (SupportTicketComment comment : rootCommentsPage.getContent()) {
            SupportTicketCommentDto dto = toDto(comment);
            // Recursively fetch replies
            dto.setReplies(getReplies(comment.getId()));
            result.add(dto);
        }
        
        // Get total count of root comments
        long totalRootComments = commentRepository.countByTicketIdAndParentCommentIdIsNull(ticketId);
        
        return new OffsetPageDto<>(offset, pageSize, totalRootComments, result);
    }

    /**
     * Recursively fetch replies to a comment
     */
    private List<SupportTicketCommentDto> getReplies(String parentCommentId) {
        List<SupportTicketComment> replies = commentRepository
                .findByParentCommentIdOrderByCommentedAtAsc(parentCommentId);
        
        List<SupportTicketCommentDto> replyDtos = new ArrayList<>();
        for (SupportTicketComment reply : replies) {
            SupportTicketCommentDto replyDto = toDto(reply);
            // Recursive call for nested replies
            replyDto.setReplies(getReplies(reply.getId()));
            replyDtos.add(replyDto);
        }
        
        return replyDtos;
    }

    @Override
    @Transactional
    public SupportTicketCommentDto createComment(String ticketId, SupportTicketCommentRequestDto request) {
        log.info("Creating comment for ticket ID: {}", ticketId);
        
        try {
            // Verify ticket exists
            SupportTicket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with ID: " + ticketId));
            
            // Get current user context
            CurrentUserContext currentUserContext = userCurrentContextService.getCurrentUserContext();
            String author = currentUserContext.getUserId();
            String authorName = currentUserContext.getUsername();
            
            // Validate comment text
            if (request.getCommentText() == null || request.getCommentText().trim().isEmpty()) {
                throw new IllegalArgumentException("Comment text is required");
            }
            
            // Create comment entity
            SupportTicketComment comment = SupportTicketComment.builder()
                    .id(UUID.randomUUID().toString())
                    .ticketId(ticketId)
                    .commentText(request.getCommentText())
                    .author(author)
                    .authorName(authorName)
                    .attachmentUrl(request.getAttachmentUrl())
                    .commentedAt(Instant.now())
                    .build();
            
            // Handle parent comment (for replies)
            if (request.getParentCommentId() != null && !request.getParentCommentId().trim().isEmpty()) {
                // Verify parent comment exists and belongs to the same ticket
                Optional<SupportTicketComment> parentComment = commentRepository.findById(request.getParentCommentId());
                if (parentComment.isEmpty()) {
                    throw new ResourceNotFoundException("Parent comment not found with ID: " + request.getParentCommentId());
                }
                
                if (!parentComment.get().getTicketId().equals(ticketId)) {
                    throw new IllegalArgumentException("Parent comment does not belong to this ticket");
                }
                
                comment.setParentCommentId(request.getParentCommentId());
            }
            
            // Save comment
            SupportTicketComment savedComment = commentRepository.save(comment);
            
            // Update ticket's updatedDate and updatedBy
            ticket.setUpdatedDate(Instant.now());
            ticket.setUpdatedBy(author);
            ticketRepository.save(ticket);
            
            log.info("Comment created successfully with ID: {}", savedComment.getId());
            
            return toDto(savedComment);
            
        } catch (Exception e) {
            log.error("Error creating comment: {}", e.getMessage(), e);
            throw new UniversalServiceException(MessageKeys.SUPPORT_COMMENT_FAILED, e);
        }
    }

    @Override
    @Transactional
    public void deleteComment(String commentId) {
        log.info("Deleting comment with ID: {}", commentId);
        
        SupportTicketComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));
        
        try {
            // Delete all replies first (recursive deletion)
            deleteReplies(commentId);
            
            // Delete the comment itself
            commentRepository.delete(comment);
            
            // Update ticket's updatedDate
            Optional<SupportTicket> ticket = ticketRepository.findById(comment.getTicketId());
            if (ticket.isPresent()) {
                ticket.get().setUpdatedDate(Instant.now());
                ticketRepository.save(ticket.get());
            }
            
            log.info("Comment deleted successfully with ID: {}", commentId);
            
        } catch (Exception e) {
            log.error("Error deleting comment: {}", e.getMessage(), e);
            throw new UniversalServiceException("Failed to delete comment: " + e.getMessage(), e);
        }
    }

    /**
     * Recursively delete all replies to a comment
     */
    private void deleteReplies(String parentCommentId) {
        List<SupportTicketComment> replies = commentRepository.findByParentCommentIdOrderByCommentedAtAsc(parentCommentId);
        for (SupportTicketComment reply : replies) {
            // Recursively delete nested replies
            deleteReplies(reply.getId());
            // Delete the reply
            commentRepository.delete(reply);
        }
    }

    /**
     * Convert entity to DTO
     */
    private SupportTicketCommentDto toDto(SupportTicketComment comment) {
        return SupportTicketCommentDto.builder()
                .id(comment.getId())
                .ticketId(comment.getTicketId())
                .parentCommentId(comment.getParentCommentId())
                .commentText(comment.getCommentText())
                .author(comment.getAuthor())
                .authorName(comment.getAuthorName())
                .attachmentUrl(comment.getAttachmentUrl())
                .commentedAt(comment.getCommentedAt())
                .replies(new ArrayList<>()) // Will be populated by recursive calls
                .build();
    }
}

