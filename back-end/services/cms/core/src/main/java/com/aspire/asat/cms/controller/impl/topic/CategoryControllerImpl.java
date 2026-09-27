package com.aspire.asat.cms.controller.impl.topic;

import com.aspire.asat.cms.controller.topic.CategoryController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.topic.CategoryReqDto;
import com.aspire.asat.cms.dto.topic.CategoryRespDto;
import com.aspire.asat.cms.service.topic.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CategoryControllerImpl implements CategoryController {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<ApiResponseDto<CategoryRespDto>> createCategory(CategoryReqDto categoryReqDto) {
        CategoryRespDto createdCategory = categoryService.createCategory(categoryReqDto);
        ApiResponseDto<CategoryRespDto> response = new ApiResponseDto<>("Category created successfully", 201, createdCategory);
        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CategoryRespDto>> updateCategory(String id, CategoryReqDto categoryReqDto) {
        CategoryRespDto updatedCategory = categoryService.updateCategory(id, categoryReqDto);
        ApiResponseDto<CategoryRespDto> response = new ApiResponseDto<>("Category updated successfully", 200, updatedCategory);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<CategoryRespDto>> getCategoryById(String id) {
        CategoryRespDto category = categoryService.getById(id);
        ApiResponseDto<CategoryRespDto> response = new ApiResponseDto<>("Category fetched successfully", 200, category);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteCategoryById(String id) {
        categoryService.deleteCategoryById(id);
        ApiResponseDto<Void> response = new ApiResponseDto<>("Category deleted successfully", 200, null);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<CategoryRespDto>>> getAllCategories() {
        List<CategoryRespDto> categories = categoryService.getAllCategories();
        ApiResponseDto<List<CategoryRespDto>> response = new ApiResponseDto<>("Categories fetched successfully", 200, categories);
        return ResponseEntity.ok(response);
    }
}
