package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Configurable tone catalog for phishing content (Mongo).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tones")
public class Tone {

    @Id
    private String id;

    private String name;

    private String description;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
