package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.dto.AspireUserCreateRequestDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.dto.AspireUserUpdateRequestDto;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.AspireUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Centralized user management service that acts as the single source of truth
 * for all user information across the system.
 */
public interface AspireUserService {

    /**
     * Create a new AspireUser record
     */
    AspireUserDto createUser(AspireUserCreateRequestDto requestDto);

    /**
     * Create a new AspireUser record from domain-specific user data
     * This method is used internally by other services to sync user data
     */
    AspireUserDto createAspireUser(String baseUserId, UserType userType, Object domainUserData);

    /**
     * Update an existing AspireUser record from domain-specific user data
     * This method is used internally by other services to sync user data updates
     */
     void updateAspireUser(String baseUserId, UserType userType, Object domainUserData);

    /**
     * Update an existing AspireUser record
     */
    AspireUserDto updateUser(UUID id, AspireUserUpdateRequestDto requestDto);


    /**
     * Get user by ID
     */
    Optional<AspireUserDto> getUserById(UUID userId);

    /**
     * Get user by email
     */
    Optional<AspireUserDto> getUserByEmail(String email);

    /**
     * Get user by username
     */
    Optional<AspireUserDto> getUserByUsername(String username);

    /**
     * Get all users with pagination
     */
    List<AspireUserDto> getAllUsers(int offset, int pageSize);

    /**
     * Get users by user type
     */
    List<AspireUserDto> getUsersByType(String userType);
    List<AspireUserDto> getUsersByTypeAndClientAdminId(String userType, String clientAdminId);

    /**
     * Get users by status
     */
    List<AspireUserDto> getUsersByStatus(String status);

    /**
     * Sends the AspireUser to Phishing module as UserRiskProfile. Call when credentials are sent (e.g. package assignment).
     * Failures are logged and do not affect the main flow.
     */
    void saveUserRiskProfileInPhishing(AspireUser aspireUser);

    /**
     * Update risk group for AspireUser by base userId.
     */
    AspireUserDto updateRiskGroup(String userId, RiskGroup riskGroup);

}
