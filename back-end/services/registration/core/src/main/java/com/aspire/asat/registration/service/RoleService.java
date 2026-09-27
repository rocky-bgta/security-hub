package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.roles.RoleRequestDTO;
import com.aspire.asat.registration.data.roles.RoleResponseDTO;

import java.util.List;

public interface RoleService {
    RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO);

    List<RoleResponseDTO> getAllRoles(String search, String sortBy, String order);

    RoleResponseDTO getRoleById(String id);

    RoleResponseDTO updateRoleById(String id, RoleRequestDTO roleRequestDTO);

    RoleResponseDTO deleteRoleById(String id);
    
    /**
     * Check if a role is a system role
     * @param roleId the role ID to check
     * @return true if the role is a system role
     */
    boolean isSystemRole(String roleId);
    
    /**
     * Check if a role name corresponds to a system role
     * @param roleName the role name to check
     * @return true if the role name matches a system role
     */
    boolean isSystemRoleByName(String roleName);
}
