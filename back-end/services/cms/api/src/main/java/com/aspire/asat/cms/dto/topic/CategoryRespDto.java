package com.aspire.asat.cms.dto.topic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRespDto {
    private String id;
    private String categoryName;
    private String description;
    private boolean isActive;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;
}
