package com.aspire.asat.registration.data.netTerm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetTermConfigurationDropdownDTO {
    private String id;
    private String netTermName;
    private Integer netTermInDays;
}

