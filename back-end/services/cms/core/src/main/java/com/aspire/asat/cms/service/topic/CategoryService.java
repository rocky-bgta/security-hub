package com.aspire.asat.cms.service.topic;

import com.aspire.asat.cms.dto.topic.CategoryReqDto;
import com.aspire.asat.cms.dto.topic.CategoryRespDto;

import java.util.List;

public interface CategoryService {

    CategoryRespDto createCategory(CategoryReqDto categoryReqDto);
    CategoryRespDto updateCategory(String id, CategoryReqDto categoryReqDto);
    CategoryRespDto getById(String id);
    void deleteCategoryById(String id);
    List<CategoryRespDto> getAllCategories();
}
