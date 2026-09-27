package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.model.MicroContentJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MicroContentJobRepository extends MongoRepository<MicroContentJob, String> {

    Optional<MicroContentJob> findByJobId(UUID jobId);

    Optional<MicroContentJob> findByJobIdAndClientId(UUID jobId, String clientId);

    boolean existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
            String clientId, String deepfakeVideoId, Collection<DeepfakeJobStatus> statuses);

    Optional<MicroContentJob> findFirstByClientIdAndVideosDeepfakeVideoIdAndStatusIn(
            String clientId, String deepfakeVideoId, Collection<DeepfakeJobStatus> statuses);

    List<MicroContentJob> findByClientIdAndVideosDeepfakeVideoIdIn(
            String clientId, Collection<String> deepfakeVideoIds);
}
