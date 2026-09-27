package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.MspType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MspTypeRepository extends MongoRepository<MspType, String> {

    List<MspType> findByIsActiveTrue();

    Optional<MspType> findByName(String name);

    boolean existsByName(String name);
}
