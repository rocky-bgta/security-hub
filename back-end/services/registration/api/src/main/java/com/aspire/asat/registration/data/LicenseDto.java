package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LicenseDto {

    private UUID licenseId;
    private String licenseName;
    private String licenseType;
    private UUID clientMspId;
    private Date issueDate;
    private Date expireDate;

}
