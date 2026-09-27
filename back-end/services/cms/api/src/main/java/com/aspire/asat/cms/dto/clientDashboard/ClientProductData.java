package com.aspire.asat.cms.dto.clientDashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Schema(description = "Client product data transferred from registration service")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProductData {
    
    @Schema(description = "Client admin ID", example = "admin-123")
    private String clientAdminId;
    
    @Schema(description = "Product ID", example = "product-456")
    private String productId;
    
    @Schema(description = "Package ID", example = "package-789")
    private String packageId;
    
    @Schema(description = "Product assignment date", example = "2024-01-15T10:30:00Z")
    private Instant assignedAt;
    
    @Schema(description = "Product expiry date", example = "2025-01-15T10:30:00Z")
    private Instant expiryDate;
    
    @Schema(description = "Client admin email", example = "admin@example.com")
    private String email;
}

