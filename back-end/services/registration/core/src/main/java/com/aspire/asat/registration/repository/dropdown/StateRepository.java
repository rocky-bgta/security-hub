package com.aspire.asat.registration.repository.dropdown;
import com.aspire.asat.registration.model.dropdown.State;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends MongoRepository<State, String> {
    
    @Query("{ 'countryId': ?0, 'active': true }")
    List<State> findByCountryIdAndActiveTrue(String countryId);
    
    @Query("{ 'countryId': ?0, 'active': true }")
    List<State> findByCountryIdAndActiveTrueOrderByDisplayOrder(String countryId);
    
    @Query("{ 'active': true }")
    List<State> findAllActive();
    
    Optional<State> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("{ 'active': true, 'code': ?0 }")
    Optional<State> findActiveByCode(String code);
    
    @Query("{ 'active': true, 'countryId': ?0, 'code': ?1 }")
    Optional<State> findActiveByCountryIdAndCode(String countryId, String code);
    
    boolean existsByCountryIdAndCode(String countryId, String code);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCountryIdAndNameIgnoreCaseAndIdNot(String countryId, String name, String id);

    boolean existsByCountryIdAndCodeIgnoreCase(@NotBlank(message = "Country ID is required") String countryId, @NotBlank(message = "State code is required") String code);

    Optional<State> findByNameIgnoreCase(String name);
}
