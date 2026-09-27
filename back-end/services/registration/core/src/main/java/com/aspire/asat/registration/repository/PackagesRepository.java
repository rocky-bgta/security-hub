package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Packages;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PackagesRepository extends MongoRepository<Packages, UUID> {

}
