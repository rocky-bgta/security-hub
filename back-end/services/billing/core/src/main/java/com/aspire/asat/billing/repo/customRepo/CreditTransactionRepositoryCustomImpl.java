package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.TransactionStatus;
import com.aspire.asat.billing.model.CreditTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CreditTransactionRepositoryCustomImpl implements CreditTransactionRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public CreditTransactionRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<CreditTransaction> findTransactionsWithDynamicFilters(String clientId, String invoiceId,
                                                                      TransactionStatus status, String type,
                                                                      Pageable pageable) {
        List<Criteria> criteriaList = buildDynamicCriteria(clientId, invoiceId, status, type);

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        query.with(pageable);
        return mongoTemplate.find(query, CreditTransaction.class);
    }

    @Override
    public long countTransactionsWithDynamicFilters(String clientId, String invoiceId,
                                                    TransactionStatus status, String type) {
        List<Criteria> criteriaList = buildDynamicCriteria(clientId, invoiceId, status, type);

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.count(query, CreditTransaction.class);
    }

    @Override
    public long countFilteredTransactions(String clientId, String invoiceId,
                                          TransactionStatus status, String type) {
        // Same logic reused
        return countTransactionsWithDynamicFilters(clientId, invoiceId, status, type);
    }

    @Override
    public double calculateTotalAmountByType(String clientId, String type) {
        MatchOperation match = Aggregation.match(
                Criteria.where("clientId").is(clientId)
                        .and("type").is(type)
        );

        GroupOperation group = Aggregation.group().sum("amount").as("total");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        AggregationResults<TotalAmountResult> results = mongoTemplate.aggregate(
                aggregation, "credit_transactions", TotalAmountResult.class);

        TotalAmountResult result = results.getUniqueMappedResult();
        return result != null ? result.getTotal() : 0.0;
    }

    @Override
    public double calculatePaidCreditByType(String clientId, String type) {
        MatchOperation match = Aggregation.match(
                Criteria.where("clientId").is(clientId)
                        .and("type").is(type)
                        .and("status").is("PAID")
        );

        GroupOperation group = Aggregation.group().sum("amount").as("total");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        AggregationResults<TotalAmountResult> results = mongoTemplate.aggregate(
                aggregation, "credit_transactions", TotalAmountResult.class);

        TotalAmountResult result = results.getUniqueMappedResult();
        return result != null ? result.getTotal() : 0.0;
    }

    @Override
    public double calculateDueCreditByType(String clientId, String type) {
        MatchOperation match = Aggregation.match(
                Criteria.where("clientId").is(clientId)
                        .and("type").is(type)
                        .and("status").ne("PAID")  // All other statuses considered due
        );

        GroupOperation group = Aggregation.group().sum("amount").as("total");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        AggregationResults<TotalAmountResult> results = mongoTemplate.aggregate(
                aggregation, "credit_transactions", TotalAmountResult.class
        );

        TotalAmountResult result = results.getUniqueMappedResult();
        return result != null ? result.getTotal() : 0.0;
    }

    /**
     * Reusable method to build dynamic filtering criteria based on parameters
     */
    private List<Criteria> buildDynamicCriteria(String clientId, String invoiceId,
                                                TransactionStatus status, String type) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (clientId != null && !clientId.isBlank()) {
            criteriaList.add(Criteria.where("clientId").is(clientId));
        }

        if (invoiceId != null && !invoiceId.isBlank()) {
            criteriaList.add(Criteria.where("referenceId").is(invoiceId));
        }

        if (status != null) {
            switch (status) {
                case PAID -> criteriaList.add(Criteria.where("status").is("PAID"));
                case PENDING -> criteriaList.add(Criteria.where("status").is("PENDING"));
                default -> criteriaList.add(Criteria.where("status").ne("PAID")); // Anything not PAID is considered UNPAID
            }
        }

        if (type != null && !type.isBlank()) {
            criteriaList.add(Criteria.where("type").is(type));
        }

        return criteriaList;
    }

    /**
     * Helper class for aggregation results
     */
    private static class TotalAmountResult {
        private double total;

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }
    }
}
