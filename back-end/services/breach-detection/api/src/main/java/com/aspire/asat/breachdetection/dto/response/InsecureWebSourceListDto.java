package com.aspire.asat.breachdetection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsecureWebSourceListDto {
    @Builder.Default
    private List<String> sources = new ArrayList<>();
    private int page;
    private int size;
    private int totalPages;
    private long totalSources;
}
