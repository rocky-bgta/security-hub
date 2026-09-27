package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.dto.topic.CategoryReqDto;
import com.aspire.asat.cms.dto.topic.CategoryRespDto;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.topic.Category;
import com.aspire.asat.cms.repository.topic.CategoryRepository;
import com.aspire.asat.cms.service.topic.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryRespDto createCategory(CategoryReqDto categoryReqDto) {
        log.info("Creating category: {}", categoryReqDto);
        if(categoryRepository.existsByCategoryNameIgnoreCase(categoryReqDto.getCategoryName())){
            throw new DuplicateDataFoundException("Category with name '" + categoryReqDto.getCategoryName() + "' already exists.");
        }
        Category categoryToSave = Category.toCategory(categoryReqDto);
        return Category.toCategoryRespDto(categoryRepository.save(categoryToSave) );
    }

    @Override
    public CategoryRespDto updateCategory(String id, CategoryReqDto categoryReqDto) {
        log.info( "Updating category with id: {} and details: {}", id, categoryReqDto);
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id '" + id + "' not found."));

        if(!existingCategory.getCategoryName().equalsIgnoreCase(categoryReqDto.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(categoryReqDto.getCategoryName())){
            throw new DuplicateDataFoundException("Category with name '" + categoryReqDto.getCategoryName() + "' already exists.");
        }
        updateCategoryData(existingCategory, categoryReqDto);
        return Category.toCategoryRespDto(categoryRepository.save(existingCategory));
    }



    @Override
    public CategoryRespDto getById(String id) {
        log.info("Fetching category with id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id '" + id + "' not found."));
        return Category.toCategoryRespDto(category);
    }

    @Override
    public void deleteCategoryById(String id) {
        log.info("Deleting category with id: {}", id);
        if(!categoryRepository.existsById(id)){
            throw new ResourceNotFoundException("Category with id '" + id + "' not found.");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryRespDto> getAllCategories() {
        log.info("Fetching all categories");
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        List<Category> categories = categoryRepository.findAll(sort);
        return categories.stream()
                .map(Category::toCategoryRespDto)
                .toList();
    }


    private void updateCategoryData(Category existingCategory, CategoryReqDto categoryReqDto) {
        if( categoryReqDto.getCategoryName() != null && !categoryReqDto.getCategoryName().isEmpty()) {
            existingCategory.setCategoryName(categoryReqDto.getCategoryName());
        }
        if( categoryReqDto.getDescription() != null ) {
            existingCategory.setDescription(categoryReqDto.getDescription());
        }
        if( categoryReqDto.getSortOrder() != null ) {
            existingCategory.setSortOrder(categoryReqDto.getSortOrder());
        }
        existingCategory.setUpdatedAt(Instant.now());
    }
}
