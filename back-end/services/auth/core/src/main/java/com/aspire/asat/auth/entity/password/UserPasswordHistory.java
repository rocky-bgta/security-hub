package com.aspire.asat.auth.entity.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores hashed password history for a user to enforce "do not reuse last N passwords".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_password_history")
@CompoundIndex(name = "userId_createdAt", def = "{'userId': 1, 'createdAt': -1}")
public class UserPasswordHistory {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /** Aspire user ID (UUID string). */
    private String userId;
    /** BCrypt (or current) hashed password. */
    private String passwordHash;
    private Instant createdAt;
}
