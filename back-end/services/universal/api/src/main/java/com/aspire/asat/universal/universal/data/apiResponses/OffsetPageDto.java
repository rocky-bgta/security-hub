package com.aspire.asat.universal.universal.data.apiResponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OffsetPageDto<T> {
    private int offset;
    private int pageSize;
    private long total;
    private List<T> items;
}

