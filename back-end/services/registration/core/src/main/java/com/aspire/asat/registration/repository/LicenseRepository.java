package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.License;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LicenseRepository extends MongoRepository<License, UUID> {

}