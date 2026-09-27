package com.aspire.asat.registration.repository.metadata;

import com.aspire.asat.registration.model.metadata.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationTypeRepository extends MongoRepository<OrganizationType, String> {

    Optional<OrganizationType> findById(String id);
    
    List<OrganizationType> findByIsActiveTrue();
    
    Optional<OrganizationType> findByIdAndIsActiveTrue(String id);
    
    boolean existsById(String  id);
    
    boolean existsByNameAndIsActiveTrue(String organizationType);

    boolean existsByNameIgnoreCaseAndIsActiveTrue(String organizationType);
}
