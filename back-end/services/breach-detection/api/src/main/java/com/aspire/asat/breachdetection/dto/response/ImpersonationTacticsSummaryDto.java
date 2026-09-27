package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Backs the "Impersonation Tactics" side panel (Phishing Websites, Spoofed
 * Emails, Typosquatting Domains, Fake Social Profiles).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationTacticsSummaryDto {
    private ShodanSubjectType subjectType;
    private long phishingWebsites;
    private long spoofedEmails;
    private long typosquattingDomains;
    private long fakeSocialProfiles;
}
