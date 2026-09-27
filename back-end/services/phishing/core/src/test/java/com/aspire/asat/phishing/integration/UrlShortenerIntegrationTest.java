package com.aspire.asat.phishing.integration;

import com.aspire.asat.phishing.controller.impl.TrackingControllerImpl;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.ShortUrl;
import com.aspire.asat.phishing.repository.ShortUrlRepository;
import com.aspire.asat.phishing.service.TrackingService;
import com.aspire.asat.phishing.service.impl.UrlShortenerServiceImpl;
import com.aspire.asat.phishing.service.support.RedirectUrlSanitizer;
import com.aspire.asat.phishing.service.support.SubmissionAwarenessPageRenderer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Testcontainers MongoDB integration for per-recipient SMS short links.
 * Requires Docker; skipped automatically when Docker is unavailable.
 */
@Testcontainers(disabledWithoutDocker = true)
class UrlShortenerIntegrationTest {

    private static final String ORIGIN = "https://online-banking.tech";
    private static final String ORIGINAL_A =
            "https://online-banking.tech/dev/gateway/phishing/t/phish/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String ORIGINAL_B =
            "https://online-banking.tech/dev/gateway/phishing/t/phish/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    private TrackingService trackingService;
    private SubmissionAwarenessPageRenderer awarenessRenderer;

    private MongoClient mongoClient;
    private MongoTemplate mongoTemplate;
    private UrlShortenerServiceImpl shortener;
    private TrackingControllerImpl controller;

    @BeforeEach
    void setUp() throws Exception {
        trackingService = mock(TrackingService.class);
        awarenessRenderer = mock(SubmissionAwarenessPageRenderer.class);
        mongoClient = MongoClients.create(mongo.getReplicaSetUrl());
        SimpleMongoClientDatabaseFactory factory =
                new SimpleMongoClientDatabaseFactory(mongoClient, "phishing");
        mongoTemplate = new MongoTemplate(factory);
        mongoTemplate.dropCollection(ShortUrl.class);
        mongoTemplate.indexOps(ShortUrl.class).ensureIndex(
                new Index().on("shortCode", Sort.Direction.ASC).unique().named("short_code_idx"));
        mongoTemplate.indexOps(ShortUrl.class).ensureIndex(
                new Index().on("trackingId", Sort.Direction.ASC).unique().named("tracking_id_idx"));

        ShortUrlRepository repository =
                new MongoRepositoryFactory(mongoTemplate).getRepository(ShortUrlRepository.class);
        shortener = new UrlShortenerServiceImpl(repository);
        Field codeLength = UrlShortenerServiceImpl.class.getDeclaredField("codeLength");
        codeLength.setAccessible(true);
        codeLength.set(shortener, 8);
        Field envPrefix = UrlShortenerServiceImpl.class.getDeclaredField("envPrefix");
        envPrefix.setAccessible(true);
        envPrefix.set(shortener, "01");

        controller = new TrackingControllerImpl(
                trackingService, awarenessRenderer, shortener, new RedirectUrlSanitizer());
    }

    @AfterEach
    void tearDown() {
        if (mongoTemplate != null) {
            mongoTemplate.dropCollection(ShortUrl.class);
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Test
    void shortenAndResolve_persistsUniqueRootPathUrl() {
        String shortUrl = shortener.shorten(ORIGINAL_A, ORIGIN, recipient("rec-a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));

        assertTrue(shortUrl.startsWith(ORIGIN + "/"));
        assertEquals(ORIGIN.length() + 1 + 10, shortUrl.length());
        String code = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);
        assertTrue(code.startsWith("01"));
        assertEquals(10, code.length());
        assertTrue(!shortUrl.contains("/gateway/phishing"));

        assertEquals(ORIGINAL_A, shortener.resolve(code).orElseThrow());

        ShortUrl stored = mongoTemplate.findById(
                mongoTemplate.findAll(ShortUrl.class).get(0).getId(), ShortUrl.class);
        assertNotNull(stored);
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", stored.getTrackingId());
        assertEquals(ORIGINAL_A, stored.getOriginalUrl());
        assertEquals(shortUrl, stored.getShortUrl());
        assertEquals(1, mongoTemplate.findAll(ShortUrl.class).size());
    }

    @Test
    void shorten_sameTrackingId_reusesPersistedRow() {
        CampaignRecipient recipient = recipient("rec-a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        String first = shortener.shorten(ORIGINAL_A, ORIGIN, recipient);
        String second = shortener.shorten(ORIGINAL_A, ORIGIN, recipient);

        assertEquals(first, second);
        assertEquals(1, mongoTemplate.findAll(ShortUrl.class).size());
    }

    @Test
    void shorten_twoRecipients_uniqueCodesAndIndependentResolve() {
        String urlA = shortener.shorten(ORIGINAL_A, ORIGIN, recipient("rec-a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        String urlB = shortener.shorten(ORIGINAL_B, ORIGIN, recipient("rec-b", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));

        assertNotEquals(urlA, urlB);
        assertEquals(2, mongoTemplate.findAll(ShortUrl.class).size());

        String codeA = urlA.substring(urlA.lastIndexOf('/') + 1);
        String codeB = urlB.substring(urlB.lastIndexOf('/') + 1);
        Set<String> codes = new HashSet<>();
        codes.add(codeA);
        codes.add(codeB);
        assertEquals(2, codes.size());
        assertEquals(ORIGINAL_A, shortener.resolve(codeA).orElseThrow());
        assertEquals(ORIGINAL_B, shortener.resolve(codeB).orElseThrow());
    }

    @Test
    void redirectShortUrl_302ToPersistedLandingUrlWithoutRecordingClick() {
        String shortUrl = shortener.shorten(ORIGINAL_A, ORIGIN, recipient("rec-a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        String code = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

        ResponseEntity<String> response = controller.redirectShortUrl(code);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(ORIGINAL_A, response.getHeaders().getFirst(HttpHeaders.LOCATION));
        verify(trackingService, never()).recordClick(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
        verify(trackingService, never()).serveLandingPage(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void redirectShortUrl_unknownCode_404() {
        ResponseEntity<String> response = controller.redirectShortUrl("missing1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().contains("Page Not Found"));
    }

    private static CampaignRecipient recipient(String id, String trackingId) {
        return CampaignRecipient.builder()
                .id(id)
                .campaignId("camp-1")
                .clientId("client-1")
                .trackingId(trackingId)
                .build();
    }
}
