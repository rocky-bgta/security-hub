package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Invoice;
import com.aspire.asat.billing.model.Payment;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public PaymentRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Payment> findPaymentsWithDynamicFilters(List<String> statuses, String method, Instant startDate,
            Instant endDate, String clientId, String mspId, String countryId, RoleType roleType, String search,
            Pageable pageable) {
        Query query = buildFilteredQuery(statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);
        query.with(pageable);
        return mongoTemplate.find(query, Payment.class);
    }

    @Override
    public List<Payment> findPaymentsWithDynamicFilters(List<String> statuses, String method, Instant startDate,
            Instant endDate, String clientId, String mspId, String countryId, RoleType roleType, String search,
            int offset, int limit) {
        Query query = buildFilteredQuery(statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);
        query.skip(offset).limit(limit);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, Payment.class);
    }

    @Override
    public long countPaymentsWithDynamicFilters(List<String> statuses, String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search) {
        Query query = buildFilteredQuery(statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);
        return mongoTemplate.count(query, Payment.class);
    }

    @Override
    public Map<String, PaymentStatusAggregate> aggregatePaymentsByStatus(String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search, List<String> statuses) {
        List<Criteria> criteriaList = buildPaymentFilterCriteria(
                statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);

        Aggregation aggregation;
        if (criteriaList.isEmpty()) {
            aggregation = Aggregation.newAggregation(
                    Aggregation.group("status")
                            .count().as("count")
                            .sum("amount").as("amountSum")
            );
        } else {
            aggregation = Aggregation.newAggregation(
                    Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))),
                    Aggregation.group("status")
                            .count().as("count")
                            .sum("amount").as("amountSum")
            );
        }

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Payment.class, Document.class);

        Map<String, PaymentStatusAggregate> aggregates = new HashMap<>();
        for (Document doc : results.getMappedResults()) {
            Object statusObj = doc.get("_id");
            if (statusObj == null) {
                continue;
            }
            Number count = doc.get("count", Number.class);
            Number amountSum = doc.get("amountSum", Number.class);
            aggregates.put(statusObj.toString(), PaymentStatusAggregate.builder()
                    .count(count != null ? count.longValue() : 0L)
                    .amountSum(amountSum != null ? amountSum.doubleValue() : 0.0)
                    .build());
        }
        return aggregates;
    }

    @Override
    public PaymentSummaryResult getPaymentSummary(Instant startDate, Instant endDate, String clientId, String mspId,
            String countryId, RoleType roleType) {
        List<Criteria> paymentCriteria = buildPaymentFilterCriteria(
                null, null, startDate, endDate, clientId, mspId, countryId, roleType, null);

        Query paymentQuery = new Query();
        if (!paymentCriteria.isEmpty()) {
            paymentQuery.addCriteria(new Criteria().andOperator(paymentCriteria.toArray(new Criteria[0])));
        }

        List<Payment> payments = mongoTemplate.find(paymentQuery, Payment.class);

        double totalPayments = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()) && p.getAmount() != null)
                .mapToDouble(Payment::getAmount)
                .sum();

        List<Criteria> invoiceCriteria = new ArrayList<>();
        if (clientId != null && !clientId.trim().isEmpty()) {
            invoiceCriteria.add(Criteria.where("clientAdminId").is(clientId));
        }
        if (mspId != null && !mspId.trim().isEmpty()) {
            invoiceCriteria.add(Criteria.where("mspAdminId").is(mspId));
        }
        if (countryId != null && !countryId.trim().isEmpty()) {
            invoiceCriteria.add(Criteria.where("countryId").is(countryId));
        }
        if (roleType != null) {
            invoiceCriteria.add(Criteria.where("roleType").is(roleType));
        }
        if (startDate != null) {
            invoiceCriteria.add(Criteria.where("createdAt").gte(startDate));
        }
        if (endDate != null) {
            invoiceCriteria.add(Criteria.where("createdAt").lte(endDate));
        }

        Query invoiceStatsQuery = new Query();
        if (!invoiceCriteria.isEmpty()) {
            invoiceStatsQuery.addCriteria(new Criteria().andOperator(invoiceCriteria.toArray(new Criteria[0])));
        }

        List<Invoice> invoices = mongoTemplate.find(invoiceStatsQuery, Invoice.class);
        long totalInvoices = invoices.size();
        long paidInvoices = invoices.stream()
                .filter(inv -> inv.getStatus() != null && "PAID".equals(inv.getStatus().name()))
                .count();
        long overdueInvoices = invoices.stream()
                .filter(inv -> inv.getStatus() != null && "OVERDUE".equals(inv.getStatus().name()))
                .count();

        Map<String, Double> invoicePaymentsMap = payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus())
                        && p.getAmount() != null
                        && p.getInvoiceId() != null)
                .collect(Collectors.groupingBy(
                        Payment::getInvoiceId,
                        Collectors.summingDouble(Payment::getAmount)
                ));

        double outstandingAmount = invoices.stream()
                .filter(inv -> inv.getStatus() != null
                        && inv.getTotalAmount() > 0
                        && !"PAID".equals(inv.getStatus().name())
                        && !"CANCELLED".equals(inv.getStatus().name()))
                .mapToDouble(inv -> {
                    double totalPaid = invoicePaymentsMap.getOrDefault(inv.getId(), 0.0);
                    if ("PARTIAL".equals(inv.getStatus().name())) {
                        return Math.max(0, inv.getTotalAmount() - totalPaid);
                    }
                    return inv.getTotalAmount();
                })
                .sum();

        return PaymentSummaryResult.builder()
                .totalPayments(totalPayments)
                .outstandingAmount(outstandingAmount)
                .paidInvoicesCount(paidInvoices)
                .totalInvoicesCount(totalInvoices)
                .overdueInvoicesCount(overdueInvoices)
                .build();
    }

    private Query buildFilteredQuery(List<String> statuses, String method, Instant startDate, Instant endDate,
            String clientId, String mspId, String countryId, RoleType roleType, String search) {
        List<Criteria> criteriaList = buildPaymentFilterCriteria(
                statuses, method, startDate, endDate, clientId, mspId, countryId, roleType, search);
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private List<Criteria> buildPaymentFilterCriteria(List<String> statuses, String method, Instant startDate,
            Instant endDate, String clientId, String mspId, String countryId, RoleType roleType, String search) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                criteriaList.add(Criteria.where("status").is(statuses.get(0)));
            } else {
                criteriaList.add(Criteria.where("status").in(statuses));
            }
        }

        if (method != null && !method.trim().isEmpty()) {
            criteriaList.add(Criteria.where("paymentSources.method").is(method.trim()));
        }

        if (clientId != null && !clientId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("clientId").is(clientId.trim()));
        }

        if (mspId != null && !mspId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId.trim()));
        }

        if (countryId != null && !countryId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("countryId").is(countryId.trim()));
        }

        if (roleType != null) {
            criteriaList.add(Criteria.where("roleType").is(roleType));
        }

        if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        }

        if (endDate != null) {
            criteriaList.add(Criteria.where("createdAt").lte(endDate));
        }

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = search.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("id").regex(searchPattern, "i"),
                    Criteria.where("invoiceId").regex(searchPattern, "i"),
                    Criteria.where("transactionId").regex(searchPattern, "i")
            ));
        }

        return criteriaList;
    }
}
