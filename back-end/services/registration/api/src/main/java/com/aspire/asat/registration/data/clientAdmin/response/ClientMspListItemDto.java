package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for client or MSP list item")
public class ClientMspListItemDto {

    @Schema(description = "Unique identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String id;

    @Schema(description = "Email address", example = "admin@company.com")
    private String email;

    @Schema(description = "Organization name", example = "Company Name Ltd.")
    private String organizationName;
}

