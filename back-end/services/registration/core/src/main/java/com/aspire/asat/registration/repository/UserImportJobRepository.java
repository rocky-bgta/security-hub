package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.UserImportJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserImportJobRepository extends MongoRepository<UserImportJob, String> {

    List<UserImportJob> findByClientAdminIdOrderByCreatedAtDesc(String clientAdminId);

    List<UserImportJob> findByStatus(String status);
}

