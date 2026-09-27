package com.aspire.asat.universal.repository.leaderboard;

import com.aspire.asat.universal.entity.leaderboard.Leaderboard;
import com.aspire.asat.universal.leaderboard.LeaderboardStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Repository
public class LeaderboardRepositoryImpl implements LeaderboardRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardRepositoryImpl.class);
    private final MongoTemplate mongoTemplate;

    public LeaderboardRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Leaderboard> findAllWithSearch(String search) {
        log.debug("Finding all leaderboards with search: {}", search);

        Query query = new Query();
        addSearchCriteria(query, search);

        // Apply sorting by createdDate DESC
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        return mongoTemplate.find(query, Leaderboard.class);
    }

    @Override
    public Page<Leaderboard> findAllWithSearchPaginated(String search, Pageable pageable) {
        log.debug("Finding all leaderboards with search: {} and pagination", search);

        Query query = new Query();
        addSearchCriteria(query, search);

        // Get total count
        long total = mongoTemplate.count(query, Leaderboard.class);

        // Apply sorting and pagination
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));
        query.with(pageable);

        List<Leaderboard> leaderboards = mongoTemplate.find(query, Leaderboard.class);

        return new PageImpl<>(leaderboards, pageable, total);
    }

    @Override
    public List<Leaderboard> findByClientIdWithSearch(UUID clientId, String search) {
        log.debug("Finding leaderboards by clientId: {} with search: {}", clientId, search);

        Query query = new Query();
        query.addCriteria(Criteria.where("clientId").is(clientId));
        addSearchCriteria(query, search);

        // Apply sorting by createdDate DESC
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        return mongoTemplate.find(query, Leaderboard.class);
    }

    @Override
    public List<Leaderboard> findDefaultWithSearch(String search) {
        log.debug("Finding default leaderboards with search: {}", search);

        Query query = new Query();
        query.addCriteria(Criteria.where("isDefault").is(true));
        addSearchCriteria(query, search);

        // Apply sorting by createdDate DESC
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));

        return mongoTemplate.find(query, Leaderboard.class);
    }

    @Override
    public List<Leaderboard> findByClientIdOrDefaultWithSearch(UUID clientId, String search) {
        log.debug("Finding leaderboards by clientId: {} OR default with search: {}", clientId, search);

        List<Leaderboard> result = new ArrayList<>();

        // First, fetch client-specific leaderboards if clientId is provided
        if (clientId != null) {
            Query clientQuery = new Query();
            clientQuery.addCriteria(Criteria.where("clientId").is(clientId));
            addSearchCriteria(clientQuery, search);
            clientQuery.with(Sort.by(Sort.Direction.DESC, "createdDate"));
            result.addAll(mongoTemplate.find(clientQuery, Leaderboard.class));
        }

        // Then, fetch default leaderboards (limit to 2)
        Query defaultQuery = new Query();
        defaultQuery.addCriteria(Criteria.where("isDefault").is(true));
        defaultQuery.addCriteria(Criteria.where("status").is(LeaderboardStatus.ACTIVE));
        addSearchCriteria(defaultQuery, search);
        defaultQuery.with(Sort.by(Sort.Direction.DESC, "createdDate"));
        defaultQuery.limit(2);
        result.addAll(mongoTemplate.find(defaultQuery, Leaderboard.class));

        // Sort combined result by createdDate DESC (nulls last)
        result.sort(Comparator.comparing(Leaderboard::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())));

        return result;
    }

    @Override
    public Page<Leaderboard> findByClientIdOrDefaultWithSearchPaginated(UUID clientId, String search, Pageable pageable) {
        log.debug("Finding leaderboards by clientId: {} OR default (max 2) with search: {} and pagination", clientId, search);

        long clientTotal = 0;
        List<Leaderboard> clientResults = new ArrayList<>();

        // First, fetch paginated client-specific leaderboards
        if (clientId != null) {
            Query clientCountQuery = new Query();
            clientCountQuery.addCriteria(Criteria.where("clientId").is(clientId));
            addSearchCriteria(clientCountQuery, search);
            clientTotal = mongoTemplate.count(clientCountQuery, Leaderboard.class);

            Query clientQuery = new Query();
            clientQuery.addCriteria(Criteria.where("clientId").is(clientId));
            addSearchCriteria(clientQuery, search);
            clientQuery.with(Sort.by(Sort.Direction.DESC, "createdDate"));
            clientQuery.with(pageable);
            clientResults = mongoTemplate.find(clientQuery, Leaderboard.class);
        }

        // Then, fetch default leaderboards (limit to 2)
        Query defaultQuery = new Query();
        defaultQuery.addCriteria(Criteria.where("isDefault").is(true));
        defaultQuery.addCriteria(Criteria.where("status").is(LeaderboardStatus.ACTIVE));
        addSearchCriteria(defaultQuery, search);
        defaultQuery.with(Sort.by(Sort.Direction.DESC, "createdDate"));
        defaultQuery.limit(2);
        List<Leaderboard> defaultResults = mongoTemplate.find(defaultQuery, Leaderboard.class);

        // Combine: client results (paginated) + default results (max 2)
        List<Leaderboard> combinedResults = new ArrayList<>(clientResults);
        combinedResults.addAll(defaultResults);

        // Total count = client total + default count (max 2)
        long total = clientTotal + defaultResults.size();

        return new PageImpl<>(combinedResults, pageable, total);
    }

    @Override
    public Page<Leaderboard> findActiveByClientIdsOrDefaultWithSearchPaginated(
            List<UUID> clientIds, String search, Pageable pageable) {
        log.debug("Finding active leaderboards for clientIds: {} or active defaults with search: {} and pagination",
                clientIds, search);

        List<Criteria> orCriteria = new ArrayList<>();
        orCriteria.add(new Criteria().andOperator(
                Criteria.where("isDefault").is(true),
                Criteria.where("status").is(LeaderboardStatus.ACTIVE)
        ));

        if (clientIds != null && !clientIds.isEmpty()) {
            orCriteria.add(new Criteria().andOperator(
                    Criteria.where("clientId").in(clientIds),
                    Criteria.where("status").is(LeaderboardStatus.ACTIVE)
            ));
        }

        Query query = new Query(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
        addSearchCriteria(query, search);

        long total = mongoTemplate.count(query, Leaderboard.class);
        query.with(Sort.by(Sort.Direction.DESC, "createdDate"));
        query.with(pageable);

        List<Leaderboard> leaderboards = mongoTemplate.find(query, Leaderboard.class);
        return new PageImpl<>(leaderboards, pageable, total);
    }

    /**
     * Add search criteria to query (searches title, name, designation - case insensitive)
     */
    private void addSearchCriteria(Query query, String search) {
        if (search != null && !search.trim().isEmpty()) {
            Pattern pattern = Pattern.compile(search.trim(), Pattern.CASE_INSENSITIVE);

            List<Criteria> searchCriteria = new ArrayList<>();
            searchCriteria.add(Criteria.where("title").regex(pattern));
            searchCriteria.add(Criteria.where("name").regex(pattern));
            searchCriteria.add(Criteria.where("designation").regex(pattern));

            query.addCriteria(new Criteria().orOperator(searchCriteria.toArray(new Criteria[0])));
            log.debug("Applied search filter: {}", search);
        }
    }
}

