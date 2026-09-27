package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Response DTO for configurable payload type entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayloadTypeDto {

    @Field("id")
    private String id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isDefault;
    private Boolean isActive;
    private PayloadTypeChannel channel;
    private Instant createdAt;
    private Instant updatedAt;
}
