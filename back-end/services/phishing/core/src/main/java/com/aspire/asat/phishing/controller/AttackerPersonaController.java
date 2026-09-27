package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.AttackerPersonaCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackerPersonaUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller interface for configurable attacker persona catalog.
 */
@Tag(name = "Attacker Personas", description = "APIs for managing phishing attacker persona configuration")
@RequestMapping(value = WebApiUrlConstants.ATTACKER_PERSONAS_PATH, produces = "application/json")
public interface AttackerPersonaController {

    @Operation(summary = "Get list of attacker personas",
            description = "Retrieve paginated list of attacker personas with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attacker personas retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<AttackerPersonaDto>>>> getAttackerPersonas(
            @Parameter(description = "Optional search term (matches name, case-insensitive substring)")
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Filter by effective active entries (includes missing isActive in DB)")
            @RequestParam(defaultValue = "true") boolean isActive,

            @Parameter(description = "Page index (0-based), same semantics as email-templates list")
            @RequestParam(defaultValue = "0") int offset,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Field to sort by (displayOrder, name, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get attacker persona by ID", description = "Retrieve a specific attacker persona by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attacker persona retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Attacker persona not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<AttackerPersonaDto>> getAttackerPersonaById(
            @Parameter(description = "Attacker persona ID") @PathVariable String id
    );

    @Operation(summary = "Create attacker persona", description = "Create a new attacker persona configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attacker persona created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<AttackerPersonaDto>> createAttackerPersona(
            @Valid @RequestBody AttackerPersonaCreateRequest request
    );

    @Operation(summary = "Update attacker persona", description = "Replace an existing attacker persona entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attacker persona updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Attacker persona not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<AttackerPersonaDto>> updateAttackerPersona(
            @Parameter(description = "Attacker persona ID") @PathVariable String id,
            @Valid @RequestBody AttackerPersonaUpdateRequest request
    );

    @Operation(summary = "Delete attacker persona", description = "Delete an attacker persona configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attacker persona deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attacker persona not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteAttackerPersona(
            @Parameter(description = "Attacker persona ID") @PathVariable String id
    );
}
