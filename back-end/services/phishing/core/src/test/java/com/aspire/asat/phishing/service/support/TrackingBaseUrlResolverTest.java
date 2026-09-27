package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.repository.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingBaseUrlResolverTest {

    private static final String CLIENT_ID = "client-1";
    private static final String FALLBACK = "https://dev.aspireelearning.com/gateway/phishing";

    @Mock
    private DomainRepository domainRepository;

    private TrackingBaseUrlResolver resolver;

    @BeforeEach
    void setUp() throws Exception {
        resolver = new TrackingBaseUrlResolver(domainRepository);
        setField("fallbackBaseUrl", FALLBACK);
        setField("campaignPathPrefix", "");
    }

    @Test
    void resolve_campaignDomainSet_usesCampaignHost() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-campaign", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-campaign", "account-storagealert.cloud")));

        String result = resolver.resolve(CLIENT_ID, "domain-campaign", "domain-landing");

        assertEquals("https://account-storagealert.cloud/gateway/phishing", result);
    }

    @Test
    void resolve_devCustomDomain_includesCampaignPathPrefix() throws Exception {
        setField("campaignPathPrefix", "/dev");
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-campaign", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-campaign", "account-storagealert.cloud")));

        String result = resolver.resolve(CLIENT_ID, "domain-campaign", null);

        assertEquals("https://account-storagealert.cloud/dev/gateway/phishing", result);
    }

    @Test
    void resolve_stagingCustomDomain_includesCampaignPathPrefix() throws Exception {
        setField("campaignPathPrefix", "/staging");
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-1", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-1", "custom.example")));

        String result = resolver.resolve(CLIENT_ID, "domain-1", null);

        assertEquals("https://custom.example/staging/gateway/phishing", result);
    }

    @Test
    void resolve_prodCustomDomain_emptyPrefix() throws Exception {
        setField("campaignPathPrefix", "");
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-1", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-1", "custom.example")));

        String result = resolver.resolve(CLIENT_ID, "domain-1", null);

        assertEquals("https://custom.example/gateway/phishing", result);
    }

    @Test
    void resolve_prefixWithoutLeadingSlash_isNormalized() throws Exception {
        setField("campaignPathPrefix", "dev");
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-1", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-1", "phish.example")));

        String result = resolver.resolve(CLIENT_ID, "domain-1", null);

        assertEquals("https://phish.example/dev/gateway/phishing", result);
    }

    @Test
    void resolve_fallbackUnchanged_whenPrefixConfigured() throws Exception {
        setField("campaignPathPrefix", "/dev");

        String result = resolver.resolve(CLIENT_ID, null, null);

        assertEquals(FALLBACK, result);
    }

    @Test
    void resolve_campaignNullLandingPageSet_usesLandingPageHost() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-landing", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-landing", "custom.example")));

        String result = resolver.resolve(CLIENT_ID, null, "domain-landing");

        assertEquals("https://custom.example/gateway/phishing", result);
    }

    @Test
    void resolve_bothNull_returnsFallbackUnchanged() {
        String result = resolver.resolve(CLIENT_ID, null, null);

        assertEquals(FALLBACK, result);
    }

    @Test
    void resolve_blankIds_returnsFallback() {
        String result = resolver.resolve(CLIENT_ID, "  ", "");

        assertEquals(FALLBACK, result);
    }

    @Test
    void validateDomainId_unverified_throws() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-bad", CLIENT_ID))
                .thenReturn(Optional.of(Domain.builder()
                        .id("domain-bad")
                        .domain("bad.example")
                        .status(DomainStatus.UNVERIFIED)
                        .build()));

        assertThrows(PhishingValidationException.class,
                () -> resolver.validateDomainId(CLIENT_ID, "domain-bad"));
    }

    @Test
    void validateDomainId_missing_throws() {
        when(domainRepository.findByIdAndClientIdOrGlobal("missing", CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(PhishingValidationException.class,
                () -> resolver.validateDomainId(CLIENT_ID, "missing"));
    }

    @Test
    void validateDomainId_nullOrBlank_skipsValidation() {
        assertDoesNotThrow(() -> resolver.validateDomainId(CLIENT_ID, null));
        assertDoesNotThrow(() -> resolver.validateDomainId(CLIENT_ID, "  "));
    }

    @Test
    void validateDomainId_verifiedAndLocked_accepts() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-locked", CLIENT_ID))
                .thenReturn(Optional.of(Domain.builder()
                        .id("domain-locked")
                        .domain("locked.example")
                        .status(DomainStatus.VERIFIED_AND_LOCKED)
                        .build()));

        assertDoesNotThrow(() -> resolver.validateDomainId(CLIENT_ID, "domain-locked"));
    }

    @Test
    void resolveHostname_returnsNormalizedHost() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-1", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-1", "Account-StorageAlert.Cloud")));

        assertEquals("account-storagealert.cloud",
                resolver.resolveHostname(CLIENT_ID, "domain-1").orElseThrow());
    }

    @Test
    void resolveShortLinkOrigin_returnsSchemeAndHostOnly() throws Exception {
        setField("campaignPathPrefix", "/dev");
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-1", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-1", "online-banking.tech")));

        assertEquals("https://online-banking.tech",
                resolver.resolveShortLinkOrigin(CLIENT_ID, "domain-1", null).orElseThrow());
    }

    @Test
    void resolveShortLinkOrigin_missingDomain_empty() {
        assertTrue(resolver.resolveShortLinkOrigin(CLIENT_ID, null, null).isEmpty());
    }

    @Test
    void resolveShortLinkOrigin_unverifiedDomain_empty() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-bad", CLIENT_ID))
                .thenReturn(Optional.of(Domain.builder()
                        .id("domain-bad")
                        .domain("online-banking.tech")
                        .status(DomainStatus.UNVERIFIED)
                        .build()));

        assertTrue(resolver.resolveShortLinkOrigin(CLIENT_ID, "domain-bad", null).isEmpty());
    }

    @Test
    void resolveShortLinkOrigin_usesLandingPageDomainWhenCampaignUnset() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-landing", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-landing", "online-banking.tech")));

        assertEquals("https://online-banking.tech",
                resolver.resolveShortLinkOrigin(CLIENT_ID, null, "domain-landing").orElseThrow());
    }

    @Test
    void resolve_swapsHostOnly_preservesFallbackPath() {
        when(domainRepository.findByIdAndClientIdOrGlobal("domain-1", CLIENT_ID))
                .thenReturn(Optional.of(verifiedDomain("domain-1", "phish.example")));

        String result = resolver.resolve(CLIENT_ID, "domain-1", null);

        assertEquals("https://phish.example/gateway/phishing", result);
    }

    private void setField(String name, String value) throws Exception {
        Field field = TrackingBaseUrlResolver.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(resolver, value);
    }

    private Domain verifiedDomain(String id, String hostname) {
        return Domain.builder()
                .id(id)
                .clientId(CLIENT_ID)
                .domain(hostname)
                .status(DomainStatus.VERIFIED)
                .build();
    }
}
