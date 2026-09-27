package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspPackageSimpleResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspProductSimpleResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for MSP product listing operations (mirrors CMS ClientProductController).
 */
@RequestMapping(value = WebApiUrlConstants.MSP_PRODUCT_API, produces = "application/json")
@Tag(name = "MSP Product Management", description = "APIs for listing MSP-assigned products")
public interface MspProductController {

    @GetMapping("/{mspId}")
    @Operation(
            summary = "Get unique products for MSP",
            description = "Retrieves a list of unique products assigned to an MSP (from msp_products), "
                    + "enriched with product name and thumbnail from the CMS service"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<List<MspProductSimpleResponseDto>>> getUniqueProductsByMspId(
            @Parameter(description = "MSP ID", required = true, example = "msp-123")
            @PathVariable("mspId") @NotBlank String mspId
    );

    @GetMapping("/packages")
    @Operation(
            summary = "Get packages for MSP and product",
            description = "Retrieves a list of packages assigned to an MSP for a specific product based on msp_products data, "
                    + "with package names resolved from CMS. When the caller is an MSP user, mspId is taken from "
                    + "the current context (context mspId if set, otherwise userId); otherwise mspId must be passed as a query param."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Packages retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing mspId"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<List<MspPackageSimpleResponseDto>>> getPackagesByMspIdAndProductId(
            @Parameter(description = "MSP ID (required for non-MSP callers)", example = "msp-123")
            @RequestParam(value = "mspId", required = false) String mspId,
            @Parameter(description = "Product ID", required = true, example = "product-456")
            @RequestParam(value = "productId", required = true) String productId
    );

    @GetMapping("/purchased-package-ids")
    @Operation(
            summary = "Get purchased package IDs for MSP and product",
            description = "Returns package IDs from msp_products for the given MSP and product, "
                    + "without CMS name enrichment. Used by CMS for MSP topic purchase flags."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package IDs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing mspId or productId"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<List<String>>> getPurchasedPackageIdsByMspIdAndProductId(
            @Parameter(description = "MSP ID (required for non-MSP callers)", example = "msp-123")
            @RequestParam(value = "mspId", required = false) String mspId,
            @Parameter(description = "Product ID", required = true, example = "product-456")
            @RequestParam(value = "productId", required = true) String productId
    );
}
