package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.model.InsecureWebOrganization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsecureWebOrganizationRepository extends MongoRepository<InsecureWebOrganization, String> {
    Optional<InsecureWebOrganization> findByClientId(String clientId);
    List<InsecureWebOrganization> findByDomainsIn(List<String> domains);
}
