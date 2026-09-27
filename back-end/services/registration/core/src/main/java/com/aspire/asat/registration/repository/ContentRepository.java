package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Content;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContentRepository extends MongoRepository<Content, UUID> {

    long countByType(String type);

}