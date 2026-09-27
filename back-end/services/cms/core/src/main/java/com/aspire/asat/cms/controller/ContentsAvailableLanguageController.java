package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageReqDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageRespDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.CONTENTS_AVAILABLE_LANGUAGE_API)
@Tag(name = "Contents Available Language API", description = "CRUD APIs for managing content available languages")
public interface ContentsAvailableLanguageController {

    @PostMapping
    @Operation(summary = "Create language", description = "Creates a new content available language")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Language created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Language with same code already exists")
    })
    ResponseEntity<ApiResponseDto<ContentsAvailableLanguageRespDto>> create(@Valid @RequestBody ContentsAvailableLanguageReqDto dto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update language", description = "Updates language by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Language updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Language not found"),
            @ApiResponse(responseCode = "409", description = "Language with same code already exists")
    })
    ResponseEntity<ApiResponseDto<ContentsAvailableLanguageRespDto>> update(
            @PathVariable("id") String id,
            @Valid @RequestBody ContentsAvailableLanguageReqDto dto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get language by ID", description = "Retrieves language details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Language retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Language not found")
    })
    ResponseEntity<ApiResponseDto<ContentsAvailableLanguageRespDto>> getById(@PathVariable("id") String id);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete language", description = "Deletes language by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Language deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Language not found")
    })
    ResponseEntity<ApiResponseDto<Void>> deleteById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all languages", description = "Retrieves list of content available languages. Use active=true to filter active only.")
    @ApiResponse(responseCode = "200", description = "Languages retrieved successfully")
    ResponseEntity<ApiResponseDto<List<ContentsAvailableLanguageRespDto>>> getAll(
            @RequestParam(value = "active", required = false) Boolean active);
}
