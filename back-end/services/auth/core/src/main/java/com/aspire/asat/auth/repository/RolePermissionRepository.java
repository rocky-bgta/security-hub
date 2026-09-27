package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.RolePermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {

    Optional<RolePermission> findByRoleId(String roleId);
}
