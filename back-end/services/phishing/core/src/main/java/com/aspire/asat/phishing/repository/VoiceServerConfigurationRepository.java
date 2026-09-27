package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.VoiceServerStatus;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoiceServerConfigurationRepository extends MongoRepository<VoiceServerConfiguration, String> {

    Page<VoiceServerConfiguration> findByClientId(String clientId, Pageable pageable);

    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    Page<VoiceServerConfiguration> findByClientIdOrGlobal(String clientId, Pageable pageable);

    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", count = true)
    long countByClientIdOrGlobal(String clientId);

    Optional<VoiceServerConfiguration> findByIdAndClientId(String id, String clientId);

    @Query("{'$and': [{'_id': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    Optional<VoiceServerConfiguration> findByIdAndClientIdOrGlobal(String id, String clientId);

    boolean existsByClientIdAndName(String clientId, String name);

    boolean existsByClientIdAndNameAndIdNot(String clientId, String name, String id);

    boolean existsByIsGlobalTrueAndName(String name);

    boolean existsByIsGlobalTrueAndNameAndIdNot(String name, String id);

    Optional<VoiceServerConfiguration> findByClientIdAndIsDefaultTrueAndStatus(
            String clientId, VoiceServerStatus status);

    List<VoiceServerConfiguration> findByClientIdAndStatus(String clientId, VoiceServerStatus status);

    List<VoiceServerConfiguration> findByIsGlobalTrueAndIsDefaultTrue();

    long countByClientId(String clientId);
}
