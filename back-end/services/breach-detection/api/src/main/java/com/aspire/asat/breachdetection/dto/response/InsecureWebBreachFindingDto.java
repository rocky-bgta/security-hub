package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.BreachSeverity;
import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsecureWebBreachFindingDto {
    private String id;
    private String externalFindingId;
    private Instant timestamp;
    private String domain;
    private String email;
    private String ipAddress;
    private String username;
    private String password;
    private String phone;
    private String databaseName;
    private String foundIn;
    private String source;
    private String leakName;
    private String breachDescription;
    private String compromisedData;
    private BreachSeverity breachSeverity;
    private String victimDomain;
    private InsecureWebBreachStatus breachStatus;
    private Boolean employee;
    private Integer echoesCount;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private Instant createdAt;
    private Instant updatedAt;
}
