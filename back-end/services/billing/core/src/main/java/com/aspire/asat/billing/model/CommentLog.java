package com.aspire.asat.billing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comment_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLog {

    @Id
    private String id;

    private String invoiceId;          // Reference to Invoice
    private Instant date;              // Timestamp of the comment
    private String userName;          // From CurrentUserContext.getUsername()
    private String userRole;          // From CurrentUserContext.getUserType()
    private String comment;           // Comment text
    private String actionTakenId;     // Reference to Action.id
    private String nextStepId;        // Reference to NextStep.id
    @Builder.Default
    private Boolean approved = false; // Approval status, defaults to false
    private Instant createdAt;        // Auto-generated timestamp
}

