package com.aspire.asat.registration.model.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "msp_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MspType {

    @Id
    private String id;
    private String name;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}

