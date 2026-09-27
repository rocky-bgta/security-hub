package com.aspire.asat.universal.data.externalresponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndustryDto {
    private String id;
    private String code;
    private String name;
    private Boolean active;
}

