package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Configurable AI model catalog entry (per provider).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_models")
public class AiModel {

    @Id
    private String id;

    private String name;

    @Indexed
    private AiProviderType providerType;

    @Builder.Default
    private boolean isDefault = false;

    @Builder.Default
    private boolean isActive = true;

    private Instant createdAt;
}
