package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.systemUser.request.SystemUserRequestDTO;
import com.aspire.asat.registration.data.systemUser.request.SystemUserUpdateRequestDTO;
import com.aspire.asat.registration.data.systemUser.response.SystemUserResponseDTO;

import java.util.List;

/**
 * Service interface for System User management
 */
public interface SystemUserService {

    /**
     * Create a new system user
     */
    SystemUserResponseDTO createSystemUser(SystemUserRequestDTO requestDTO);

    /**
     * Get system user by ID
     */
    SystemUserResponseDTO getSystemUserById(String userId);

    /**
     * Get all system users with pagination, sorting, and filtering
     */
    List<SystemUserResponseDTO> listSystemUsers(String search, UserStatus status, List<String> departments, int offset, int pageSize);

    /**
     * Count system users with filtering
     */
    long countSystemUsers(String search, UserStatus status, List<String> departments);

    /**
     * Update system user details and status
     */
    SystemUserResponseDTO updateSystemUser(String userId, SystemUserUpdateRequestDTO requestDTO);
}
