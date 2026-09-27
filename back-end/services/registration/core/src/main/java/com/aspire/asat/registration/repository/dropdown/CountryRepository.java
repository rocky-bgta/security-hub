package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.Country;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends MongoRepository<Country, String> {
    
    @Query("{ 'active': true }")
    List<Country> findAllActive();
    
    @Query("{ 'active': true }")
    List<Country> findAllActiveOrderByDisplayOrder();
    
    Optional<Country> findByCode(String code);

    Optional<Country> findByPhoneCode(String phoneCode);
    
    boolean existsByCode(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("{ 'active': true, 'code': ?0 }")
    Optional<Country> findActiveByCode(String code);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByPhoneCodeIgnoreCase(String phoneCode);

    Optional<Country> findByNameIgnoreCase(String name);

}
