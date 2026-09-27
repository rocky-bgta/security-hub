package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CampaignRepositoryCustomImpl repository;

    private String captureCountQuery(
            String clientId,
            String search,
            CampaignStatus status,
            CampaignChannel channel,
            CampaignType type,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType) {
        when(mongoTemplate.count(any(Query.class), eq(Campaign.class))).thenReturn(1L);
        repository.countWithFilters(
                clientId, search, status, channel, type, startDate, endDate, searchNameOrType);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(Campaign.class));
        return captor.getValue().getQueryObject().toString();
    }

    private String captureCountQuery(
            String clientId, String search, CampaignStatus status, CampaignChannel channel) {
        return captureCountQuery(clientId, search, status, channel, null, null, null, false);
    }

    @Test
    void countWithFilters_noOptionalFilters_scopesByClientOnly() {
        String queryText = captureCountQuery("client-1", null, null, null);

        assertTrue(queryText.contains("client-1"));
        assertFalse(queryText.contains("VOICE"));
        assertFalse(queryText.contains("SMS"));
        assertFalse(queryText.contains("status"));
        assertFalse(queryText.contains("campaignName"));
    }

    @Test
    void countWithFilters_blankSearch_ignored() {
        String queryText = captureCountQuery("client-1", "   ", null, CampaignChannel.VOICE);

        assertTrue(queryText.contains("VOICE"));
        assertFalse(queryText.contains("campaignName"));
    }

    @Test
    void countWithFilters_voiceUsesExactChannelMatch() {
        when(mongoTemplate.count(any(Query.class), eq(Campaign.class))).thenReturn(5L);

        long count = repository.countWithFilters(
                "client-1", null, null, CampaignChannel.VOICE, null, null, null, false);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(Campaign.class));
        String queryText = captor.getValue().getQueryObject().toString();

        assertTrue(queryText.contains("client-1"));
        assertTrue(queryText.contains("VOICE"));
        assertFalse(queryText.contains("$exists"));
        assertEquals(5L, count);
    }

    @Test
    void countWithFilters_smsUsesExactChannelMatch() {
        String queryText = captureCountQuery("client-1", null, null, CampaignChannel.SMS);

        assertTrue(queryText.contains("SMS"));
        assertFalse(queryText.contains("$exists"));
    }

    @Test
    void countWithFilters_emailIncludesLegacyMissingChannel() {
        String queryText = captureCountQuery("client-1", null, null, CampaignChannel.EMAIL);

        assertTrue(queryText.contains("EMAIL"));
        assertTrue(queryText.contains("$exists") || queryText.contains("exists"));
    }

    @Test
    void countLaunchedBetweenByChannel_emailIncludesLegacyChannelAndEmailTypes() {
        when(mongoTemplate.count(any(Query.class), eq(Campaign.class))).thenReturn(4L);
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-01T00:00:00Z");

        long count = repository.countLaunchedBetweenByChannel("client-1", start, end, CampaignChannel.EMAIL);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(Campaign.class));
        String queryText = captor.getValue().getQueryObject().toString();

        assertEquals(4L, count);
        assertTrue(queryText.contains("launchedAt"));
        assertTrue(queryText.contains("EMAIL"));
        assertTrue(queryText.contains("SIMULATED_PHISHING"));
        assertTrue(queryText.contains("PHISHING_WITH_TRAINING"));
        assertFalse(queryText.contains("SMISHING_SIMULATION"));
        assertFalse(queryText.contains("VISHING_SIMULATION"));
    }

    @Test
    void countLaunchedBetweenByChannel_smsUsesSmsTypesOnly() {
        when(mongoTemplate.count(any(Query.class), eq(Campaign.class))).thenReturn(2L);

        repository.countLaunchedBetweenByChannel(
                "client-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                CampaignChannel.SMS);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(Campaign.class));
        String queryText = captor.getValue().getQueryObject().toString();

        assertTrue(queryText.contains("SMS"));
        assertTrue(queryText.contains("SMISHING_SIMULATION"));
        assertTrue(queryText.contains("SMISHING_WITH_TRAINING"));
        assertFalse(queryText.contains("SIMULATED_PHISHING"));
        assertFalse(queryText.contains("VISHING_SIMULATION"));
    }

    @Test
    void countWithFilters_statusOnly() {
        String queryText = captureCountQuery("client-1", null, CampaignStatus.RUNNING, null);

        assertTrue(queryText.contains("RUNNING"));
        assertFalse(queryText.contains("VOICE"));
        assertFalse(queryText.contains("SMS"));
    }

    @Test
    void countWithFilters_searchOnly() {
        String queryText = captureCountQuery("client-1", "Q1 Awareness", null, null);

        assertTrue(queryText.toLowerCase().contains("q1 awareness"));
        assertTrue(queryText.contains("campaignName"));
        assertFalse(queryText.contains("campaignType"));
    }

    @Test
    void countWithFilters_searchNameOrType_includesMatchingTypes() {
        String queryText = captureCountQuery(
                "client-1", "Simulated Phishing", null, null, null, null, null, true);

        assertTrue(queryText.contains("campaignName"));
        assertTrue(queryText.contains("campaignType"));
        assertTrue(queryText.contains("SIMULATED_PHISHING"));
    }

    @Test
    void countWithFilters_typeAndDateRange() {
        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-01-31T23:59:59Z");
        String queryText = captureCountQuery(
                "client-1",
                null,
                CampaignStatus.COMPLETED,
                null,
                CampaignType.PHISHING_WITH_TRAINING,
                start,
                end,
                false);

        assertTrue(queryText.contains("COMPLETED"));
        assertTrue(queryText.contains("PHISHING_WITH_TRAINING"));
        assertTrue(queryText.contains("createdAt"));
    }

    @Test
    void findWithFilters_appliesStatusSearchAndChannelTogether() {
        when(mongoTemplate.find(any(Query.class), eq(Campaign.class))).thenReturn(List.of());

        repository.findWithFilters(
                "client-1",
                "phish",
                CampaignStatus.DRAFT,
                CampaignChannel.SMS,
                null,
                null,
                null,
                false,
                PageRequest.of(0, 12));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Campaign.class));
        Query query = captor.getValue();
        String queryText = query.getQueryObject().toString();

        assertTrue(queryText.contains("client-1"));
        assertTrue(queryText.contains("DRAFT"));
        assertTrue(queryText.contains("SMS"));
        assertTrue(queryText.toLowerCase().contains("phish"));
        assertEquals(12, query.getLimit());
        assertEquals(0, query.getSkip());
    }

    @Test
    void findWithFilters_appliesPaginationAndSort() {
        when(mongoTemplate.find(any(Query.class), eq(Campaign.class))).thenReturn(List.of());

        repository.findWithFilters(
                "client-1",
                null,
                null,
                CampaignChannel.VOICE,
                null,
                null,
                null,
                false,
                PageRequest.of(2, 12, Sort.by(Sort.Direction.DESC, "createdAt")));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Campaign.class));
        Query query = captor.getValue();

        assertEquals(12, query.getLimit());
        assertEquals(24, query.getSkip());
        assertTrue(query.getSortObject().toString().contains("createdAt"));
    }

    @Test
    void findWithStatuses_multiStatuses_appliesInOperator() {
        when(mongoTemplate.find(any(Query.class), eq(Campaign.class))).thenReturn(List.of());

        repository.findWithStatuses(
                "client-1",
                null,
                List.of(CampaignStatus.RUNNING, CampaignStatus.COMPLETED, CampaignStatus.EXPIRED),
                CampaignChannel.EMAIL,
                PageRequest.of(0, 10));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(Campaign.class));
        String queryText = captor.getValue().getQueryObject().toString();

        assertTrue(queryText.contains("client-1"));
        assertTrue(queryText.contains("$in"));
        assertTrue(queryText.contains("RUNNING"));
        assertTrue(queryText.contains("COMPLETED"));
        assertTrue(queryText.contains("EXPIRED"));
        assertFalse(queryText.contains("DRAFT"));
    }

    @Test
    void countByClientIdAndChannelAndStatuses_appliesInOperator() {
        when(mongoTemplate.count(any(Query.class), eq(Campaign.class))).thenReturn(3L);

        long count = repository.countByClientIdAndChannelAndStatuses(
                "client-1",
                CampaignChannel.SMS,
                List.of(CampaignStatus.RUNNING, CampaignStatus.COMPLETED, CampaignStatus.EXPIRED));

        assertEquals(3L, count);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(Campaign.class));
        String queryText = captor.getValue().getQueryObject().toString();

        assertTrue(queryText.contains("client-1"));
        assertTrue(queryText.contains("SMS"));
        assertTrue(queryText.contains("$in"));
        assertTrue(queryText.contains("RUNNING"));
        assertTrue(queryText.contains("COMPLETED"));
        assertTrue(queryText.contains("EXPIRED"));
        assertFalse(queryText.contains("DRAFT"));
    }

    @Test
    void matchingCampaignTypes_normalizesSpacesAndUnderscores() {
        List<CampaignType> matches = CampaignRepositoryCustomImpl.matchingCampaignTypes("Simulated Phishing");
        assertTrue(matches.contains(CampaignType.SIMULATED_PHISHING));

        List<CampaignType> training = CampaignRepositoryCustomImpl.matchingCampaignTypes("with_training");
        assertTrue(training.contains(CampaignType.PHISHING_WITH_TRAINING)
                || training.contains(CampaignType.SMISHING_WITH_TRAINING)
                || training.contains(CampaignType.VISHING_WITH_TRAINING));
    }
}
