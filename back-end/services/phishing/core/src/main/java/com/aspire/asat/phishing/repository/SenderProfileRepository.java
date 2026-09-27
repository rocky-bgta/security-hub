package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.repository.custom.SenderProfileRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for sender profiles.
 */
@Repository
public interface SenderProfileRepository extends MongoRepository<SenderProfile, String>, SenderProfileRepositoryCustom {

    /**
     * Find all profiles for a client with pagination
     */
    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    Page<SenderProfile> findByClientIdOrGlobal(String clientId, Pageable pageable);

    /**
     * Find all profiles with pagination.
     */
    Page<SenderProfile> findAll(Pageable pageable);

    /**
     * Find profiles by client with optional search
     */
    @Query("{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'$or': [" +
           "  {'profileName': {'$regex': ?1, '$options': 'i'}}," +
           "  {'fromAddress': {'$regex': ?1, '$options': 'i'}}," +
           "  {'host': {'$regex': ?1, '$options': 'i'}}" +
           "]}" +
           "]}")
    Page<SenderProfile> searchByClientIdAndKeyword(String clientId, String keyword, Pageable pageable);

    /**
     * Find all profiles by search keyword.
     */
    @Query("{'$or': [" +
           "  {'profileName': {'$regex': ?0, '$options': 'i'}}," +
           "  {'fromAddress': {'$regex': ?0, '$options': 'i'}}," +
           "  {'host': {'$regex': ?0, '$options': 'i'}}" +
           "]}")
    Page<SenderProfile> searchByKeyword(String keyword, Pageable pageable);

    /**
     * Find profiles by client and profile type
     */
    Page<SenderProfile> findByClientIdAndProfileType(String clientId, ProfileType profileType, Pageable pageable);

    /**
     * Find all profiles by profile type.
     */
    Page<SenderProfile> findByProfileType(ProfileType profileType, Pageable pageable);

    /**
     * Find profile by ID and client ID
     */
    @Query("{'$and': [{'_id': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    Optional<SenderProfile> findByIdAndClientIdOrGlobal(String id, String clientId);

    /**
     * Find profile by ID and client ID (strict, non-global fallback).
     * Kept for backward-compatible call sites (e.g., campaign validation).
     */
    Optional<SenderProfile> findByIdAndClientId(String id, String clientId);

    /**
     * Check if profile name exists for client
     */
    boolean existsByClientIdAndProfileName(String clientId, String profileName);

    /**
     * Check if profile name exists for client excluding specific profile
     */
    boolean existsByClientIdAndProfileNameAndIdNot(String clientId, String profileName, String id);

    /**
     * Find all profiles for a client (no pagination)
     */
    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    List<SenderProfile> findByClientIdOrGlobal(String clientId);

    /**
     * Count profiles accessible by a client (client-specific + global)
     */
    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", count = true)
    long countByClientIdOrIsGlobal(String clientId);

    /**
     * Count profiles by client and type
     */
    @Query(value = "{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'profileType': ?1}]}", count = true)
    long countByClientIdAndProfileType(String clientId, ProfileType profileType);

    /**
     * Count all profiles by type.
     */
    long countByProfileType(ProfileType profileType);

    /**
     * Find verified profiles for a client
     */
    @Query("{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'isVerified': true}]}")
    List<SenderProfile> findByClientIdAndIsVerifiedTrue(String clientId);

    /**
     * Check if profile is used in any campaign (placeholder - will be implemented with Campaign entity)
     */
    @Query(value = "{ '_id': ?0 }", exists = true)
    default boolean isProfileUsedInCampaign(String profileId) {
        // TODO: Implement when Campaign entity is available
        return false;
    }
}
