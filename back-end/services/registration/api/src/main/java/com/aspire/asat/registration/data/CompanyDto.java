package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CompanyDto {

    private UUID id;
    private String companyName;
    private String domain;

}