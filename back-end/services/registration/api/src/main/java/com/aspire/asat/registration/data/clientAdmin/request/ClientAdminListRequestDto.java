package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request parameters for listing client admins with filtering and pagination")
public class ClientAdminListRequestDto {

    @Schema(
            description = "Search keyword to filter client admins by organization name, domain, or email",
            example = "aspire"
    )
    private String search;

    @Schema(
            description = "MSP (Managed Service Provider) ID to filter client admins",
            example = "msp-uuid-123"
    )
    private String mspId;

    @Schema(
            description = "Offset value for pagination (page number)",
            example = "0",
            minimum = "0"
    )
    @Min(value = 0, message = "Offset must be 0 or greater")
    private Integer offset;

    @Schema(
            description = "Number of client admins to return per page",
            example = "10",
            minimum = "1"
    )
    @Min(value = 1, message = "Page size must be 1 or greater")
    private Integer pageSize;

    @Schema(
            description = "Filter client admins by status",
            example = "ACTIVE",
            allowableValues = {"PENDING", "ACTIVE", "INACTIVE"}
    )
    private AdminStatus status;

    @Schema(
            description = "Filter client admins by creation date",
            example = "2024-01-15T10:30:00Z"
    )
    private Instant createdAt;

    @Schema(
            description = "Filter client admins by country",
            example = "Bangladesh"
    )
    private String country;

    @Schema(
            description = "Filter client admins by state",
            example = "California"
    )
    private String state;

}
