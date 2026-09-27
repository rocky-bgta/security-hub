package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.SuspendReason;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspendReasonRepository extends MongoRepository<SuspendReason, String> {
    
    List<SuspendReason> findByActiveTrue();
    
    Optional<SuspendReason> findByName(String name);
    
    boolean existsByName(String name);
}

