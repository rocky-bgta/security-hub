package com.aspire.asat.cms.dto.topic;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReqDto {
    @NotEmpty( message = "Category name must not be empty")
    private String categoryName;
    private String description;
    @NotNull(message = "Sort order must not be null")
    private Integer sortOrder;
}
