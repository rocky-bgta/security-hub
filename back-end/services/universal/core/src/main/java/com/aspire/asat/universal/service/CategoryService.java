package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.entity.Category;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.category.CategoryDto;
import com.aspire.asat.universal.category.CategoryRequest;
import com.aspire.asat.universal.repository.CategoryRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.data.apiresponse.PaginatedResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ModelMapper modelMapper;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                          UserCurrentContextService userCurrentContextService,
                          ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.userCurrentContextService = userCurrentContextService;
        this.modelMapper = modelMapper;
    }

    public PaginatedResponseDto<CategoryDto> getAllCategories(int limit, int offset) {
        // Create PageRequest for limit-offset pagination
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        // Convert to DTOs
        List<CategoryDto> categoryDtos = categoryPage.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());

        // Return paginated response
        return new PaginatedResponseDto<>(
                categoryDtos,
                categoryPage.getTotalElements(),
                limit,
                offset
        );
    }

    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findByStatus(Status.ACTIVE).stream()
                .map(category -> modelMapper.map(category, CategoryDto.class))
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(String id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(cat -> modelMapper.map(cat, CategoryDto.class)).orElse(null);
    }

    public CategoryDto createCategory(CategoryRequest categoryRequest) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        Category category = modelMapper.map(categoryRequest, Category.class);
        category.setCreatedBy(context.getUserId());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedBy(context.getUserId());
        category.setUpdatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDto.class);
    }

    public CategoryDto updateCategory(String id, CategoryRequest categoryRequest) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        Optional<Category> existingCategoryOptional = categoryRepository.findById(id);
        if (existingCategoryOptional.isPresent()) {
            Category existingCategory = existingCategoryOptional.get();
            modelMapper.map(categoryRequest, existingCategory);
            existingCategory.setId(id); // Ensure ID is not changed
            existingCategory.setUpdatedBy(context.getUserId());
            existingCategory.setUpdatedAt(LocalDateTime.now());

            Category updatedCategory = categoryRepository.save(existingCategory);
            return modelMapper.map(updatedCategory, CategoryDto.class);
        }
        return null;
    }

    public boolean deleteCategory(String id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
