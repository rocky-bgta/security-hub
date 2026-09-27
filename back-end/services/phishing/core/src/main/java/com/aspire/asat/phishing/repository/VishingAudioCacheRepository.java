package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.VishingAudioCache;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VishingAudioCacheRepository extends MongoRepository<VishingAudioCache, String> {

    Optional<VishingAudioCache> findByCacheKey(String cacheKey);
}
