package com.aspire.asat.registration.data.apiResponses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class AllResponseDto<T> {

    private Integer offset;
    private Integer pageSize;
    private Long total;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long totalLocked;
    private T items;

    public AllResponseDto(Integer offset, Integer pageSize, Long total, T items) {
        this(offset, pageSize, total, null, items);
    }

    public AllResponseDto(Integer offset, Integer pageSize, Long total, Long totalLocked, T items) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.total = total;
        this.totalLocked = totalLocked;
        this.items = items;
    }

}
