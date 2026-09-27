package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.Industry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryRepository extends MongoRepository<Industry, String> {
    
    List<Industry> findByActiveTrue();

    List<Industry> findByActiveTrueAndOrganizationTypeId(String organizationTypeId);
    
    Optional<Industry> findByCode(String code);

    Optional<Industry> findByNameIgnoreCase(String name);
    
    boolean existsByCode(String code);
}
