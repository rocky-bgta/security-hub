package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.DashboardStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.repository.custom.DailyEmailMetricsAggregation;
import com.aspire.asat.phishing.repository.custom.DashboardStatsRepositoryCustom;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Platform-wide daily email metrics from {@code dashboard_stats}.
 * Channel series live on the same unique {@code (clientId, period, date)} document.
 */
@Repository
@RequiredArgsConstructor
public class DashboardStatsRepositoryCustomImpl implements DashboardStatsRepositoryCustom {

    private static final String COLLECTION = "dashboard_stats";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<DailyEmailMetricsAggregation> aggregateDailyEmailMetricsForAllNonBlankClients(LocalDate startDate) {
        return aggregateDailyEmailMetricsForAllNonBlankClients(startDate, CampaignChannel.EMAIL);
    }

    @Override
    public List<DailyEmailMetricsAggregation> aggregateDailyEmailMetricsForAllNonBlankClients(
            LocalDate startDate, CampaignChannel channel) {
        String prefix = metricsFieldPrefix(channel);
        Criteria match = new Criteria().andOperator(
                Criteria.where("period").is(StatsPeriod.DAILY.name()),
                Criteria.where("date").gte(startDate),
                Criteria.where("clientId").exists(true).ne(null).ne("")
        );

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(match),
                Aggregation.group("date")
                        .sum(prefix + ".totalRecipients").as("totalRecipients")
                        .sum(prefix + ".totalEmailsSent").as("totalEmailsSent")
                        .sum(prefix + ".emailsDelivered").as("emailsDelivered")
                        .sum(prefix + ".emailsBounced").as("emailsBounced")
                        .sum(prefix + ".emailsOpened").as("emailsOpened")
                        .sum(prefix + ".linksClicked").as("linksClicked")
                        .sum(prefix + ".attachmentsOpened").as("attachmentsOpened")
                        .sum(prefix + ".dataSubmitted").as("dataSubmitted")
                        .sum(prefix + ".emailsReported").as("emailsReported"),
                Aggregation.sort(Sort.Direction.ASC, "_id")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, COLLECTION, Document.class);

        List<DailyEmailMetricsAggregation> out = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            LocalDate date = toLocalDate(doc.get("_id"));
            if (date == null) {
                continue;
            }
            EmailMetrics em = new EmailMetrics();
            em.setTotalRecipients(intOrZero(doc, "totalRecipients"));
            em.setTotalEmailsSent(intOrZero(doc, "totalEmailsSent"));
            em.setEmailsDelivered(intOrZero(doc, "emailsDelivered"));
            em.setEmailsBounced(intOrZero(doc, "emailsBounced"));
            em.setEmailsOpened(intOrZero(doc, "emailsOpened"));
            em.setLinksClicked(intOrZero(doc, "linksClicked"));
            em.setAttachmentsOpened(intOrZero(doc, "attachmentsOpened"));
            em.setDataSubmitted(intOrZero(doc, "dataSubmitted"));
            em.setEmailsReported(intOrZero(doc, "emailsReported"));
            em.calculateRates();
            out.add(new DailyEmailMetricsAggregation(date, em));
        }
        return out;
    }

    @Override
    public List<DashboardStats> findDailyStatsForLastNDays(
            String clientId, LocalDate startDate, CampaignChannel channel) {
        Query query = new Query(new Criteria().andOperator(
                Criteria.where("clientId").is(clientId),
                Criteria.where("period").is(StatsPeriod.DAILY),
                Criteria.where("date").gte(startDate)
        ));
        query.with(Sort.by(Sort.Direction.ASC, "date"));
        return mongoTemplate.find(query, DashboardStats.class);
    }

    static String metricsFieldPrefix(CampaignChannel channel) {
        return switch (DashboardChannelScope.effective(channel)) {
            case SMS -> "smsMetrics";
            case VOICE -> "voiceMetrics";
            default -> "emailMetrics";
        };
    }

    private static int intOrZero(Document doc, String key) {
        Object v = doc.get(key);
        if (v == null) {
            return 0;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    private static LocalDate toLocalDate(Object id) {
        if (id == null) {
            return null;
        }
        if (id instanceof LocalDate ld) {
            return ld;
        }
        if (id instanceof Date d) {
            return Instant.ofEpochMilli(d.getTime()).atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (id instanceof Instant ins) {
            return ins.atZone(ZoneOffset.UTC).toLocalDate();
        }
        return null;
    }
}
