package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.ShortUrl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends MongoRepository<ShortUrl, String> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    Optional<ShortUrl> findByTrackingId(String trackingId);
}
