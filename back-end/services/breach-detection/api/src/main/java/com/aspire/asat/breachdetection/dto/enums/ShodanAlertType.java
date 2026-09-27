package com.aspire.asat.breachdetection.dto.enums;

/**
 * Canonical alert types surfaced in the "IP / Domain Breach Detection" UI.
 * Each type also maps to an {@link ImpersonationTactic} for the pie chart.
 */
public enum ShodanAlertType {
    PHISHING_SITE(ImpersonationTactic.PHISHING),
    BOTNET_LOG(ImpersonationTactic.PHISHING),
    EXPLOIT_DATABASE(ImpersonationTactic.PHISHING),
    MALICIOUS_CLONE(ImpersonationTactic.TYPOSQUATTING),
    TYPOSQUATTING_DOMAIN(ImpersonationTactic.TYPOSQUATTING),
    SPOOFED_EMAIL(ImpersonationTactic.SPOOFED_EMAIL),
    FAKE_SOCIAL_PROFILE(ImpersonationTactic.SOCIAL_MEDIA),
    EXPOSED_SERVICE(ImpersonationTactic.PHISHING),
    VULNERABILITY(ImpersonationTactic.PHISHING);

    private final ImpersonationTactic tactic;

    ShodanAlertType(ImpersonationTactic tactic) {
        this.tactic = tactic;
    }

    public ImpersonationTactic getTactic() {
        return tactic;
    }
}
