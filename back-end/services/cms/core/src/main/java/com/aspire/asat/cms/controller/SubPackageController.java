package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageDetailsResponseDTO;
import com.aspire.asat.cms.dto.subPackage.SubPackageAssignedUserDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageRequestDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageResponseDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageUpdateDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageDetailResponseDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageCreationRequestDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageResponseDto;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.SUB_PACKAGE_API, produces = "application/json")
@Tag(name = "SubPackage Management", description = "APIs for managing sub-packages")
public interface SubPackageController {

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Create a new sub-package", description = "Creates a new sub-package with the provided details.")
    ResponseEntity<ApiResponseDto<SubPackageResponseDto>> createSubPackage(
            @Valid @RequestBody SubPackageRequestDto requestDto
    );

    @PostMapping(value = "/trial", consumes = "application/json")
    @Operation(summary = "Create a trial sub-package", description = "Creates a new trial sub-package for a product and package with the specified trial period.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trial sub-package created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient topics"),
            @ApiResponse(responseCode = "404", description = "Product or package not found")
    })
    ResponseEntity<ApiResponseDto<SubPackageResponseDto>> createTrialSubPackage(
            @Valid @RequestBody TrialSubPackageCreationRequestDto requestDto
    );

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get sub-package by ID with details", description = "Retrieves a specific sub-package by its ID with product and package details.")
    ResponseEntity<ApiResponseDto<SubPackageDetailResponseDto>> getSubPackageById(
            @Parameter(description = "SubPackage ID") @PathVariable("id") String id
    );

    @GetMapping("/{id}/assigned-users")
    @Operation(summary = "Get users assigned to a sub-package",
            description = "Returns a paginated list of users assigned to the given sub-package from user_subpackages "
                    + "(ordered by assignedDate descending), enriched with AspireUser email and fullName from Registration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assigned users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SubPackageAssignedUserDto>>>> getAssignedUsersBySubPackageId(
            @Parameter(description = "SubPackage ID") @PathVariable("id") @NotBlank String id,
            @Parameter(description = "Page offset (default: 0)")
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @Parameter(description = "Page size (default: 10)")
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    );

    @GetMapping
    @Operation(summary = "Get all sub-packages", description = "Retrieves a paginated list of all sub-packages with optional search and filtering.")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SubPackageResponseDto>>>> getAllSubPackages(
            @Parameter(description = "Search term for sub-package name") 
            @RequestParam(value = "search", required = false) String search,
            
            @Parameter(description = "Page offset (default: 0)") 
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            
            @Parameter(description = "Page size (default: 10)") 
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            
            @Parameter(description = "Sort by field (default: createdAt)") 
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort order (asc/desc, default: desc)") 
            @RequestParam(value = "order", defaultValue = "desc") String order,
            
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, DRAFT, ARCHIVED)") 
            @RequestParam(value = "status", required = false) SubPackageStatus status,
            
            @Parameter(description = "Filter by product ID") 
            @RequestParam(value = "productId", required = false) String productId,
            
            @Parameter(description = "Filter by client admin ID") 
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId,

            @Parameter(description = "Filter by MSP ID; when provided, returns sub-packages for all client admins under the MSP")
            @RequestParam(value = "mspId", required = false) String mspId
    );

    @GetMapping(WebApiUrlConstants.EXISTS)
    @Operation(summary = "Check if sub-package name exists for a client", description = "Returns true when a sub-package with same name already exists for the client.")
    ResponseEntity<ApiResponseDto<Boolean>> isSubPackageNameExistsForClient(
            @Parameter(description = "Sub-package name") @RequestParam(value = "subPackageName", required = true) String subPackageName,
            @Parameter(description = "Client admin ID (optional; current user ID is used if omitted)") @RequestParam(value = "clientAdminId", required = false) String clientAdminId
    );

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update sub-package", description = "Updates an existing sub-package with the provided details.")
    ResponseEntity<ApiResponseDto<SubPackageResponseDto>> updateSubPackage(
            @Parameter(description = "SubPackage ID") @PathVariable("id") String id,
            @Valid @RequestBody SubPackageUpdateDto updateDto
    );

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete sub-package", description = "Soft deletes a sub-package by setting its status to INACTIVE.")
    ResponseEntity<ApiResponseDto<SubPackageResponseDto>> deleteSubPackage(
            @Parameter(description = "SubPackage ID") @PathVariable("id") String id
    );

    // ========== CLIENT-FACING SUBPACKAGE APIs ==========

    @GetMapping("/client/user-subpackage")
    @Operation(summary = "Get user subpackage details", description = "Retrieves user subpackage information based on userId and subPackageId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User subpackage details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User subpackage not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    ResponseEntity<ApiResponseDto<UserSubPackageDetailsResponseDTO>> getUserSubPackageDetails(
            @Parameter(description = "User ID") @RequestParam @NotBlank String userId,
            @Parameter(description = "SubPackage ID") @RequestParam @NotBlank String subPackageId
    );

    // ========== TRIAL SUBPACKAGE APIs ==========

    @GetMapping("/trial/sub-packages")
    @Operation(summary = "Get all trial sub-packages", description = "Retrieves a paginated list of all trial sub-packages with showInSite field and optional search and filtering.")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TrialSubPackageResponseDto>>>> getAllTrialSubPackages(
            @Parameter(description = "Search term for sub-package name")
            @RequestParam(value = "search", required = false) String search,

            @Parameter(description = "Page offset (default: 0)")
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,

            @Parameter(description = "Page size (default: 10)")
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,

            @Parameter(description = "Sort by field (default: createdAt)")
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort order (asc/desc, default: desc)")
            @RequestParam(value = "order", defaultValue = "desc") String order,

            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, DRAFT, ARCHIVED)")
            @RequestParam(value = "status", required = false) SubPackageStatus status,

            @Parameter(description = "Filter by product ID")
            @RequestParam(value = "productId", required = false) String productId,

            @Parameter(description = "Filter by client admin ID")
            @RequestParam(value = "clientAdminId", required = false) String clientAdminId
    );

    @PostMapping(value = "/by-package-ids", consumes = "application/json")
    @Operation(summary = "Get sub-packages by package IDs",
            description = "Retrieves sub-packages for the given package IDs. Prefers client-specific sub-packages when clientAdminId is provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sub-packages retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    ResponseEntity<ApiResponseDto<List<SubPackageResponseDto>>> getSubPackagesByPackageIds(
            @Valid @RequestBody com.aspire.asat.cms.dto.subPackage.SubPackagesByPackageIdsRequest request
    );
}
