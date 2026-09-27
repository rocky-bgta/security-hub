package com.aspire.asat.billing.dto.apiResponses;

import lombok.Data;

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

