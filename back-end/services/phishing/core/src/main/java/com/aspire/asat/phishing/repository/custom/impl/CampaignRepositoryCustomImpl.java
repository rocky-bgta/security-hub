package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.custom.CampaignRepositoryCustom;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MongoTemplate filters for campaign list/count. Channel is applied in the query
 * (not after pagination) so page contents and {@code total} stay consistent.
 * Missing/null {@code channel} is treated as EMAIL for backward compatibility.
 */
@Repository
@RequiredArgsConstructor
public class CampaignRepositoryCustomImpl implements CampaignRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Campaign> findWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel,
            Pageable pageable) {
        return findWithFilters(clientId, searchParam, status, channel, null, null, null, false, pageable);
    }

    @Override
    public Page<Campaign> findWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType,
            Pageable pageable) {
        return findWithStatuses(
                clientId,
                searchParam,
                status != null ? List.of(status) : null,
                channel,
                campaignType,
                startDate,
                endDate,
                searchNameOrType,
                pageable);
    }

    @Override
    public Page<Campaign> findWithStatuses(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType,
            Pageable pageable) {
        Query query = buildQuery(
                clientId, searchParam, statuses, channel, campaignType, startDate, endDate, searchNameOrType);
        query.with(pageable);
        List<Campaign> content = mongoTemplate.find(query, Campaign.class);
        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Campaign.class));
    }

    @Override
    public long countWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel) {
        return countWithFilters(clientId, searchParam, status, channel, null, null, null, false);
    }

    @Override
    public long countWithFilters(
            String clientId,
            String searchParam,
            CampaignStatus status,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType) {
        return countWithStatuses(
                clientId,
                searchParam,
                status != null ? List.of(status) : null,
                channel,
                campaignType,
                startDate,
                endDate,
                searchNameOrType);
    }

    @Override
    public long countWithStatuses(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType) {
        return mongoTemplate.count(
                buildQuery(clientId, searchParam, statuses, channel, campaignType, startDate, endDate, searchNameOrType),
                Campaign.class);
    }

    private Query buildQuery(
            String clientId,
            String searchParam,
            Collection<CampaignStatus> statuses,
            CampaignChannel channel,
            CampaignType campaignType,
            Instant startDate,
            Instant endDate,
            boolean searchNameOrType) {
        List<Criteria> andCriteria = new ArrayList<>();

        if (StringUtils.hasText(clientId)) {
            andCriteria.add(Criteria.where("clientId").is(clientId.trim()));
        }
        if (StringUtils.hasText(searchParam)) {
            andCriteria.add(searchCriteria(searchParam.trim(), searchNameOrType));
        }
        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                andCriteria.add(Criteria.where("status").is(statuses.iterator().next()));
            } else {
                andCriteria.add(Criteria.where("status").in(statuses));
            }
        }
        if (channel != null) {
            andCriteria.add(DashboardChannelScope.campaignChannelCriteria(channel));
        }
        if (campaignType != null) {
            andCriteria.add(Criteria.where("campaignType").is(campaignType));
        }
        if (startDate != null && endDate != null) {
            andCriteria.add(Criteria.where("createdAt").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            andCriteria.add(Criteria.where("createdAt").gte(startDate));
        } else if (endDate != null) {
            andCriteria.add(Criteria.where("createdAt").lte(endDate));
        }

        Query query = new Query();
        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(Criteria[]::new)));
        }
        return query;
    }

    private static Criteria searchCriteria(String searchParam, boolean searchNameOrType) {
        Pattern pattern = Pattern.compile(Pattern.quote(searchParam), Pattern.CASE_INSENSITIVE);
        Criteria nameCriteria = Criteria.where("campaignName").regex(pattern);
        if (!searchNameOrType) {
            return nameCriteria;
        }

        List<CampaignType> matchingTypes = matchingCampaignTypes(searchParam);
        if (matchingTypes.isEmpty()) {
            return nameCriteria;
        }
        return new Criteria().orOperator(
                nameCriteria,
                Criteria.where("campaignType").in(matchingTypes));
    }

    static List<CampaignType> matchingCampaignTypes(String searchParam) {
        String normalized = searchParam.toLowerCase(Locale.ROOT).replace('_', ' ').trim();
        String underscored = searchParam.toLowerCase(Locale.ROOT).replace(' ', '_').trim();
        return Arrays.stream(CampaignType.values())
                .filter(type -> {
                    String enumName = type.name().toLowerCase(Locale.ROOT);
                    String spaced = enumName.replace('_', ' ');
                    return enumName.contains(underscored)
                            || spaced.contains(normalized)
                            || enumName.contains(normalized.replace(' ', '_'));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findIdsByClientIdAndChannel(String clientId, CampaignChannel channel) {
        Query query = channelScopedQuery(clientId, channel);
        query.fields().include("_id");
        return mongoTemplate.find(query, Campaign.class).stream()
                .map(Campaign::getId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    @Override
    public long countLaunchedBetweenByChannel(
            String clientId, Instant start, Instant end, CampaignChannel channel) {
        List<Criteria> andCriteria = new ArrayList<>();
        addClientAndChannel(andCriteria, clientId, channel);
        andCriteria.add(Criteria.where("launchedAt").gte(start).lt(end));
        return mongoTemplate.count(andQuery(andCriteria), Campaign.class);
    }

    @Override
    public long countByClientIdAndChannel(String clientId, CampaignChannel channel, CampaignStatus status) {
        return countByClientIdAndChannelAndStatuses(
                clientId, channel, status != null ? List.of(status) : null);
    }

    @Override
    public long countByClientIdAndChannelAndStatuses(
            String clientId, CampaignChannel channel, Collection<CampaignStatus> statuses) {
        List<Criteria> andCriteria = new ArrayList<>();
        addClientAndChannel(andCriteria, clientId, channel);
        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                andCriteria.add(Criteria.where("status").is(statuses.iterator().next()));
            } else {
                andCriteria.add(Criteria.where("status").in(statuses));
            }
        }
        return mongoTemplate.count(andQuery(andCriteria), Campaign.class);
    }

    @Override
    public List<String> findTrainingModuleIdsByClientIdAndChannel(String clientId, CampaignChannel channel) {
        Query query = channelScopedQuery(clientId, channel);
        query.addCriteria(Criteria.where("trainingData.trainingModuleId").exists(true).ne(null).ne(""));
        query.fields().include("trainingData.trainingModuleId");
        return mongoTemplate.find(query, Campaign.class).stream()
                .map(campaign -> campaign.getTrainingData() != null
                        ? campaign.getTrainingData().getTrainingModuleId()
                        : null)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
    }

    private Query channelScopedQuery(String clientId, CampaignChannel channel) {
        List<Criteria> andCriteria = new ArrayList<>();
        addClientAndChannel(andCriteria, clientId, channel);
        return andQuery(andCriteria);
    }

    private static void addClientAndChannel(
            List<Criteria> andCriteria, String clientId, CampaignChannel channel) {
        if (StringUtils.hasText(clientId)) {
            andCriteria.add(Criteria.where("clientId").is(clientId.trim()));
        }
        andCriteria.add(DashboardChannelScope.campaignChannelCriteria(channel));
        andCriteria.add(Criteria.where("campaignType").in(DashboardChannelScope.campaignTypes(channel)));
    }

    private static Query andQuery(List<Criteria> andCriteria) {
        Query query = new Query();
        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(Criteria[]::new)));
        }
        return query;
    }
}
