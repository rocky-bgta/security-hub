package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "net_term_configurations")
public class NetTermConfiguration {

    @Id
    private String id;

    private String netTermName;       // e.g., "Net Term 15 Days", "Net Term 30 Days"
    private Integer netTermInDays;    // Number of days for the net term (e.g., 15, 30, 45)
    private Boolean isActive;          // true = Active, false = Inactive (default: true)

    // System-managed fields
    private String createdBy;         // ID of the user who created this record
    private Instant createdAt;         // Timestamp when created
    private String updatedBy;          // ID of the user who last updated this record
    private Instant updatedAt;        // Timestamp when last updated
}

