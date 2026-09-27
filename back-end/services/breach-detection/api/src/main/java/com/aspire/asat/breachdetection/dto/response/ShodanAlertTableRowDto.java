package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanAlertTableRowDto {
    private String id;
    private String subject;
    private String fakeDomain;
    private ShodanAlertType type;
    private Instant dateDetected;
    private ShodanAlertSeverity severity;
    private ShodanAlertStatus status;
}
