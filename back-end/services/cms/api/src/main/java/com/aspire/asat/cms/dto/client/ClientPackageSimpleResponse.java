package com.aspire.asat.cms.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for client package listing
 * Contains only essential package information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Package information for a client admin and product")
public class ClientPackageSimpleResponse {
    
    @Schema(description = "Package ID", example = "package-uuid-123")
    private String id;
    
    @Schema(description = "Package name", example = "Premium Package")
    private String name;
}

