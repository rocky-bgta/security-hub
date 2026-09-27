package com.aspire.asat.registration.model.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class State {
    
    @Id
    private String id;
    private String countryId;
    private String code;
    private String name;
    private Integer displayOrder;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
