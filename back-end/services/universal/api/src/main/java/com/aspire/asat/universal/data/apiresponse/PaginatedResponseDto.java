package com.aspire.asat.universal.data.apiresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PaginatedResponseDto<T> {
    @JsonProperty("items")
    private List<T> items;

    @JsonProperty("total")
    private long total;

    @JsonProperty("pageSize")
    private int pageSize;

    @JsonProperty("offset")
    private int offset;

    public PaginatedResponseDto(List<T> data, long totalElements, int limit, int offset) {
        this.items = data;
        this.total = totalElements;
        this.pageSize = limit;
        this.offset = offset;
    }
}
