package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Response DTO for configurable trigger event entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerEventDto {

    @Field("id")
    private String id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isDefault;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
