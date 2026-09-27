package com.aspire.asat.registration.model.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "countries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {
    
    @Id
    private String id;
    private String code;
    private String phoneCode;
    private String name;
    private Integer displayOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
