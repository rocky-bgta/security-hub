package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String>, RoleRepositoryCustom {
    Boolean existsByRoleName(String roleName);
    Role findByRoleName(String roleName);
}
