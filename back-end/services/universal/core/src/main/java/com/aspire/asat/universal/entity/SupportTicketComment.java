package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "support_ticket_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketComment {

    @Id
    private String id;

    private String ticketId; // reference to SupportTicket.id

    private String parentCommentId; // null for root comments, allows threading/replies

    private String commentText;

    private String author; // user ID who created the comment

    private String authorName; // username of the author

    private String attachmentUrl; // optional attachment URL

    private Instant commentedAt;
}

