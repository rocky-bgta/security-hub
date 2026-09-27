package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.CompanyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Company {

    @Id
    private UUID id;
    private String companyName;
    private String domain;

    public static CompanyDto toCompanyDto(Company company) {
        return CompanyDto.builder().
                id(company.getId()).
                companyName(company.getCompanyName()).
                domain(company.getDomain()).
                build();
    }

    public static Company toCompany(UUID companyId, String companyName, String domain) {
        return Company.builder().
                id(companyId).
                companyName(companyName).
                domain(domain).
                build();
    }

}
