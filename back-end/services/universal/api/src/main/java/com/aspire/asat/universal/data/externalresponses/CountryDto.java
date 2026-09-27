package com.aspire.asat.universal.data.externalresponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryDto {
    private String id;
    private String code;
    private String name;
    private Integer displayOrder;
    private Boolean active;
}

