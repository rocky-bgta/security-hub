package com.aspire.asat.breachdetection.dto.request;

import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Filter request for searching breach findings against the upstream InsecureWeb
 * dark-web API. Mirrors the operator-suffix query syntax accepted by InsecureWeb:
 *
 * <pre>
 *   GET /api/dark-web/{organizationId}/breaches
 *       ?breachStatus.in=OPEN,IN_PROGRESS
 *       &amp;domain.contains=yopmail.com
 *       &amp;email.contains=alice@
 *       &amp;employeesOnly=true
 *       &amp;maskData=true
 *       &amp;page=0
 *       &amp;size=20
 * </pre>
 *
 * All fields are optional (server applies sensible defaults when missing).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsecureWebBreachSearchRequest {

    /** Maps to {@code breachStatus.in} (comma-separated upstream). */
    @Builder.Default
    private Set<InsecureWebBreachStatus> breachStatusIn = new LinkedHashSet<>();

    /** Maps to {@code domain.contains}. */
    private String domainContains;

    /** Maps to {@code email.contains}. */
    private String emailContains;

    /** Maps to {@code employeesOnly}. Defaults to {@code true}. */
    @Builder.Default
    private Boolean employeesOnly = Boolean.TRUE;

    /** Maps to {@code maskData}. Defaults to {@code true}. */
    @Builder.Default
    private Boolean maskData = Boolean.TRUE;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}
