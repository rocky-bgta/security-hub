package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.model.AiProviderSecretRef;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiProviderSecretRefRepository extends MongoRepository<AiProviderSecretRef, String> {

    List<AiProviderSecretRef> findByClientAdminIdOrderByProviderTypeAsc(String clientAdminId);

    Optional<AiProviderSecretRef> findByClientAdminIdAndProviderTypeAndActiveIsTrue(String clientAdminId, AiProviderType providerType);
}

