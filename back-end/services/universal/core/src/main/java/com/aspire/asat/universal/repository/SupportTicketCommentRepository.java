package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.SupportTicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketCommentRepository extends MongoRepository<SupportTicketComment, String> {
    
    /**
     * Find all root comments (no parent) for a ticket, ordered by creation time
     */
    List<SupportTicketComment> findByTicketIdAndParentCommentIdIsNullOrderByCommentedAtAsc(String ticketId);
    
    /**
     * Find paginated root comments (no parent) for a ticket, ordered by creation time
     */
    Page<SupportTicketComment> findByTicketIdAndParentCommentIdIsNullOrderByCommentedAtAsc(String ticketId, Pageable pageable);
    
    /**
     * Find all replies to a specific comment, ordered by creation time
     */
    List<SupportTicketComment> findByParentCommentIdOrderByCommentedAtAsc(String parentCommentId);
    
    /**
     * Find all comments for a ticket (root and replies), ordered by creation time
     */
    List<SupportTicketComment> findByTicketIdOrderByCommentedAtAsc(String ticketId);
    
    /**
     * Count comments for a ticket
     */
    long countByTicketId(String ticketId);
    
    /**
     * Count root comments (no parent) for a ticket
     */
    long countByTicketIdAndParentCommentIdIsNull(String ticketId);
    
    /**
     * Delete all comments for a ticket
     */
    void deleteByTicketId(String ticketId);
}

