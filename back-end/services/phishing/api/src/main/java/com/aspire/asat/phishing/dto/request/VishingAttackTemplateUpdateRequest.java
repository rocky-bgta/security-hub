package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a vishing attack template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingAttackTemplateUpdateRequest {

    @NotBlank(message = "Attack template name is required")
    @Size(max = 150, message = "Attack template name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Attack template script is required")
    private String script;
}
