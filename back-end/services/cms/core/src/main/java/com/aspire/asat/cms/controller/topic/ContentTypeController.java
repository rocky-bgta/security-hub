package com.aspire.asat.cms.controller.topic;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.topic.ContentTypeReqDto;
import com.aspire.asat.cms.dto.topic.ContentTypeRespDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.CONTENT_TYPE_API)
@Tag(name = "content type API", description = "APIs for managing content types")
public interface ContentTypeController {
    @PostMapping
    @Operation(summary = "Create a new content type", description = "Creates a new content type with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "content type created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "content type with same name already exists")
    })
    ResponseEntity<ApiResponseDto<ContentTypeRespDto>> createContentType(@Valid @RequestBody ContentTypeReqDto contentTypeReqDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update an existing content type", description = "Updates content type details by ID")
    @ApiResponses ({
            @ApiResponse(responseCode = "200", description = "content type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "content type not found"),
            @ApiResponse(responseCode = "409", description = "content type with same name already exists")
    })
    ResponseEntity<ApiResponseDto<ContentTypeRespDto>> updateContentType(@PathVariable("id") String id, @RequestBody ContentTypeReqDto contentTypeReqDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get content type by ID", description = "Retrieves content type details by ID")
    @ApiResponses ({
            @ApiResponse(responseCode = "200", description = "content type retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "content type not found")
    })
    ResponseEntity<ApiResponseDto<ContentTypeRespDto>> getContentTypeById(@PathVariable("id") String id);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete content type by ID", description = "Deletes content type by ID")
    @ApiResponses ({
            @ApiResponse(responseCode = "200", description = "content type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "content type not found")
    })
    ResponseEntity<ApiResponseDto<Void>> deleteContentTypeById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all content types", description = "Retrieves list of all content types")
    @ApiResponse(responseCode = "200", description = "content types retrieved successfully")
    ResponseEntity<ApiResponseDto<List<ContentTypeRespDto>>> getAllContentTypes();
}
