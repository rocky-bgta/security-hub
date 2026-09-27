package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.tag.TagReqDto;
import com.aspire.asat.cms.dto.tag.TagRespDto;
import com.aspire.asat.cms.dto.tag.TagStatusUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.TAG_API)
@Tag(name = "Tags API", description = "APIs for managing tags")
public interface TagController {

    @PostMapping
    @Operation(summary = "Create a new tag", description = "Creates a new tag with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Tag with same name already exists")
    })
    ResponseEntity<ApiResponseDto<TagRespDto>> createTag(@Valid @RequestBody TagReqDto request);

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update an existing tag", description = "Updates tag details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "409", description = "Tag with same name already exists")
    })
    ResponseEntity<ApiResponseDto<TagRespDto>> updateTag(
            @PathVariable("id") String id,
            @Valid @RequestBody TagReqDto request);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get tag by ID", description = "Retrieves tag details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    ResponseEntity<ApiResponseDto<TagRespDto>> getTagById(@PathVariable("id") String id);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete tag by ID", description = "Soft-deletes a tag by setting its status to INACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    ResponseEntity<ApiResponseDto<TagRespDto>> deleteTagById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all tags", description = "Retrieves list of all tags")
    @ApiResponse(responseCode = "200", description = "Tags retrieved successfully")
    ResponseEntity<ApiResponseDto<List<TagRespDto>>> getAllTags();

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_ID + "/status")
    @Operation(summary = "Update tag status", description = "Updates tag status (ACTIVE/INACTIVE) by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    ResponseEntity<ApiResponseDto<TagRespDto>> updateTagStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody TagStatusUpdateDto statusUpdateDto);
}
