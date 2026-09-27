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
public class ShodanAlertPageDto {
    @Builder.Default
    private List<ShodanAlertDto> items = new ArrayList<>();
    private long total;
    private int page;
    private int size;
}
