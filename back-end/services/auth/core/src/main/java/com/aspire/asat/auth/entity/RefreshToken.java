package com.aspire.asat.auth.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Document(collection = "refresh_tokens")
public class RefreshToken {
    
    @Id
    private String id = UUID.randomUUID().toString();
    
    /** Lookup field; index is managed in MongoDB (legacy unique index may already exist). */
    private String token;
    
    private String username;
    
    private String userType;
    private Instant expiryDate;
    private Instant createdAt;
    private boolean isRevoked;
    private Instant revokedAt;
    
    // Device information for security
    private String deviceId;
    private String deviceInfo;
    private String ipAddress;
    
    public RefreshToken() {
        this.createdAt = Instant.now();
        this.isRevoked = false;
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
    
    public boolean isValid() {
        return !isExpired() && !isRevoked;
    }
}
