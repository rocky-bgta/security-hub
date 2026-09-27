package com.aspire.asat.cms.controller.topic;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.topic.CategoryReqDto;
import com.aspire.asat.cms.dto.topic.CategoryRespDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.CATEGORY_API)
@Tag(name = "Category API", description = "APIs for managing categories")
public interface CategoryController {

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "category with same name already exists")
    })
    ResponseEntity<ApiResponseDto<CategoryRespDto>> createCategory(@Valid @RequestBody CategoryReqDto categoryReqDto);

    @PutMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update an existing category", description = "Updates category details by ID")
    @ApiResponses ({
            @ApiResponse(responseCode = "200", description = "category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "category not found"),
            @ApiResponse(responseCode = "409", description = "category with same name/code already exists")
    })
    ResponseEntity<ApiResponseDto<CategoryRespDto>> updateCategory(@PathVariable("id") String id, @RequestBody CategoryReqDto categoryReqDto);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get category by ID", description = "Retrieves category details by ID")
    @ApiResponses ({
            @ApiResponse(responseCode = "200", description = "category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "category not found")
    })
    ResponseEntity<ApiResponseDto<CategoryRespDto>> getCategoryById(@PathVariable("id") String id);

    @DeleteMapping(value = WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete category by ID", description = "Deletes category by ID")
    @ApiResponses ({
            @ApiResponse(responseCode = "200", description = "category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "category not found")
    })
    ResponseEntity<ApiResponseDto<Void>> deleteCategoryById(@PathVariable("id") String id);

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves list of all categories")
    @ApiResponse(responseCode = "200", description = "categories retrieved successfully")
    ResponseEntity<ApiResponseDto<List<CategoryRespDto>>> getAllCategories();
}
