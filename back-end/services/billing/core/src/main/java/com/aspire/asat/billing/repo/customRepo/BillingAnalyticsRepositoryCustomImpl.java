package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.analytics.*;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.PaymentFailureReason;
import com.aspire.asat.billing.dto.refund.RefundStatus;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import com.aspire.asat.billing.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BillingAnalyticsRepositoryCustomImpl implements BillingAnalyticsRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Double getTotalRevenue(Instant startDate, Instant endDate, String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is(InvoiceStatus.PAID));

        if (startDate != null) {
            criteriaList.add(Criteria.where("paidAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("paidAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                Aggregation.group().sum("totalAmount").as("total")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();
        return result != null ? result.getDouble("total") : 0.0;
    }

    @Override
    public Double getTotalRefunds(Instant startDate, Instant endDate, String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is(RefundStatus.PROCESSED));

        if (startDate != null) {
            criteriaList.add(Criteria.where("refundedAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("refundedAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                Aggregation.group().sum("refundAmount").as("total")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Refund.class, Document.class);
        Document result = results.getUniqueMappedResult();
        return result != null ? result.getDouble("total") : 0.0;
    }

    @Override
    public List<RevenueByPeriodDTO> getRevenueByPeriod(AnalyticsPeriod period, Instant startDate, Instant endDate, String mspId, String countryId) {
        // Get invoices data
        List<Criteria> invoiceCriteria = new ArrayList<>();
        invoiceCriteria.add(Criteria.where("status").is(InvoiceStatus.PAID));
        if (startDate != null) {
            invoiceCriteria.add(Criteria.where("paidAt").gte(startDate));
        }
        if (endDate != null) {
            invoiceCriteria.add(Criteria.where("paidAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            invoiceCriteria.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            invoiceCriteria.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation invoiceAggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(invoiceCriteria.toArray(new Criteria[0]))),
                Aggregation.project("totalAmount", "paidAt")
                        .and(DateOperators.dateOf("paidAt").year()).as("year")
                        .and(DateOperators.dateOf("paidAt").month()).as("month")
                        .and(getQuarterExpression()).as("quarter"),
                Aggregation.group(Fields.fields().and("year").and(period == AnalyticsPeriod.YEARLY ? "year" : period == AnalyticsPeriod.QUARTERLY ? "quarter" : "month"))
                        .sum("totalAmount").as("revenue")
                        .count().as("newSubscriptions")
                        .first("year").as("year")
                        .first("month").as("month")
                        .first("quarter").as("quarter"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.ASC, "year", period == AnalyticsPeriod.QUARTERLY ? "quarter" : "month")
        );

        AggregationResults<Document> invoiceResults = mongoTemplate.aggregate(invoiceAggregation, Invoice.class, Document.class);

        // Get refunds data
        List<Criteria> refundCriteria = new ArrayList<>();
        refundCriteria.add(Criteria.where("status").is(RefundStatus.PROCESSED));
        if (startDate != null) {
            refundCriteria.add(Criteria.where("refundedAt").gte(startDate));
        }
        if (endDate != null) {
            refundCriteria.add(Criteria.where("refundedAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            refundCriteria.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            refundCriteria.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation refundAggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(refundCriteria.toArray(new Criteria[0]))),
                Aggregation.project("refundAmount", "refundedAt")
                        .and(DateOperators.dateOf("refundedAt").year()).as("year")
                        .and(DateOperators.dateOf("refundedAt").month()).as("month")
                        .and(getQuarterExpressionForRefund()).as("quarter"),
                Aggregation.group(Fields.fields().and("year").and(period == AnalyticsPeriod.YEARLY ? "year" : period == AnalyticsPeriod.QUARTERLY ? "quarter" : "month"))
                        .sum("refundAmount").as("refunds")
                        .first("year").as("year")
                        .first("month").as("month")
                        .first("quarter").as("quarter")
        );

        AggregationResults<Document> refundResults = mongoTemplate.aggregate(refundAggregation, Refund.class, Document.class);

        // Merge results
        Map<String, Double> refundMap = new HashMap<>();
        for (Document doc : refundResults.getMappedResults()) {
            String key = buildPeriodKey(doc, period);
            Double refund = doc.getDouble("refunds");
            refundMap.put(key, refund != null ? refund : 0.0);
        }

        List<RevenueByPeriodDTO> result = new ArrayList<>();
        for (Document doc : invoiceResults.getMappedResults()) {
            String periodLabel = buildPeriodLabel(doc, period);
            String key = buildPeriodKey(doc, period);
            Double revenue = doc.getDouble("revenue");
            if (revenue == null) revenue = 0.0;
            
            Object subsObj = doc.get("newSubscriptions");
            Long newSubscriptions = subsObj instanceof Number ? ((Number) subsObj).longValue() : 0L;
            
            Double refunds = refundMap.getOrDefault(key, 0.0);
            Double netRevenue = revenue - refunds;

            result.add(RevenueByPeriodDTO.builder()
                    .period(periodLabel)
                    .revenue(revenue)
                    .newSubscriptions(newSubscriptions)
                    .refunds(refunds)
                    .netRevenue(netRevenue)
                    .build());
        }

        return result;
    }

    private AggregationExpression getQuarterExpression() {
        return context -> new Document("$ceil",
                new Document("$divide", Arrays.asList(
                        new Document("$month", "$paidAt"), 3
                )));
    }

    private AggregationExpression getQuarterExpressionForRefund() {
        return context -> new Document("$ceil",
                new Document("$divide", Arrays.asList(
                        new Document("$month", "$refundedAt"), 3
                )));
    }

    private String buildPeriodKey(Document doc, AnalyticsPeriod period) {
        Integer year = doc.getInteger("year");
        if (period == AnalyticsPeriod.YEARLY) {
            return String.valueOf(year);
        } else if (period == AnalyticsPeriod.QUARTERLY) {
            Object quarterObj = doc.get("quarter");
            int quarter = quarterObj instanceof Number ? ((Number) quarterObj).intValue() : 1;
            return year + "-Q" + quarter;
        } else {
            Integer month = doc.getInteger("month");
            return year + "-" + month;
        }
    }

    private String buildPeriodLabel(Document doc, AnalyticsPeriod period) {
        Integer year = doc.getInteger("year");
        if (period == AnalyticsPeriod.YEARLY) {
            return String.valueOf(year);
        } else if (period == AnalyticsPeriod.QUARTERLY) {
            Object quarterObj = doc.get("quarter");
            int quarter = quarterObj instanceof Number ? ((Number) quarterObj).intValue() : 1;
            return "Q" + quarter + " " + year;
        } else {
            Integer month = doc.getInteger("month");
            String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            return months[month] + " " + year;
        }
    }

    @Override
    public Long getNewSubscriptionsCount(Instant startDate, Instant endDate, String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        
        if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation;
        if (criteriaList.isEmpty()) {
            aggregation = Aggregation.newAggregation(
                    Aggregation.group().count().as("count")
            );
        } else {
            aggregation = Aggregation.newAggregation(
                    Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                    Aggregation.group().count().as("count")
            );
        }

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();
        if (result == null) return 0L;
        Object countObj = result.get("count");
        return countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
    }

    @Override
    public Long getActiveLicensesCount(String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is(InvoiceStatus.PAID));

        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                Aggregation.unwind("productSelections"),
                Aggregation.group().sum("productSelections.licenseCount").as("totalLicenses")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();
        if (result == null) return 0L;
        Object licenseObj = result.get("totalLicenses");
        return licenseObj instanceof Number ? ((Number) licenseObj).longValue() : 0L;
    }

    @Override
    public List<TopPackageDTO> getTopPackages(Instant startDate, Instant endDate, String mspId, String countryId, int limit) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is(InvoiceStatus.PAID));

        if (startDate != null) {
            criteriaList.add(Criteria.where("paidAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("paidAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                Aggregation.unwind("productSelections"),
                Aggregation.group("productSelections.packageName")
                        .count().as("subscriptionCount")
                        .sum(ArithmeticOperators.Multiply.valueOf("productSelections.licenseCount")
                                .multiplyBy("productSelections.pricePerLicense")
                                .multiplyBy("productSelections.validityPeriod"))
                        .as("revenue"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "revenue"),
                Aggregation.limit(limit)
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);

        // Calculate total revenue for percentage
        double totalRevenue = results.getMappedResults().stream()
                .mapToDouble(doc -> {
                    Object rev = doc.get("revenue");
                    return rev instanceof Number ? ((Number) rev).doubleValue() : 0.0;
                })
                .sum();

        return results.getMappedResults().stream()
                .map(doc -> {
                    String packageName = doc.getString("_id");
                    Object subsObj = doc.get("subscriptionCount");
                    Long subscriptionCount = subsObj instanceof Number ? ((Number) subsObj).longValue() : 0L;
                    Object revObj = doc.get("revenue");
                    Double revenue = revObj instanceof Number ? ((Number) revObj).doubleValue() : 0.0;
                    Double revenuePercentage = totalRevenue > 0 ? (revenue / totalRevenue) * 100 : 0.0;

                    return TopPackageDTO.builder()
                            .packageName(packageName != null ? packageName : "Unknown")
                            .subscriptionCount(subscriptionCount)
                            .revenue(revenue)
                            .revenuePercentage(Math.round(revenuePercentage * 10.0) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FailedPaymentAnalysisDTO> getFailedPaymentsByReason(Instant startDate, Instant endDate, String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is("FAILED"));

        if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                Aggregation.group("failureReason")
                        .count().as("count"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "count")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Payment.class, Document.class);

        // Calculate total for percentage
        long totalFailed = results.getMappedResults().stream()
                .mapToLong(doc -> {
                    Object countObj = doc.get("count");
                    return countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
                })
                .sum();

        return results.getMappedResults().stream()
                .map(doc -> {
                    String reasonStr = doc.getString("_id");
                    PaymentFailureReason reason = reasonStr != null ? 
                            PaymentFailureReason.valueOf(reasonStr) : PaymentFailureReason.OTHER;
                    Object countObj = doc.get("count");
                    Long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
                    Double percentage = totalFailed > 0 ? (count.doubleValue() / totalFailed) * 100 : 0.0;

                    return FailedPaymentAnalysisDTO.builder()
                            .reason(reason)
                            .reasonLabel(reason.getDisplayName())
                            .count(count)
                            .percentage(Math.round(percentage * 10.0) / 10.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public PaymentSuccessRateDTO getPaymentSuccessRate(Instant startDate, Instant endDate, String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation;
        if (criteriaList.isEmpty()) {
            aggregation = Aggregation.newAggregation(
                    Aggregation.group("status").count().as("count")
            );
        } else {
            aggregation = Aggregation.newAggregation(
                    Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                    Aggregation.group("status").count().as("count")
            );
        }

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Payment.class, Document.class);

        long successCount = 0;
        long failedCount = 0;
        long totalCount = 0;

        for (Document doc : results.getMappedResults()) {
            String status = doc.getString("_id");
            Object countObj = doc.get("count");
            long count = countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
            totalCount += count;

            if ("SUCCESS".equals(status)) {
                successCount = count;
            } else if ("FAILED".equals(status)) {
                failedCount = count;
            }
        }

        double successRate = totalCount > 0 ? (successCount * 100.0) / totalCount : 0.0;

        return PaymentSuccessRateDTO.builder()
                .paymentSuccessRate(Math.round(successRate * 10.0) / 10.0)
                .totalPayments(totalCount)
                .successfulPayments(successCount)
                .failedPayments(failedCount)
                .build();
    }

    @Override
    public Long getFailedPaymentsCount(Instant startDate, Instant endDate, String mspId, String countryId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is("FAILED"));

        if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(endDate));
        }
        if (mspId != null && !mspId.isBlank()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.isBlank()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                Aggregation.group().count().as("count")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Payment.class, Document.class);
        Document result = results.getUniqueMappedResult();
        if (result == null) return 0L;
        Object countObj = result.get("count");
        return countObj instanceof Number ? ((Number) countObj).longValue() : 0L;
    }
}

