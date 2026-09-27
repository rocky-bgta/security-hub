package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating an EmailType.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTypeCreateRequest {

    @NotBlank(message = "Email type name is required")
    @Size(max = 100, message = "Email type name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;
}

