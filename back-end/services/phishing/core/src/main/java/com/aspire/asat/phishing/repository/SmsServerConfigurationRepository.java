package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.SmsServerStatus;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmsServerConfigurationRepository extends MongoRepository<SmsServerConfiguration, String> {

    Page<SmsServerConfiguration> findByClientId(String clientId, Pageable pageable);

    Optional<SmsServerConfiguration> findByIdAndClientId(String id, String clientId);

    boolean existsByClientIdAndName(String clientId, String name);

    boolean existsByClientIdAndNameAndIdNot(String clientId, String name, String id);

    Optional<SmsServerConfiguration> findByClientIdAndIsDefaultTrueAndStatus(
            String clientId, SmsServerStatus status);

    List<SmsServerConfiguration> findByClientIdAndStatus(String clientId, SmsServerStatus status);

    long countByClientId(String clientId);
}
