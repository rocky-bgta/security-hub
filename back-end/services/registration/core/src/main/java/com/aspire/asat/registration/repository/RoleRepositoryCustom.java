package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.Role;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface RoleRepositoryCustom {
    List<Role> searchRoles(String search, String sortBy, Sort.Direction direction);
}
