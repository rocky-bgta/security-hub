package com.aspire.asat.registration.repository;


import com.aspire.asat.common.enums.TokenActionType;
import com.aspire.asat.registration.model.UserLoginHistory;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLoginHistoryRepository extends MongoRepository<UserLoginHistory, UUID> {

    Optional<UserLoginHistory> findByUsernameIgnoreCase(@NotBlank(message = "please.provide.username") String username);

    Optional<UserLoginHistory> findTopByUsernameIgnoreCaseAndActionOrderByLoginTimeDesc(String username, TokenActionType action);
    Optional<UserLoginHistory> findTopByUsernameIgnoreCaseOrderByLoginTimeDesc(String username);

}
