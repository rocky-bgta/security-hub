package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.model.ShodanMonitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShodanMonitorRepository extends MongoRepository<ShodanMonitor, String> {

    Optional<ShodanMonitor> findByClientIdAndSubjectTypeAndSubject(String clientId, ShodanSubjectType subjectType, String subject);

    Optional<ShodanMonitor> findByIdAndClientId(String id, String clientId);

    Page<ShodanMonitor> findByClientIdAndSubjectType(String clientId, ShodanSubjectType subjectType, Pageable pageable);

    List<ShodanMonitor> findByClientIdAndSubjectTypeAndEnabledTrue(String clientId, ShodanSubjectType subjectType);

    long countByClientIdAndSubjectType(String clientId, ShodanSubjectType subjectType);
}
