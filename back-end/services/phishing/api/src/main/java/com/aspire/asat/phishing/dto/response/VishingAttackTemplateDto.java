package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for vishing attack templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingAttackTemplateDto {

    @Field("id")
    private String id;
    private String name;
    private String script;
    private List<String> variables;
    private Instant createdAt;
    private Instant updatedAt;
}
