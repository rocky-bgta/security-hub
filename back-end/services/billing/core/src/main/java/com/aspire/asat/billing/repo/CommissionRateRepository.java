package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.CommissionRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommissionRateRepository extends MongoRepository<CommissionRate, String> {

    Optional<CommissionRate> findByClientId(String clientId);

    Page<CommissionRate> findAll(Pageable pageable);

    boolean existsByClientId(String clientId);
}
