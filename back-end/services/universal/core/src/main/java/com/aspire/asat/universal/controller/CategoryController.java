package com.aspire.asat.universal.controller;

import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.data.apiresponse.PaginatedResponseDto;
import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.universal.category.CategoryRequest;
import com.aspire.asat.universal.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<CategoryDto>>> getAllCategories(
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int offset) {
        PaginatedResponseDto<CategoryDto> categories = categoryService.getAllCategories(pageSize, offset);
        ApiResponseDto<PaginatedResponseDto<CategoryDto>> response = new ApiResponseDto<>(
                "Categories retrieved successfully",
                HttpStatus.OK.value(),
                categories
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<CategoryDto>>> getActiveCategories() {
        List<CategoryDto> categories = categoryService.getActiveCategories();
        ApiResponseDto<List<CategoryDto>> response = new ApiResponseDto<>(
                "Active categories retrieved successfully",
                HttpStatus.OK.value(),
                categories
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CategoryDto>> getCategoryById(@PathVariable String id) {
        CategoryDto category = categoryService.getCategoryById(id);
        if (category != null) {
            ApiResponseDto<CategoryDto> response = new ApiResponseDto<>(
                    "Category retrieved successfully",
                    HttpStatus.OK.value(),
                    category
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<CategoryDto> response = new ApiResponseDto<>(
                "Category not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<CategoryDto>> createCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryDto createdCategory = categoryService.createCategory(categoryRequest);
        ApiResponseDto<CategoryDto> response = new ApiResponseDto<>(
                "Category created successfully",
                HttpStatus.CREATED.value(),
                createdCategory
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CategoryDto>> updateCategory(@PathVariable String id,
                                                    @RequestBody CategoryRequest categoryRequest) {
        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryRequest);
        if (updatedCategory != null) {
            ApiResponseDto<CategoryDto> response = new ApiResponseDto<>(
                    "Category updated successfully",
                    HttpStatus.OK.value(),
                    updatedCategory
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<CategoryDto> response = new ApiResponseDto<>(
                "Category not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteCategory(@PathVariable String id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (deleted) {
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    "Category deleted successfully",
                    HttpStatus.NO_CONTENT.value(),
                    null
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Category not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
