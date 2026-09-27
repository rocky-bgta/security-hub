package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.data.mspUser.request.MspBuyNowRequestDto;
import com.aspire.asat.registration.data.mspUser.request.MspOnboardingRequestDto;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.mspUser.request.MspUpdateRequestDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientDropdownDto;
import com.aspire.asat.registration.data.mspUser.response.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MSP User Management", description = "Endpoints for MSP user management and onboarding")
@RequestMapping(value = WebApiUrlConstants.MSP_API, produces = "application/json")
public interface MspUserController {

    @Operation(summary = "Onboard MSP user", description = "Handles onboarding flow for an MSP user")
    @PostMapping("/onboarding")
    ResponseEntity<ApiResponseDto<MspOnboardingResponseDto>> onboardMspUser(@Valid @RequestBody MspOnboardingRequestDto requestDto);

    @Operation(summary = "MSP buy-now flow", description = "Adds products to an existing MSP account and creates an invoice for payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buy-now flow completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/buy-now")
    ResponseEntity<ApiResponseDto<MspBuyNowResponseDto>> buyNow(@Valid @RequestBody MspBuyNowRequestDto requestDto);

    @Operation(summary = "Get MSP product catalog", description = "Returns all CMS products merged with MSP assignment status (ENABLED/DISABLED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product catalog retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{mspId}/products/catalog")
    ResponseEntity<ApiResponseDto<MspProductCatalogResponseDto>> getMspProductCatalog(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("mspId") @NotBlank String mspId,
            @Parameter(description = "Search by product name")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by MSP assignment status: ENABLED or DISABLED")
            @RequestParam(value = "mspProductStatus", required = false) String mspProductStatus,
            @Parameter(description = "Page offset (default: 0)")
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)")
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize);

    @Operation(summary = "Get assigned MSP products",
            description = "Returns only products assigned to the MSP, using the same response shape as CMS GET /api/v1/products.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned MSP products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{mspId}/products/assigned")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<MspAssignedProductResponseDto>>>> getAssignedMspProducts(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("mspId") @NotBlank String mspId,
            @Parameter(description = "Search by product name")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by CMS product status")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Page offset (default: 0)")
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)")
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "Sort field (default: displayOrder)")
            @RequestParam(value = "sortBy", defaultValue = "displayOrder", required = false) String sortBy,
            @Parameter(description = "Sort order: asc or desc (default: asc)")
            @RequestParam(value = "order", defaultValue = "asc", required = false) String order);

    @Operation(summary = "Get single MSP product-package details by ID",
            description = "Retrieves detailed information about a specific MSP product-package assignment by its unique msp_products ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP product-package details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP product-package not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/package/detail/{id}")
    ResponseEntity<ApiResponseDto<MspProductDetailedDto>> getMspProductDetailById(
            @Parameter(description = "MSP product ID (msp_products.id)", required = true)
            @PathVariable("id") @NotBlank String id);

    @Operation(summary = "Get all MSPs", description = "Retrieves a paginated list of all MSPs with optional filtering and search")
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<MspListResponseDto>>>> getAllMsp(
            @Parameter(description = "Page offset (default: 0)", example = "0")
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)", example = "10")
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "Search by organization name, contact email, or MSP admin email", example = "aspire")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by status", example = "ACTIVE")
            @RequestParam(value = "status", required = false) MspStatus status);

    @Operation(summary = "Get MSP details", description = "Retrieves detailed information of an MSP by ID")
    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<MspDetailsResponseDto>> getMspDetails(@PathVariable("id") String id);

    @Operation(summary = "Get organization license statistics for an MSP", description = "Retrieves detailed license statistics for organization dashboard including total available, allocated, active, and expired licenses for a specific MSP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/license-statistics")
    ResponseEntity<ApiResponseDto<OrganizationLicenseStatisticsResponseDto>> getLicenseStatistics(
            @RequestParam("mspId") @NotBlank String mspId);

    @Operation(summary = "Get onboarded MSPs", description = "Retrieves a paginated list of onboarded MSPs with status and license counts for dashboard")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboarded MSPs retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/onboarded")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<MspOnboardedListResponseDto>>>> getOnboardedMsps(
            @Parameter(description = "Page offset (default: 0)", example = "0")
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)", example = "10")
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "Search by organization name", example = "Global Brand")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, PENDING)", example = "ACTIVE")
            @RequestParam(value = "status", required = false) MspStatus status,
            @Parameter(description = "Minimum license count filter", example = "10")
            @RequestParam(value = "minLicenses", required = false) Integer minLicenses,
            @Parameter(description = "Maximum license count filter", example = "100")
            @RequestParam(value = "maxLicenses", required = false) Integer maxLicenses);

    @Operation(summary = "Update MSP status", description = "Updates the status of an MSP user (ACTIVE, INACTIVE, LOCKED, PENDING)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP status updated successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/status")
    ResponseEntity<ApiResponseDto<Void>> updateMspStatus(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("id") @NotBlank String id,
            @Parameter(description = "New status for the MSP", required = true, example = "ACTIVE")
            @RequestParam("status") @NotNull MspStatus status);

    @Operation(summary = "Get MSP details", description = "Retrieves detailed information of an MSP by ID")
    @GetMapping(WebApiUrlConstants.PATH_VAR_ID+ "/view")
    ResponseEntity<ApiResponseDto<MspViewDetailsResponseDto>> getMspViewDetails(@PathVariable("id") String id);

    @Operation(summary = "Update MSP details", description = "Updates MSP organization info, products, clients, and credit terms")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP updated successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<MspUpdateResponseDto>> updateMsp(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("id") @NotBlank String id,
            @Valid @RequestBody MspUpdateRequestDto requestDto);

    @Operation(summary = "Update MSP user status from AspireUser", description = "Activates or deactivates an MSP user and cascades status to associated client admins and users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP user status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "MSP user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/user/{userId}/status")
    ResponseEntity<ApiResponseDto<Void>> updateMspUserStatus(
            @Parameter(description = "User ID from AspireUser", required = true)
            @PathVariable("userId") @NotBlank String userId,
            @Valid @RequestBody com.aspire.asat.registration.data.mspUser.request.MspStatusUpdateDto statusUpdateDto);

    @Operation(summary = "Suspend or activate MSP user with suspend reason", description = "Suspends or activates an MSP user (AspireUser) and cascades status changes to all users under the MSP's client admins. When suspending, a suspend reason can be provided. When activating, suspend reason is optional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP user status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input (status must be SUSPEND or ACTIVE)"),
            @ApiResponse(responseCode = "404", description = "MSP user not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/user/{userId}/suspend")
    ResponseEntity<ApiResponseDto<Void>> suspendMspUser(
            @Parameter(description = "User ID from AspireUser", required = true)
            @PathVariable("userId") @NotBlank String userId,
            @Parameter(description = "Request body containing status and optional suspend reason", required = true)
            @Valid @RequestBody com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto requestDto);

    @Operation(summary = "Activate MSP license", description = "Activates MSP license. When invoiceId is provided, only MspProducts linked to that billing invoice are activated; otherwise all PENDING products are activated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP license activated successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/activate-license/{mspId}")
    ResponseEntity<ApiResponseDto<Void>> activateMspLicense(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("mspId") @NotBlank String mspId,
            @Parameter(description = "Billing invoice ID — activates only products on this invoice")
            @RequestParam(value = "invoiceId", required = false) String invoiceId);

    @Operation(summary = "Get clients dropdown for MSP product assign",
            description = "Returns client id and organization name for Step 1 of MSP product assign wizard. "
                    + "Supports country, state, and search filters. MSP users can only access their own MSP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients dropdown retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{mspId}/clients/dropdown")
    ResponseEntity<ApiResponseDto<List<ClientDropdownDto>>> getMspClientsDropdown(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("mspId") @NotBlank String mspId,
            @Parameter(description = "Filter by country ID")
            @RequestParam(value = "country", required = false) String country,
            @Parameter(description = "Filter by state/province ID")
            @RequestParam(value = "state", required = false) String state,
            @Parameter(description = "Search by organization name, domain, or email")
            @RequestParam(value = "search", required = false) String search);

    @Operation(summary = "Get assigned clients for MSP", description = "Retrieves a paginated list of clients assigned to an MSP. Includes client ID, name, contact email, status, license count, and creation date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned clients retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{mspId}/assigned-clients")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<com.aspire.asat.registration.data.mspUser.response.AssignedClientDto>>>> getAssignedClientsForMsp(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("mspId") @NotBlank String mspId,
            @Parameter(description = "Page offset (default: 0)", example = "0")
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)", example = "10")
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize);

    @Operation(summary = "Get topics for MSP products",
            description = "Loads MspProducts for the MSP, extracts unique productId/packageId pairs, "
                    + "and fetches matching topics from CMS. "
                    + "When isAvailable=true (default), returns topics assigned to the MSP; "
                    + "when false, returns topics not assigned (locked). "
                    + "Response always includes total (assigned count) and totalLocked (unassigned count). "
                    + "Supports TopicFilterRequest filters in the body.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{mspId}/msp-product-topics")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto>>>> getClientProductTopicsByMspId(
            @Parameter(description = "MSP ID", required = true)
            @PathVariable("mspId") @NotBlank String mspId,
            @RequestBody(required = false) com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto request);

    @Operation(summary = "Get MSP list with license counts", description = "Retrieves a paginated list of MSPs with aggregated license counts. Supports filtering by status, country, mspTier, and search by organization name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP list retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<com.aspire.asat.registration.data.mspUser.response.MspListWithLicenseResponseDto>>>> getMspListWithLicenses(
            @Parameter(description = "Page offset (default: 0)", example = "0")
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)", example = "10")
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "Search by organization name", example = "Aspire")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Filter by status", example = "ACTIVE")
            @RequestParam(value = "status", required = false) MspStatus status,
            @Parameter(description = "Filter by country", example = "Bangladesh")
            @RequestParam(value = "country", required = false) String country,
            @Parameter(description = "Filter by MSP tier", example = "tier-premium-001")
            @RequestParam(value = "mspTier", required = false) String mspTier);

    @Operation(summary = "Get active license summary", description = "Retrieves total active license count and used license count across all MSPs. Only includes products with ACTIVE licenseStatus.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active license summary retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/active-license-summary")
    ResponseEntity<ApiResponseDto<com.aspire.asat.registration.data.mspUser.response.MspActiveLicenseSummaryResponseDto>> getActiveLicenseSummary();

}
