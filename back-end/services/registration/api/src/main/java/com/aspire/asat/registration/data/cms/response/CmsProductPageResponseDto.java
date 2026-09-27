package com.aspire.asat.registration.data.cms.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsProductPageResponseDto {
    private int offset;
    private int pageSize;
    private int total;
    private List<CmsFullProductResponseDto> items;
}
