package com.aspire.asat.registration.repository.msp;

import com.aspire.asat.registration.model.msp.MspUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MSP Users in the dedicated MSP_USERS database
 * This repository handles all MSP onboarding related data operations
 */
@Repository
public interface MspUsersRepository extends MongoRepository<MspUser, String> {
    /**
     * Check if MSP user exists by MSP admin email
     */
    boolean existsByMspAdminEmailIgnoreCase(String mspAdminEmail);

    /**
     * Find MSP users by country
     * Matches the country field in MspUser model
     */
    List<MspUser> findByCountry(String country);

    /**
     * Find MSP user by MSP ID
     * Matches the mspId field in MspUser model
     */
    java.util.Optional<MspUser> findByMspId(String mspId);

    /**
     * Find MSP users by status with pagination
     */
    Page<MspUser> findByStatus(String status, Pageable pageable);

    /**
     * Find all MSP users with pagination (no filter)
     */
    Page<MspUser> findAll(Pageable pageable);  // Already exists in MongoRepository

}
