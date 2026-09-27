package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.VatConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VatConfigurationRepository extends MongoRepository<VatConfiguration, String> {

    // Find VAT configuration by countryId (UUID)
    // Note: findById() is already available from MongoRepository and can be used with countryId

}
