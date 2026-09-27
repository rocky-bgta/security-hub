package com.aspire.asat.billing.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "credits")
public class Credit {

    @Id
    private String id;

    private String clientId;        // Reference to the client who owns the credits
    private Double availableAmount; // Remaining credits available
    private boolean isActive;       // Whether the credit balance is usable
    private Instant createdAt = Instant.now();
    private Instant updatedAt;
    private Instant expirationDate;    // Optional expiration for credits

    private String remarks;         // Optional notes (e.g., "Recharged via admin panel")
}
