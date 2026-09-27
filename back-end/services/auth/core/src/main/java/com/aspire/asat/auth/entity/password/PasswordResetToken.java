package com.aspire.asat.auth.entity.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String token;

    /** username/email of the user this token is for */
    private String username;

    private Instant expiryDate;
    private Instant createdAt;
    private Instant updatedAt;

    private boolean used;

    /** Per project convention: createdBy and updatedBy will be userId */
    private String createdBy;
    private String updatedBy;
}

