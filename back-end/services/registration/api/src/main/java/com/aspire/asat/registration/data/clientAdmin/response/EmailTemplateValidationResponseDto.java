package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of validating the provided email template")
public class EmailTemplateValidationResponseDto {

    @Schema(description = "True if the template is valid and contains all required placeholders", example = "true")
    private boolean valid;

    @Schema(description = "List of missing placeholders if any", example = "[\"{{USERNAME}}\", \"{{PORTAL_LINK}}\"]")
    private List<String> missingPlaceholders;

    @Schema(description = "Message about the validation result", example = "Template is valid")
    private String message;
}
