package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.RoleType;
import com.aspire.asat.billing.model.Invoice;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Repository
public class InvoiceRepositoryCustomImpl implements InvoiceRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public InvoiceRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Invoice> findInvoicesWithDynamicFilters(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId, Pageable pageable) {
        Query query = buildFilteredQuery(clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);
        query.with(pageable);
        return mongoTemplate.find(query, Invoice.class);
    }

    @Override
    public List<Invoice> findInvoicesWithDynamicFilters(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId, int offset, int limit) {
        Query query = buildFilteredQuery(clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);
        query.skip(offset).limit(limit);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, Invoice.class);
    }

    @Override
    public long countInvoicesWithDynamicFilters(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId) {
        Query query = buildFilteredQuery(clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);
        return mongoTemplate.count(query, Invoice.class);
    }

    @Override
    public Map<InvoiceStatus, Long> countInvoicesByStatus(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId) {
        List<Criteria> criteriaList = buildInvoiceFilterCriteria(
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);

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

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);

        Map<InvoiceStatus, Long> counts = new EnumMap<>(InvoiceStatus.class);
        for (Document doc : results.getMappedResults()) {
            Object statusObj = doc.get("_id");
            if (statusObj == null) {
                continue;
            }
            try {
                InvoiceStatus status = InvoiceStatus.valueOf(statusObj.toString());
                Number count = doc.get("count", Number.class);
                counts.put(status, count != null ? count.longValue() : 0L);
            } catch (IllegalArgumentException ignored) {
                // Skip unknown status values
            }
        }
        return counts;
    }

    private Query buildFilteredQuery(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId) {
        List<Criteria> criteriaList = buildInvoiceFilterCriteria(
                clientId, mspId, countryId, statuses, roleType, startDate, endDate, search, productId);

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return query;
    }

    private List<Criteria> buildInvoiceFilterCriteria(
            String clientId, String mspId, String countryId, List<InvoiceStatus> statuses, RoleType roleType,
            Instant startDate, Instant endDate, String search, String productId) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (clientId != null && !clientId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("clientAdminId").is(clientId));
        }

        if (mspId != null && !mspId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("mspAdminId").is(mspId));
        }

        if (countryId != null && !countryId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("countryId").is(countryId));
        }

        if (statuses != null && !statuses.isEmpty()) {
            if (statuses.size() == 1) {
                criteriaList.add(Criteria.where("status").is(statuses.get(0)));
            } else {
                criteriaList.add(Criteria.where("status").in(statuses));
            }
        }

        if (roleType != null) {
            criteriaList.add(Criteria.where("roleType").is(roleType));
        }

        if (startDate != null) {
            criteriaList.add(Criteria.where("createdAt").gte(startDate));
        }

        if (endDate != null) {
            Instant endInstant = endDate.plusSeconds(86400);
            criteriaList.add(Criteria.where("createdAt").lt(endInstant));
        }

        if (productId != null && !productId.trim().isEmpty()) {
            criteriaList.add(Criteria.where("productSelections.productId").is(productId.trim()));
        }

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = search.trim();
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("clientName").regex(searchPattern, "i"),
                    Criteria.where("mspName").regex(searchPattern, "i"),
                    Criteria.where("id").regex(searchPattern, "i")
            );
            criteriaList.add(searchCriteria);
        }

        return criteriaList;
    }
}
