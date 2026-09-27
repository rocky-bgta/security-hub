package com.aspire.asat.registration.data.metadata.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTypeUpdateRequestDTO {

    @NotNull(message = "ID is required")
    private String id;
    
    @Size(min = 1, max = 100, message = "Organization type must be between 1 and 100 characters")
    private String name;
    
    private Boolean isActive;
}
