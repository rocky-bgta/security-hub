package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PolicyType;
import com.aspire.asat.universal.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyTypeRepository extends MongoRepository<PolicyType, String> {
    List<PolicyType> findByStatus(Status status);
}

