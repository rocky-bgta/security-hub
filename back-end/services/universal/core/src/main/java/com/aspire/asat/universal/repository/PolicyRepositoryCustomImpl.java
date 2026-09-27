package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.Policy;
import com.aspire.asat.universal.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
public class PolicyRepositoryCustomImpl implements PolicyRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public PolicyRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<Policy> findAllWithFilters(String policyName, String countryId, String industryId, Pageable pageable) {
        Query query = new Query();
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        if (countryId != null && !countryId.isBlank()) {
            query.addCriteria(Criteria.where("countryId").is(countryId.trim()));
        }
        if (industryId != null && !industryId.isBlank()) {
            query.addCriteria(Criteria.where("industryId").is(industryId.trim()));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findByPolicyNameContainingIgnoreCase(String policyName, Pageable pageable) {
        Query query = new Query();
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findByStatusAndPolicyNameContainingIgnoreCase(PolicyStatus status, String policyName, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(status));
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findByClientAdminId(String clientAdminId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("clientAdminId").is(clientAdminId));
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findByClientAdminIdAndPolicyNameContainingIgnoreCase(String clientAdminId, String policyName, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("clientAdminId").is(clientAdminId));
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findByClientAdminIdWithFilters(String clientAdminId, String policyName, String countryId, String industryId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("clientAdminId").is(clientAdminId));
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        if (countryId != null && !countryId.isBlank()) {
            query.addCriteria(Criteria.where("countryId").is(countryId.trim()));
        }
        if (industryId != null && !industryId.isBlank()) {
            query.addCriteria(Criteria.where("industryId").is(industryId.trim()));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findByMspIdWithFilters(String mspId, String policyName, String countryId, String industryId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("mspId").is(mspId));
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        if (countryId != null && !countryId.isBlank()) {
            query.addCriteria(Criteria.where("countryId").is(countryId.trim()));
        }
        if (industryId != null && !industryId.isBlank()) {
            query.addCriteria(Criteria.where("industryId").is(industryId.trim()));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findActivePoliciesForClientUserWithFilters(String clientAdminId, String policyName, String countryId, String industryId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is(PolicyStatus.ACTIVE));
        query.addCriteria(new Criteria().orOperator(
                Criteria.where("clientAdminId").is(clientAdminId),
                Criteria.where("isDefault").is(true)
        ));
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        if (countryId != null && !countryId.isBlank()) {
            query.addCriteria(Criteria.where("countryId").is(countryId.trim()));
        }
        if (industryId != null && !industryId.isBlank()) {
            query.addCriteria(Criteria.where("industryId").is(industryId.trim()));
        }
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<Policy> findPoliciesForClientUserWithFilters(
            String clientAdminId,
            String policyName,
            String countryId,
            String industryId,
            Pageable pageable) {
        Query query = buildScopedPolicyQuery(
                defaultPlatformCriteria(),
                clientAdminIdCriteria(clientAdminId));
        applyOptionalFilters(query, policyName, countryId, industryId);
        return executePagedQuery(query, pageable);
    }

    @Override
    public Page<Policy> findPoliciesForClientAdminWithFilters(
            String clientAdminId,
            String mspId,
            String policyName,
            String countryId,
            String industryId,
            Pageable pageable) {
        Query query = buildScopedPolicyQuery(
                defaultPlatformCriteria(),
                mspIdCriteria(mspId),
                clientAdminIdCriteria(clientAdminId));
        applyOptionalFilters(query, policyName, countryId, industryId);
        return executePagedQuery(query, pageable);
    }

    @Override
    public Page<Policy> findPoliciesForMspAdminWithFilters(
            String mspUserId,
            List<String> clientAdminIds,
            String policyName,
            String countryId,
            String industryId,
            Pageable pageable) {
        List<Criteria> scopeCriteria = new ArrayList<>();
        scopeCriteria.add(defaultPlatformCriteria());
        if (mspUserId != null && !mspUserId.isBlank()) {
            scopeCriteria.add(Criteria.where("mspId").is(mspUserId.trim()));
        }
        if (clientAdminIds != null && !clientAdminIds.isEmpty()) {
            scopeCriteria.add(Criteria.where("clientAdminId").in(clientAdminIds));
        }

        Query query = buildScopedPolicyQuery(scopeCriteria.toArray(new Criteria[0]));
        applyOptionalFilters(query, policyName, countryId, industryId);
        return executePagedQuery(query, pageable);
    }

    private Criteria defaultPlatformCriteria() {
        return Criteria.where("isDefault").is(true);
    }

    private Criteria mspIdCriteria(String mspId) {
        if (mspId == null || mspId.isBlank()) {
            return null;
        }
        return Criteria.where("mspId").is(mspId.trim());
    }

    private Criteria clientAdminIdCriteria(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return null;
        }
        return Criteria.where("clientAdminId").is(clientAdminId.trim());
    }

    private Query buildScopedPolicyQuery(Criteria... scopeCriteria) {
        List<Criteria> criteria = new ArrayList<>();
        for (Criteria criterion : scopeCriteria) {
            if (criterion != null) {
                criteria.add(criterion);
            }
        }
        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(criteria.toArray(new Criteria[0])));
        }
        return query;
    }

    private void applyOptionalFilters(Query query, String policyName, String countryId, String industryId) {
        if (policyName != null && !policyName.isBlank()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(policyName.trim()) + ".*", Pattern.CASE_INSENSITIVE);
            query.addCriteria(Criteria.where("policyName").regex(pattern));
        }
        if (countryId != null && !countryId.isBlank()) {
            query.addCriteria(Criteria.where("countryId").is(countryId.trim()));
        }
        if (industryId != null && !industryId.isBlank()) {
            query.addCriteria(Criteria.where("industryId").is(industryId.trim()));
        }
    }

    private Page<Policy> executePagedQuery(Query query, Pageable pageable) {
        long total = mongoTemplate.count(query, Policy.class);
        query.with(pageable);
        List<Policy> results = mongoTemplate.find(query, Policy.class);
        return new PageImpl<>(results, pageable, total);
    }
}
