package com.aspire.asat.auth.repository;


import com.aspire.asat.auth.entity.AspireUser;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<AspireUser, UUID>, UserRepositoryCustom {

    /**
     * Find all users matching username (case-insensitive). Use this when duplicates may exist;
     * for single-result use {@link UserRepositoryCustom#findByUsernameIgnoreCase(String)}.
     */
    List<AspireUser> findAllByUsernameIgnoreCase(@NotBlank(message = "please.provide.username") String username);

    Optional<AspireUser> findByUserId(UUID userId);
}
