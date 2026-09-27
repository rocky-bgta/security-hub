package com.aspire.asat.registration.model.dropdown;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "languages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Language {
    
    @Id
    private String id;
    private String code;
    private String displayName;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
