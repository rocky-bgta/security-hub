package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.VishingScenarioStatus;
import com.aspire.asat.phishing.model.VishingScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VishingScenarioRepository extends MongoRepository<VishingScenario, String> {

    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    Page<VishingScenario> findByClientIdOrGlobal(String clientId, Pageable pageable);

    @Query("{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'scenarioName': {'$regex': ?1, '$options': 'i'}}" +
           "]}")
    Page<VishingScenario> searchByClientIdAndKeyword(String clientId, String keyword, Pageable pageable);

    @Query("{'$and': [{'_id': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    Optional<VishingScenario> findByIdAndClientIdOrGlobal(String id, String clientId);

    Optional<VishingScenario> findByIdAndClientId(String id, String clientId);

    boolean existsByClientIdAndScenarioName(String clientId, String scenarioName);

    boolean existsByClientIdAndScenarioNameAndIdNot(String clientId, String scenarioName, String id);

    long countByClientId(String clientId);

    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", count = true)
    long countByClientIdOrIsGlobal(String clientId);

    Page<VishingScenario> findByClientIdAndStatus(String clientId, VishingScenarioStatus status, Pageable pageable);
}
