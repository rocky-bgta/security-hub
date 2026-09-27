package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.Timezone;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimezoneRepository extends MongoRepository<Timezone, String> {
    
    @Query("{ 'stateId': ?0, 'active': true }")
    List<Timezone> findByStateIdAndActiveTrue(String stateId);
    
    @Query("{ 'stateId': ?0, 'active': true }")
    List<Timezone> findByStateIdAndActiveTrueOrderByDisplayOrder(String stateId);
    
    @Query("{ 'countryId': ?0, 'active': true }")
    List<Timezone> findByCountryIdAndActiveTrue(String countryId);
    
    @Query("{ 'active': true }")
    List<Timezone> findAllActive();
    
    Optional<Timezone> findByTimezoneId(String timezoneId);
    
    boolean existsByTimezoneId(String timezoneId);
    
    @Query("{ 'active': true, 'timezoneId': ?0 }")
    Optional<Timezone> findActiveByTimezoneId(String timezoneId);
    
    @Query("{ 'active': true, 'stateId': ?0, 'timezoneId': ?1 }")
    Optional<Timezone> findActiveByStateIdAndTimezoneId(String stateId, String timezoneId);
    
    boolean existsByStateIdAndTimezoneId(String stateId, String timezoneId);

    boolean existsByStateIdAndTimezoneIdIgnoreCase(String stateId, String timezoneId);
}
