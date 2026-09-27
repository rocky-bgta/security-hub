package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MSP list item response")
public class MspListResponseDto {

    @Schema(description = "MSP ID", example = "msp-id-123")
    private String id;

    @Schema(description = "MSP ID (legacy/display)", example = "MSP001")
    private String mspId;

    @Schema(description = "Organization name", example = "Aspire Digital Ltd.")
    private String organizationName;

    @Schema(description = "Logo URL", example = "https://cdn.aspire.com/logos/aspire.png")
    private String logoUrl;

    @Schema(description = "MSP admin email", example = "admin@aspiredigital.com")
    private String mspAdminEmail;

    @Schema(description = "Contact email", example = "info@aspiredigital.com")
    private String contactEmail;

    @Schema(description = "Phone number", example = "+8801700000000")
    private String phoneNumber;

    @Schema(description = "International dialing code", example = "+880")
    private String phoneCode;

    @Schema(description = "Country", example = "Bangladesh")
    private String country;

    @Schema(description = "State or province", example = "Dhaka")
    private String stateProvince;

    @Schema(description = "Status", example = "ACTIVE")
    private String status;

    @Schema(description = "Created at timestamp")
    private Instant createdAt;

    @Schema(description = "Updated at timestamp")
    private Instant updatedAt;
}

