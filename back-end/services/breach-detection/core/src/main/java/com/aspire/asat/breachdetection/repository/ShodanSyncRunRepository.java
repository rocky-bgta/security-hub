package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.model.ShodanSyncRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShodanSyncRunRepository extends MongoRepository<ShodanSyncRun, String> {

    Optional<ShodanSyncRun> findTopByClientIdAndSubjectTypeOrderByStartedAtDesc(
            String clientId, ShodanSubjectType subjectType);
}
