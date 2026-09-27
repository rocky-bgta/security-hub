package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.ClientAdmin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for querying ClientAdmin collection
 * Used to check ClientAdmin status for CLIENT_ADMIN users
 */
@Repository
public interface ClientAdminRepository extends MongoRepository<ClientAdmin, String> {
    
    Optional<ClientAdmin> findById(String id);

    List<ClientAdmin> findAllByMspId(String mspId);
}

