package com.aspire.asat.notification.dto;

import lombok.Data;

/**
 * Paginated response DTO following the same pattern as other services
 */
@Data
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
