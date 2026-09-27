package com.aspire.asat.registration.controller.metadata;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.metadata.request.OrganizationTypeRequestDTO;
import com.aspire.asat.registration.data.metadata.request.OrganizationTypeUpdateRequestDTO;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeList;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.ORGANIZATION_TYPE, produces = "application/json")
@Tag(name = "Organization Type Management", description = "Manage organization types")
public interface OrganizationTypeController {

    @Operation(
            summary = "Create a new organization type",
            description = "Creates a new organization type with the provided name"
    )
    @PostMapping
    ResponseEntity<ApiResponseDto<OrganizationTypeResponseDTO>> createOrganizationType(
            @Parameter(description = "Request body containing organization type details", required = true)
            @Valid @RequestBody OrganizationTypeRequestDTO requestDTO
    );

    @Operation(
            summary = "Get organization type by ID",
            description = "Retrieves a specific organization type by its unique identifier"
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<OrganizationTypeResponseDTO>> getOrganizationTypeById(
            @Parameter(description = "Unique identifier of the organization type", required = true)
            @PathVariable String id
    );

    @Operation(
            summary = "Get all organization types",
            description = "Retrieves all active organization types"
    )
    @GetMapping
    ResponseEntity<ApiResponseDto<List<OrganizationTypeList>>> getAllOrganizationTypes();

    @Operation(
            summary = "Update organization type",
            description = "Updates the details and status of an existing organization type"
    )
    @PutMapping
    ResponseEntity<ApiResponseDto<OrganizationTypeResponseDTO>> updateOrganizationType(
            @Parameter(description = "Updated organization type details", required = true)
            @Valid @RequestBody OrganizationTypeUpdateRequestDTO requestDTO
    );

    @Operation(
            summary = "Delete organization type",
            description = "Soft deletes an organization type by setting isActive to false"
    )
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteOrganizationType(
            @Parameter(description = "Unique identifier of the organization type", required = true)
            @PathVariable String id
    );
}
