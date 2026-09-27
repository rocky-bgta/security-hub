package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.DeepfakeBackgroundImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeepfakeBackgroundImageRepository extends MongoRepository<DeepfakeBackgroundImage, String> {

    Optional<DeepfakeBackgroundImage> findByBackgroundImageId(UUID backgroundImageId);

    boolean existsByFileKeyAndIsGlobalTrue(String fileKey);
}
