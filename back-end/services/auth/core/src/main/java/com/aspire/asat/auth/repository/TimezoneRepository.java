package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.Timezone;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimezoneRepository extends MongoRepository<Timezone, String> {
}
