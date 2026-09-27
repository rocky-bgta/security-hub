package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    
    /**
     * Find role by role name
     */
    Optional<Role> findByRoleName(String roleName);
    
    /**
     * Find all active roles
     */
    List<Role> findByStatus(String status);
    
    /**
     * Find roles by access level
     */
    List<Role> findByAccessLevel(Integer accessLevel);
    
    /**
     * Check if role exists by role name
     */
    boolean existsByRoleName(String roleName);
}
