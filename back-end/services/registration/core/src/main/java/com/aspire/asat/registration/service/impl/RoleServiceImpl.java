package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.enums.SystemRole;
import com.aspire.asat.registration.data.roles.RoleRequestDTO;
import com.aspire.asat.registration.data.roles.RoleResponseDTO;
import com.aspire.asat.registration.exception.DuplicateDataFoundException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.exception.SystemRoleOperationException;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) {
        // Check if attempting to create a system role
        if (SystemRole.isSystemRole(roleRequestDTO.getRoleName())) {
            throw new SystemRoleOperationException("Cannot create system role: " + roleRequestDTO.getRoleName() + ". System roles are predefined and cannot be created via API.");
        }
        
        // Check if the role already exists
        if (roleRepository.existsByRoleName(roleRequestDTO.getRoleName())) {
            throw new DuplicateDataFoundException("Role with name " + roleRequestDTO.getRoleName() + " already exists.");
        }
        Role roleToSave = Role.toRole(roleRequestDTO);
        return Role.toRoleResponseDTO(roleRepository.save(roleToSave));
    }

    @Override
    public List<RoleResponseDTO> getAllRoles(String search, String sortBy, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String finalSortBy = (sortBy == null || sortBy.isEmpty()) ? "createdAt" : sortBy;
        List<Role> roles = roleRepository.searchRoles(search, finalSortBy, direction);
        return roles.stream()
                .map(Role::toRoleResponseDTO)
                .toList();
    }

    @Override
    public RoleResponseDTO getRoleById(String id) {
        return roleRepository.findById(id)
                .map(Role::toRoleResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + id + " not found."));
    }

    @Override
    public RoleResponseDTO updateRoleById(String id, RoleRequestDTO roleRequestDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + id + " not found."));

        // Check if attempting to update a system role
        if (existingRole.isSystemRole()) {
            throw new SystemRoleOperationException("Cannot update system role: " + existingRole.getRoleName() + ". System roles are immutable.");
        }
        
        // Check if attempting to change role name to a system role name
        if (!existingRole.getRoleName().equals(roleRequestDTO.getRoleName()) && 
            SystemRole.isSystemRole(roleRequestDTO.getRoleName())) {
            throw new SystemRoleOperationException("Cannot change role name to system role: " + roleRequestDTO.getRoleName() + ". System roles are predefined.");
        }

        // Check if the role name is being changed and if the new name already exists
        if (!existingRole.getRoleName().equals(roleRequestDTO.getRoleName()) &&
            roleRepository.existsByRoleName(roleRequestDTO.getRoleName())) {
            throw new DuplicateDataFoundException("Role with name " + roleRequestDTO.getRoleName() + " already exists.");
        }
        updateRoleInformation(roleRequestDTO, existingRole);
        return Role.toRoleResponseDTO(roleRepository.save(existingRole));
    }

    @Override
    public RoleResponseDTO deleteRoleById(String id) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id " + id + " not found."));

        // Check if attempting to delete a system role
        if (existingRole.isSystemRole()) {
            throw new SystemRoleOperationException("Cannot delete system role: " + existingRole.getRoleName() + ". System roles are protected and cannot be deleted.");
        }

        roleRepository.delete(existingRole);
        return Role.toRoleResponseDTO(existingRole);
    }

    private static void updateRoleInformation(RoleRequestDTO roleRequestDTO, Role existingRole) {
        if(roleRequestDTO.getRoleName() != null && !roleRequestDTO.getRoleName().isEmpty()) {
            existingRole.setRoleName(roleRequestDTO.getRoleName());
        }
        if(roleRequestDTO.getDescription() != null && !roleRequestDTO.getDescription().isEmpty()) {
            existingRole.setDescription(roleRequestDTO.getDescription());
        }
        if(roleRequestDTO.getAccessLevel() != null) {
            existingRole.setAccessLevel(roleRequestDTO.getAccessLevel());
        }
        if(roleRequestDTO.getColorTheme() != null && !roleRequestDTO.getColorTheme().isEmpty()) {
            existingRole.setColorTheme(roleRequestDTO.getColorTheme());
        }
        if(roleRequestDTO.getStatus() != null) {
            existingRole.setStatus(roleRequestDTO.getStatus());
        }
        existingRole.setUpdatedAt(Instant.now()); // Update the timestamp for last update
    }
    
    @Override
    public boolean isSystemRole(String roleId) {
        return roleRepository.findById(roleId)
                .map(Role::isSystemRole)
                .orElse(false);
    }
    
    @Override
    public boolean isSystemRoleByName(String roleName) {
        return SystemRole.isSystemRole(roleName);
    }
}
