package com.aspire.asat.auth.repository;


import com.aspire.asat.auth.entity.UserLoginHistory;
import com.aspire.asat.common.enums.TokenActionType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLoginHistoryRepository extends MongoRepository<UserLoginHistory, UUID> {

    Optional<UserLoginHistory> findByUsernameIgnoreCase(@NotBlank(message = "please.provide.username") String username);

    Optional<UserLoginHistory> findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(String username, TokenActionType action);

}
