package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByTokenAndIsRevokedFalse(String token);

    Optional<RefreshToken> findByUsernameAndDeviceId(String username, String deviceId);
    
    List<RefreshToken> findByUsername(String username);

    Optional<RefreshToken> findTopByUsernameOrderByCreatedAtDesc(String username);

    @Query("{'username': ?0, 'isRevoked': false, 'expiryDate': {$gt: ?1}}")
    List<RefreshToken> findValidTokensByUsername(String username, Instant now);
    
    @Query("{'expiryDate': {$lt: ?0}}")
    List<RefreshToken> findExpiredTokens(java.time.Instant now);
    
    @Query("{'username': ?0}")
    void deleteByUsername(String username);
    
    @Query("{'username': ?0, 'deviceId': ?1}")
    void deleteByUsernameAndDeviceId(String username, String deviceId);
    
    @Query("{'username': ?0, 'isRevoked': false}")
    List<RefreshToken> findByUsernameAndIsRevokedFalse(String username);
}
