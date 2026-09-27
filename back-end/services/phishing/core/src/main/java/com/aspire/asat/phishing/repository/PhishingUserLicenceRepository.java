package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.PhishingUserLicence;
import com.aspire.asat.phishing.repository.custom.PhishingUserLicenceRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface PhishingUserLicenceRepository
        extends MongoRepository<PhishingUserLicence, String>, PhishingUserLicenceRepositoryCustom {

    long countByClientAdminIdAndProductPackageId(String clientAdminId, String productPackageId);

    List<PhishingUserLicence> findByClientAdminIdAndProductPackageIdAndUserIdIn(
            String clientAdminId, String productPackageId, Collection<String> userIds);

    boolean existsByClientAdminIdAndProductPackageIdAndUserId(
            String clientAdminId, String productPackageId, String userId);
}
