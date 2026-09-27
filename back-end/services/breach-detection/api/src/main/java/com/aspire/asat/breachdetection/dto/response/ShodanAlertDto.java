package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * One row in the "IP / Domain Breach Alerts" table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanAlertDto {
    private String id;

    private ShodanSubjectType subjectType;

    /** The monitored subject (IP or domain) that produced the alert. */
    private String subject;

    /** "Fake Domain" column in the UI — the suspicious artifact associated with the alert. */
    private String fakeSubject;

    private ShodanAlertType alertType;
    private ShodanAlertSeverity severity;
    private ShodanAlertStatus status;

    private Instant dateDetected;
    private Instant lastSeenAt;

    private String title;
    private String description;
    private String source;

    private List<String> evidence;
}
