package com.aspire.asat.phishing.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated response DTO following the same pattern as other services.
 */
@Data
@NoArgsConstructor
@Builder
public class AllResponseDto<T> {

    private Integer offset;
    private Integer pageSize;
    private Long total;
    private T items;

    public AllResponseDto(Integer offset, Integer pageSize, Long total, T items) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.total = total;
        this.items = items;
    }
}

