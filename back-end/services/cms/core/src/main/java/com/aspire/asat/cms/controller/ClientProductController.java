package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.ClientPackageSimpleResponse;
import com.aspire.asat.cms.dto.client.ClientProductSimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller interface for client product operations
 */
@RequestMapping(value = WebApiUrlConstants.CLIENT_PRODUCT_API, produces = "application/json")
@Tag(name = "Client Product Management", description = "APIs for managing client products")
public interface ClientProductController {

    /**
     * Get list of unique products assigned to a client admin
     * 
     * @param clientAdminId the client admin ID
     * @return ResponseEntity containing list of unique products with id, productName, and thumbnailUrl
     */
    @GetMapping("/{clientAdminId}")
    @Operation(
        summary = "Get unique products for client admin",
        description = "Retrieves a list of unique products assigned to a client admin based on ClientProductReplica data"
    )
    ResponseEntity<ApiResponseDto<List<ClientProductSimpleResponse>>> getUniqueProductsByClientAdminId(
        @Parameter(description = "Client admin ID", required = true, example = "admin-123")
        @PathVariable String clientAdminId
    );

    /**
     * Get list of packages assigned to a client admin for a specific product
     * Client admin ID is retrieved from user context
     * 
     * @param productId the product ID
     * @return ResponseEntity containing list of packages with id and name
     */
    @GetMapping("/packages")
    @Operation(
        summary = "Get packages for client admin and product",
        description = "Retrieves a list of packages assigned to the current client admin (from context) for a specific product based on ClientProductReplica data"
    )
    ResponseEntity<ApiResponseDto<List<ClientPackageSimpleResponse>>> getPackagesByClientAdminIdAndProductId(
        @Parameter(description = "Product ID", required = true, example = "product-456")
        @RequestParam(value = "productId", required = true) String productId
    );
}

