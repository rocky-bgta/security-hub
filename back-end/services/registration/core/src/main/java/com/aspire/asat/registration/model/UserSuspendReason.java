package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity to track user suspension reasons.
 * Stores the reason when a user is suspended.
 */
@Document(collection = "user_suspend_reasons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuspendReason {

    @Id
    private String id;

    private String userId; // User ID (UUID as String)

    private String reason; // Suspend reason ID from SuspendReason dropdown

    private Instant createdAt;

    private String createdBy;
}

