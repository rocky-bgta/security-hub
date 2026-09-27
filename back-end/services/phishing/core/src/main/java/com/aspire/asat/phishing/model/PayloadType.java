package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
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
 * Configurable payload type catalog (Mongo). Distinct from {@link com.aspire.asat.phishing.dto.enums.PayloadType}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payload_types")
public class PayloadType {

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

    @Builder.Default
    private PayloadTypeChannel channel = PayloadTypeChannel.EMAIL;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
