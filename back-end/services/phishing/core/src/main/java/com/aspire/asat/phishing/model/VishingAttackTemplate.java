package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Platform-managed vishing attack template catalog (shared across all clients).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vishing_attack_templates")
public class VishingAttackTemplate {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String script;

    @Builder.Default
    private List<String> variables = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
}
