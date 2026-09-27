package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.LicenseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class License {

    @Id
    private UUID licenseId;
    private String licenseName;
    private String licenseType;
    private UUID clientMspId;
    private Date issueDate;
    private Date expireDate;

    public static License toLicense(LicenseDto licenseDto) {
        return License.builder().
                licenseId(UUID.randomUUID()).
                licenseName(licenseDto.getLicenseName()).
                licenseType(licenseDto.getLicenseType()).
                clientMspId(licenseDto.getClientMspId()).
                issueDate(licenseDto.getIssueDate()).
                expireDate(licenseDto.getExpireDate()).
                build();
    }

    public static LicenseDto toLicenseDto(License license) {
        return LicenseDto.builder().
                licenseId(license.getLicenseId()).
                licenseName(license.getLicenseName()).
                licenseType(license.getLicenseType()).
                clientMspId(license.getClientMspId()).
                issueDate(license.getIssueDate()).
                expireDate(license.getExpireDate()).
                build();
    }

    public static License toUpdateLicense(LicenseDto licenseDto) {
        return License.builder().
                licenseId(licenseDto.getLicenseId()).
                licenseName(licenseDto.getLicenseName()).
                licenseType(licenseDto.getLicenseType()).
                clientMspId(licenseDto.getClientMspId()).
                issueDate(licenseDto.getIssueDate()).
                expireDate(licenseDto.getExpireDate()).
                build();
    }

}