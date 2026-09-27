package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import com.aspire.asat.phishing.repository.custom.DeepfakeRenderJobRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeepfakeRenderJobRepository
        extends MongoRepository<DeepfakeRenderJob, String>, DeepfakeRenderJobRepositoryCustom {

    Optional<DeepfakeRenderJob> findByRenderId(UUID renderId);

    Optional<DeepfakeRenderJob> findByRenderIdAndClientId(UUID renderId, String clientId);
}
