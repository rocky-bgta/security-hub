package com.aspire.asat.registration.data.mspUser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedClientDto {
    private String clientId;
    private String clientName;
    private String contactEmail;
    private String status;
    private Integer licenseCount;
    private Instant createdAt;
}
