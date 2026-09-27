package com.aspire.asat.cms.model.topic;

import com.aspire.asat.cms.dto.topic.CategoryReqDto;
import com.aspire.asat.cms.dto.topic.CategoryRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private String id;
    private String categoryName;
    private String description;
    private boolean isActive;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;


    public static Category toCategory(CategoryReqDto categoryReqDto ) {
        return Category.builder()
                .id(UUID.randomUUID().toString())
                .categoryName(categoryReqDto.getCategoryName())
                .description(categoryReqDto.getDescription())
                .sortOrder(categoryReqDto.getSortOrder())
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

    }

    public static CategoryRespDto toCategoryRespDto(Category category) {
        return CategoryRespDto.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .isActive(category.isActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
