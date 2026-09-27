package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.model.InsecureWebBreachFinding;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsecureWebBreachFindingRepository extends MongoRepository<InsecureWebBreachFinding, String> {
    Optional<InsecureWebBreachFinding> findByClientIdAndExternalFindingId(String clientId, String externalFindingId);

    Optional<InsecureWebBreachFinding> findByIdAndClientId(String id, String clientId);
    /**
     * Returns email-bearing findings for the client whose {@code timestamp} falls in [from, to).
     * Used by the dashboard "Breach Activity" chart.
     */
    @Query("{ 'clientId': ?0, 'email': { $exists: true, $ne: null, $ne: '' }, 'timestamp': { $gte: ?1, $lt: ?2 } }")
    List<InsecureWebBreachFinding> findEmailFindingsBetween(String clientId, Instant from, Instant to);

    @Query(value = "{ 'clientId': ?0, 'email': { $exists: true, $ne: null, $ne: '' } }", count = true)
    long countEmailFindingsByClientId(String clientId);
}
