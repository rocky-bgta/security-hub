package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.ShortUrl;
import com.aspire.asat.phishing.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceImplTest {

    private static final String ORIGIN = "https://online-banking.tech";
    private static final String ENV_PREFIX = "01";
    private static final String ORIGINAL_URL =
            "https://online-banking.tech/dev/gateway/phishing/t/phish/225cb258f8404dde9541e51c3af83662";
    private static final Pattern SHORT_CODE = Pattern.compile(
            Pattern.quote(ENV_PREFIX) + "[" + Pattern.quote(UrlShortenerServiceImpl.ALPHABET) + "]{8}");
    private static final String EXAMPLE_CODE = "01k7Qm2Nxp";

    @Mock
    private ShortUrlRepository shortUrlRepository;

    private UrlShortenerServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new UrlShortenerServiceImpl(shortUrlRepository);
        setField(service, "codeLength", 8);
        setField(service, "envPrefix", ENV_PREFIX);
    }

    @Test
    void shorten_buildsHostRootPathUrlWithEnvPrefix() {
        when(shortUrlRepository.findByTrackingId("225cb258f8404dde9541e51c3af83662"))
                .thenReturn(Optional.empty());
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CampaignRecipient recipient = recipient("225cb258f8404dde9541e51c3af83662");
        String shortUrl = service.shorten(ORIGINAL_URL, ORIGIN, recipient);

        ArgumentCaptor<ShortUrl> captor = ArgumentCaptor.forClass(ShortUrl.class);
        verify(shortUrlRepository).save(captor.capture());
        ShortUrl saved = captor.getValue();

        assertTrue(shortUrl.startsWith(ORIGIN + "/"));
        assertEquals(ORIGIN.length() + 1 + 10, shortUrl.length());
        assertTrue(SHORT_CODE.matcher(saved.getShortCode()).matches());
        assertTrue(saved.getShortCode().startsWith(ENV_PREFIX));
        assertEquals(shortUrl, saved.getShortUrl());
        assertEquals(ORIGINAL_URL, saved.getOriginalUrl());
        assertEquals(recipient.getTrackingId(), saved.getTrackingId());
        assertTrue(!shortUrl.contains("/gateway/phishing"));
        assertTrue(!shortUrl.contains("/t/phish/"));
    }

    @Test
    void shorten_reusesExistingMappingByTrackingId() {
        ShortUrl existing = ShortUrl.builder()
                .shortCode(EXAMPLE_CODE)
                .shortUrl(ORIGIN + "/" + EXAMPLE_CODE)
                .trackingId("trk-1")
                .originalUrl(ORIGINAL_URL)
                .build();
        when(shortUrlRepository.findByTrackingId("trk-1")).thenReturn(Optional.of(existing));

        String result = service.shorten(ORIGINAL_URL, ORIGIN, recipient("trk-1"));

        assertEquals(ORIGIN + "/" + EXAMPLE_CODE, result);
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shorten_retriesOnShortCodeCollisionThenSucceeds() {
        when(shortUrlRepository.findByTrackingId("trk-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenThrow(new DuplicateKeyException("shortCode"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = service.shorten(ORIGINAL_URL, ORIGIN, recipient("trk-1"));

        assertTrue(result.startsWith(ORIGIN + "/"));
        assertEquals(ORIGIN.length() + 1 + 10, result.length());
        verify(shortUrlRepository, times(2)).save(any(ShortUrl.class));
    }

    @Test
    void shorten_returnsExistingWhenTrackingIdRaceOccurs() {
        ShortUrl raced = ShortUrl.builder()
                .shortCode(EXAMPLE_CODE)
                .shortUrl(ORIGIN + "/" + EXAMPLE_CODE)
                .trackingId("trk-1")
                .build();
        when(shortUrlRepository.findByTrackingId("trk-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(raced));
        when(shortUrlRepository.save(any(ShortUrl.class)))
                .thenThrow(new DuplicateKeyException("trackingId"));

        String result = service.shorten(ORIGINAL_URL, ORIGIN, recipient("trk-1"));

        assertEquals(ORIGIN + "/" + EXAMPLE_CODE, result);
    }

    @Test
    void previewUrl_matchesShortUrlLengthWithoutPersisting() {
        String preview = service.previewUrl(ORIGIN);

        assertEquals(ORIGIN + "/01XXXXXXXX", preview);
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shorten_rejectsMissingEnvPrefix() throws Exception {
        setField(service, "envPrefix", "");
        assertThrows(IllegalStateException.class,
                () -> service.shorten(ORIGINAL_URL, ORIGIN, recipient("trk-1")));
    }

    @Test
    void shorten_rejectsInvalidEnvPrefix() throws Exception {
        setField(service, "envPrefix", "99");
        assertThrows(IllegalStateException.class,
                () -> service.shorten(ORIGINAL_URL, ORIGIN, recipient("trk-1")));
    }

    @Test
    void resolve_returnsOriginalUrl() {
        when(shortUrlRepository.findByShortCode(EXAMPLE_CODE))
                .thenReturn(Optional.of(ShortUrl.builder()
                        .shortCode(EXAMPLE_CODE)
                        .originalUrl(ORIGINAL_URL)
                        .build()));

        assertEquals(ORIGINAL_URL, service.resolve(EXAMPLE_CODE).orElseThrow());
    }

    @Test
    void resolve_unknownCode_empty() {
        when(shortUrlRepository.findByShortCode("missing")).thenReturn(Optional.empty());
        assertTrue(service.resolve("missing").isEmpty());
    }

    @Test
    void shorten_rejectsMissingOrigin() {
        assertThrows(IllegalArgumentException.class,
                () -> service.shorten(ORIGINAL_URL, "  ", recipient("trk-1")));
    }

    @Test
    void shorten_rejectsMissingTrackingId() {
        CampaignRecipient recipient = CampaignRecipient.builder().id("rec-1").trackingId(" ").build();
        assertThrows(IllegalArgumentException.class,
                () -> service.shorten(ORIGINAL_URL, ORIGIN, recipient));
    }

    @Test
    void shorten_rejectsBlankOriginalUrl() {
        assertThrows(IllegalArgumentException.class,
                () -> service.shorten("  ", ORIGIN, recipient("trk-1")));
    }

    @Test
    void shorten_stripsTrailingSlashOnOrigin() {
        when(shortUrlRepository.findByTrackingId("trk-1")).thenReturn(Optional.empty());
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String shortUrl = service.shorten(ORIGINAL_URL, ORIGIN + "/", recipient("trk-1"));

        assertTrue(shortUrl.startsWith(ORIGIN + "/"));
        assertFalse(shortUrl.contains(ORIGIN + "//"));
        assertEquals(ORIGIN.length() + 1 + 10, shortUrl.length());
    }

    @Test
    void resolve_blankCode_empty() {
        assertTrue(service.resolve("  ").isEmpty());
        assertTrue(service.resolve(null).isEmpty());
        verify(shortUrlRepository, never()).findByShortCode(any());
    }

    @Test
    void resolve_trimsShortCode() {
        when(shortUrlRepository.findByShortCode(EXAMPLE_CODE))
                .thenReturn(Optional.of(ShortUrl.builder()
                        .shortCode(EXAMPLE_CODE)
                        .originalUrl(ORIGINAL_URL)
                        .build()));

        assertEquals(ORIGINAL_URL, service.resolve("  " + EXAMPLE_CODE + "  ").orElseThrow());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = UrlShortenerServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static CampaignRecipient recipient(String trackingId) {
        return CampaignRecipient.builder()
                .id("rec-1")
                .campaignId("camp-1")
                .clientId("client-1")
                .trackingId(trackingId)
                .build();
    }
}
