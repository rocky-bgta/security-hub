package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.model.ProviderCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Client-scoped access to {@link ProviderCredential} documents.
 */
@Repository
public interface ProviderCredentialRepository extends MongoRepository<ProviderCredential, String> {

    Optional<ProviderCredential> findByIdAndClientId(String id, String clientId);

    Optional<ProviderCredential> findByClientIdAndProviderName(String clientId, String providerName);

    List<ProviderCredential> findByClientIdAndProviderNameIgnoreCase(String clientId, String providerName);

    Optional<ProviderCredential> findByClientIdAndCategoryAndIsDefaultTrue(String clientId, ProviderCategory category);

    boolean existsByClientIdAndProviderName(String clientId, String providerName);

    boolean existsByClientIdAndProviderNameAndIdNot(String clientId, String providerName, String id);

    Page<ProviderCredential> findByClientId(String clientId, Pageable pageable);

    Page<ProviderCredential> findByClientIdAndProviderNameIgnoreCase(String clientId, String providerName, Pageable pageable);

    Page<ProviderCredential> findByClientIdAndIsActive(String clientId, boolean isActive, Pageable pageable);

    Page<ProviderCredential> findByClientIdAndProviderNameIgnoreCaseAndIsActive(
            String clientId, String providerName, boolean isActive, Pageable pageable);

    List<ProviderCredential> findByClientIdAndCategory(String clientId, ProviderCategory category);
}
