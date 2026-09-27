package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.AspireUser;

import java.util.Optional;

/**
 * Custom fragment for user lookup by username. Returns the first match when multiple
 * users exist with the same username (case-insensitive), avoiding MongoDB "non unique result".
 */
public interface UserRepositoryCustom {

    /**
     * Find one user by username (case-insensitive). If multiple documents match (e.g. duplicates),
     * returns the first and logs a warning. Use this for login and other single-user lookups.
     */
    Optional<AspireUser> findByUsernameIgnoreCase(String username);
}
