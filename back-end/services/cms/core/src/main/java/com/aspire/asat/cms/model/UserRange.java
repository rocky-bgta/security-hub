package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_ranges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRange {
    @Id
    private String id;

    private String rangeName;          // e.g., "10-100", "101-500"

    private Integer minUsers;          // Minimum number of users (inclusive)

    private Integer maxUsers;          // Maximum number of users (inclusive), null means unlimited

    private String description;        // Optional description

    private Instant createdAt;
    private Instant updatedAt;

    @Builder.Default
    private Boolean isActive = true;    // Soft delete flag

    @Builder.Default
    private Boolean isDefault = false;   // Default range flag
}

