package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.OrganizationSize;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationSizeRepository extends MongoRepository<OrganizationSize, String> {
    
    List<OrganizationSize> findAll();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByRangeIgnoreCase(String range);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    boolean existsByRangeIgnoreCaseAndIdNot(String range, String id);
}