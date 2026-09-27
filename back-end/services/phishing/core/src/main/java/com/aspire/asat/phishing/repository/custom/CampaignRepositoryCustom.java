package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Filtered campaign queries that combine client, search, status, channel, type, and dates.
 */
public interface CampaignRepositoryCustom {

    /**
     * Campaign-list filters (name-only search).
     */
    Page<Campaign> findWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel,
            Pageable pageable);

    default Page<Campaign> findWithStatuses(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel,
            Pageable pageable) {
        return findWithStatuses(clientId, searchParam, statuses, channel, null, null, null, false, pageable);
    }

    Page<Campaign> findWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType,
            Pageable pageable);

    Page<Campaign> findWithStatuses(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType,
            Pageable pageable);

    /**
     * Campaign-list count (name-only search).
     */
    long countWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel);

    default long countWithStatuses(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel) {
        return countWithStatuses(clientId, searchParam, statuses, channel, null, null, null, false);
    }

    long countWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType);

    long countWithStatuses(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType);

    /**
     * Campaign IDs for a client and delivery channel (EMAIL includes missing/null channel).
     */
    List<String> findIdsByClientIdAndChannel(String clientId, CampaignChannel channel);

    /**
     * Count campaigns launched in {@code [start, end)} for a channel.
     */
    long countLaunchedBetweenByChannel(String clientId, Instant start, Instant end, CampaignChannel channel);

    /**
     * Count campaigns for a client and channel, optionally restricted to a status.
     */
    long countByClientIdAndChannel(String clientId, CampaignChannel channel, CampaignStatus status);

    /**
     * Count campaigns for a client and channel, optionally restricted to a collection of statuses.
     */
    long countByClientIdAndChannelAndStatuses(String clientId, CampaignChannel channel, Collection<CampaignStatus> statuses);

    /**
     * Training-module IDs on campaigns of this client and channel.
     */
    List<String> findTrainingModuleIdsByClientIdAndChannel(String clientId, CampaignChannel channel);
}
