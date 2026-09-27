package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.clientAdmin.request.*;
import com.aspire.asat.registration.constant.WebApiUrlConstants;

import com.aspire.asat.registration.data.clientAdmin.response.*;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Client Admin Onboarding", description = "Endpoints for onboarding, validation, and email template processing")
@RequestMapping(value = WebApiUrlConstants.CLIENT_ADMIN_API, produces = "application/json")
public interface ClientAdminController {

    @Operation(summary = "Onboard client admin", description = "Accepts all required information and initializes onboarding flow")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client admin onboarded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/onboard")
    ResponseEntity<ApiResponseDto<ClientOnboardingResponseDto>> onboardClientAdmin(@Valid @RequestBody ClientOnboardingRequestDto requestDto);

    @Operation(summary = "Buy now flow", description = "Updates existing client admin account info, adds products, and creates invoice. Does not create new user or client admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buy now flow completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/buy-now")
    ResponseEntity<ApiResponseDto<BuyNowResponseDto>> buyNow(@Valid @RequestBody BuyNowRequestDto requestDto);

    @Operation(summary = "Validate email template syntax", description = "Validates that provided placeholders are correct")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result with message and missing placeholders"),
            @ApiResponse(responseCode = "400", description = "Malformed template or missing fields")
    })
    @PostMapping("/validate-template")
    ResponseEntity<ApiResponseDto<EmailTemplateValidationResponseDto>> validateEmailTemplate(@RequestBody String templateBody);


    @Operation(summary = "Check if username/email already exists", description = "Checks availability of given email for client admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username availability response")
    })
    @GetMapping("/username/exists")
    ResponseEntity<ApiResponseDto<UsernameValidationResponseDto>> checkUsernameExists(@RequestParam @NotBlank String email);

    @Operation(summary = "Activate client license", description = "Activates client license by updating admin status to ACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client license activated"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/activate-license/{clientId}")
    ResponseEntity<ApiResponseDto<Void>> activateClientLicense(@PathVariable("clientId") @NotBlank String clientId);

    @Operation(summary = "Deactivate client license",
            description = "Deactivates invoice-linked client products (PENDING/ACTIVE → INACTIVE). "
                    + "If ClientAdmin status is PENDING, sets it to INACTIVE. Called by billing after coupon-expiry invoice cancel.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client license deactivated"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/deactivate-license/{clientId}")
    ResponseEntity<ApiResponseDto<Void>> deactivateClientLicense(
            @PathVariable("clientId") @NotBlank String clientId,
            @RequestBody(required = false) ClientLicenseDeactivateRequestDto requestDto);

    @Operation(summary = "Expire client license due to unpaid invoice",
            description = "Expires invoice-linked client products (PENDING/ACTIVE → EXPIRED). "
                    + "If ClientAdmin status is PENDING, sets admin and cascaded users to INACTIVE. "
                    + "Called by billing after global unpaid invoice expiry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client license expired"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/expire-license/{clientId}")
    ResponseEntity<ApiResponseDto<Void>> expireClientLicenseDueToUnpaidInvoice(
            @PathVariable("clientId") @NotBlank String clientId,
            @RequestBody(required = false) ClientLicenseDeactivateRequestDto requestDto);

    @Operation(summary = "Fetch product/package IDs assigned to a client",
            description = "Returns product/package IDs mapped to the client admin. Optionally filter by productId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product IDs fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found or no products assigned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products")
    ResponseEntity<ApiResponseDto<ClientProductIdListResponseDto>> getClientProductIds(
            @RequestParam("clientAdminId") @NotBlank String clientAdminId,
            @RequestParam(value = "productId", required = false) String productId);

    @Operation(summary = "Update used license count", description = "Updates the used license count for a specific client product")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Used license count updated successfully"),
            @ApiResponse(responseCode = "404", description = "Client product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/product-license/used-count")
    ResponseEntity<ApiResponseDto<Void>> updateUsedLicenseCount(
            @RequestParam @NotBlank String clientAdminId,
            @RequestParam @NotBlank String productId,
            @RequestParam int usedLicenseCount);

    @PostMapping("/product/reassign")
    ResponseEntity<ApiResponseDto<ClientProductReassignmentResponseDto>> reassignProductsToClient(
            @Valid @RequestBody ClientProductAssignment assignmentRequest);

    @Operation(summary = "List client admins with products", description = "Returns a paginated list of client admins with their associated product details, supporting filtering by search, mspId, status, createdAt, country, and state")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client admins retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<ClientAdminListResponseDto>> listClientAdminsWithProducts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "mspId", required = false) String mspId,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) AdminStatus status,
            @RequestParam(value = "createdAt", required = false) String createdAt,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "state", required = false) String state);

    @Operation(summary = "Get detailed client admin information by ID", description = "Retrieves detailed client admin information with dropdown details and complete product information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detailed client admin information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{clientAdminId}")
    ResponseEntity<ApiResponseDto<ClientAdminDetailedResponseDto>> getClientAdminDetailedById(@PathVariable("clientAdminId") @NotBlank String clientAdminId);

    @Operation(summary = "Update client admin details by ID", description = "Updates client admin information including organization, billing, and contact details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client admin updated successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{clientAdminId}")
    ResponseEntity<ApiResponseDto<ClientAdminWithProductsResponseDto>> updateClientAdminById(
            @PathVariable("clientAdminId") @NotBlank String clientAdminId,
            @Valid @RequestBody ClientAdminUpdateRequestDto updateRequestDto);

    @Operation(summary = "Get assigned client products with pagination and search",
            description = "Retrieves paginated list of client products assigned to a specific client admin with search and sorting. "
                    + "Default ordering is CMS product displayOrder ascending.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products/assigned/{clientAdminId}")
    ResponseEntity<ApiResponseDto<ClientProductsPaginatedResponseDto>> getAssignedClientProducts(
            @PathVariable("clientAdminId") @NotBlank String clientAdminId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "displayOrder", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "asc", required = false) String order);

    @Operation(summary = "Get assigned ACTIVE and PENDING client products with pagination and search",
            description = "Retrieves paginated list of client products with license status ACTIVE or PENDING "
                    + "assigned to a specific client admin with search and sorting. "
                    + "Default ordering is CMS product displayOrder ascending.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client products retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/products/assigned/active-pending/{clientAdminId}")
    ResponseEntity<ApiResponseDto<ClientProductsPaginatedResponseDto>> getAssignedActiveAndPendingClientProducts(
            @PathVariable("clientAdminId") @NotBlank String clientAdminId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "displayOrder", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "asc", required = false) String order);

    @Operation(summary = "Get license statistics for a client",
            description = "Retrieves aggregated license statistics including total licenses, used licenses, "
                    + "and utilization percentage for the current client admin. Optionally filter by productId "
                    + "for a single product. For Phishing Simulation, Smishing Simulation, and Vishing Simulation, "
                    + "usedLicenseCount is the number of unique users who have participated in campaigns for "
                    + "each ClientProduct assignment / productPackageId (a user is counted once per assignment "
                    + "regardless of campaign count). "
                    + "All other products use the existing assigned-seat usedLicenseCount.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/license-statistics")
    ResponseEntity<ApiResponseDto<LicenseStatisticsResponseDto>> getLicenseStatistics(
            @Parameter(description = "Optional product ID to filter statistics for a single product")
            @RequestParam(value = "productId", required = false) String productId);

    @Operation(summary = "Get License Overview summary cards",
            description = "Returns the six License Overview cards: total purchased licenses, assigned seats, "
                    + "available seats, utilization rate (with 30-day change), product-packages expiring within 30 days, "
                    + "and distinct products with ACTIVE licenses. Seat totals are sums of licenseCount and "
                    + "usedLicenseCount on ACTIVE client_products (not campaign unique users).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License overview summary retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "clientAdminId could not be resolved"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/license-overview-summary")
    ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> getLicenseOverviewSummary(
            @Parameter(description = "Client admin ID. Defaults to the current user's client admin / user id.")
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId);

    @Operation(summary = "Get single product-package details by ID", description = "Retrieves detailed information about a specific client product-package assignment by its unique ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product-package details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product-package not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/product/package/detail/{id}")
    ResponseEntity<ApiResponseDto<ClientProductDetailedDto>> getProductPackageDetailById(
            @PathVariable("id") @NotBlank String id);

    @Operation(
            summary = "Update used license count by product-package ID",
            description = "Sets usedLicenseCount on a specific ClientProduct assignment (document id / productPackageId). "
                    + "Only ACTIVE assignments are updated. Used by phishing allocate-licence to sync seat usage.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Used license count updated successfully"),
            @ApiResponse(responseCode = "404", description = "Active client product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/product/package/{id}/used-license-count")
    ResponseEntity<ApiResponseDto<Void>> updateUsedLicenseCountByClientProductId(
            @PathVariable("id") @NotBlank String id,
            @RequestParam int usedLicenseCount);

    @Operation(summary = "Get MSP ID by client admin ID", description = "Retrieves the MSP ID associated with a specific client admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MSP ID retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found or MSP ID not available"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{clientAdminId}/msp-id")
    ResponseEntity<ApiResponseDto<MspIdResponseDto>> getMspIdByClientAdminId(
            @PathVariable("clientAdminId") @NotBlank String clientAdminId);

    @Operation(summary = "Get list of clients or MSPs by country", description = "Retrieves a list of clients or MSPs filtered by country. If isClient is true, returns clients from ClientAdmin model. If isMSP is true, returns MSPs (not yet implemented).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of clients/MSPs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/by-country")
    ResponseEntity<ApiResponseDto<List<ClientMspListItemDto>>> getClientsOrMspsByCountry(
            @RequestParam("country") @NotBlank String country,
            @RequestParam("isClient") boolean isClient);

    @Operation(summary = "Get organization dashboard license statistics", description = "Retrieves detailed license statistics for organization dashboard including total available, allocated, active, and expired licenses for a specific client admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/organization-license-statistics")
    ResponseEntity<ApiResponseDto<OrganizationLicenseStatisticsResponseDto>> getOrganizationLicenseStatistics(
            @RequestParam("clientAdminId") @NotBlank String clientAdminId);

    @Operation(summary = "Get clients dropdown by MSP ID", description = "Retrieves a list of clients (id and organization name) for dropdown selection filtered by MSP ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/get-client-by-msp")
    ResponseEntity<ApiResponseDto<List<ClientDropdownDto>>> getClientsByMspId(
            @RequestParam("mspId") @NotBlank String mspId);

    @Operation(summary = "Update client admin status", description = "Updates the status of a client admin. When set to INACTIVE, all active users under the client admin will be set to INACTIVE with isAdminInactive=true. When set to ACTIVE, all users with isAdminInactive=true will be set to ACTIVE with isAdminInactive=false.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client admin status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{clientAdminId}/status")
    ResponseEntity<ApiResponseDto<Void>> updateClientAdminStatus(
            @PathVariable("clientAdminId") @NotBlank String clientAdminId,
            @Valid @RequestBody ClientAdminStatusUpdateDto statusUpdateDto);

    @Operation(summary = "Suspend or activate AspireUser (client admin)", description = "Updates the status of an AspireUser (client admin) to SUSPEND or ACTIVE. When suspending, saves the suspend reason and cascades suspension to all users under the same client admin. When activating, reactivates previously suspended users. Sends email notification to the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/user/{userId}/status")
    ResponseEntity<ApiResponseDto<Void>> updateAspireUserStatus(
            @PathVariable("userId") @NotBlank String userId,
            @Valid @RequestBody UserSuspendRequestDto requestDto);


}
