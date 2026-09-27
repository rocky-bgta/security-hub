package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.MicrosoftIntegration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MicrosoftIntegrationRepository extends MongoRepository<MicrosoftIntegration, String> {

    Optional<MicrosoftIntegration> findByClientAdminIdAndActive(String clientAdminId, boolean active);

    Optional<MicrosoftIntegration> findByClientAdminId(String clientAdminId);

    boolean existsByClientAdminIdAndActive(String clientAdminId, boolean active);
}

