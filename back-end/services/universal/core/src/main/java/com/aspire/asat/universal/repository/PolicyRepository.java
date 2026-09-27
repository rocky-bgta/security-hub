package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.Policy;
import com.aspire.asat.universal.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends MongoRepository<Policy, String>, PolicyRepositoryCustom {
    Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);
}
