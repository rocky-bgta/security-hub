package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ClientCertificateTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientCertificateTemplateRepository extends MongoRepository<ClientCertificateTemplate, String> {

    /**
     * Find active client certificate template by client admin ID
     *
     * @param clientAdminId The client admin ID
     * @param active        The active status
     * @return Optional of ClientCertificateTemplate
     */
    Optional<ClientCertificateTemplate> findByClientAdminIdAndActive(String clientAdminId, boolean active);

    /**
     * Find client certificate template by client admin ID
     *
     * @param clientAdminId The client admin ID
     * @return Optional of ClientCertificateTemplate
     */
    Optional<ClientCertificateTemplate> findByClientAdminId(String clientAdminId);

    /**
     * Check if an active client certificate template exists for a client admin
     *
     * @param clientAdminId The client admin ID
     * @param active        The active status
     * @return true if exists, false otherwise
     */
    boolean existsByClientAdminIdAndActive(String clientAdminId, boolean active);
}

