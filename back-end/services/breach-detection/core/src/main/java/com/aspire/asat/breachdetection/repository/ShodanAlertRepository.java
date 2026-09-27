package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.model.ShodanAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShodanAlertRepository extends MongoRepository<ShodanAlert, String> {

    Optional<ShodanAlert> findByClientIdAndExternalAlertId(String clientId, String externalAlertId);

    Optional<ShodanAlert> findByIdAndClientId(String id, String clientId);

    Page<ShodanAlert> findByClientIdAndSubjectType(
            String clientId, ShodanSubjectType subjectType, Pageable pageable);

    Page<ShodanAlert> findByClientIdAndSubjectTypeAndStatus(
            String clientId, ShodanSubjectType subjectType, ShodanAlertStatus status, Pageable pageable);

    Page<ShodanAlert> findByClientIdAndSubjectTypeAndSeverity(
            String clientId, ShodanSubjectType subjectType, ShodanAlertSeverity severity, Pageable pageable);

    Page<ShodanAlert> findByClientIdAndSubjectTypeAndAlertType(
            String clientId, ShodanSubjectType subjectType, ShodanAlertType alertType, Pageable pageable);

    long countByClientIdAndSubjectType(String clientId, ShodanSubjectType subjectType);

    long countByClientIdAndSubjectTypeAndStatus(
            String clientId, ShodanSubjectType subjectType, ShodanAlertStatus status);

    long countByClientIdAndSubjectTypeAndAlertType(
            String clientId, ShodanSubjectType subjectType, ShodanAlertType alertType);

    long countByClientIdAndSubjectAndStatus(
            String clientId, String subject, ShodanAlertStatus status);

    @Query("{ 'clientId': ?0, 'subjectType': ?1, 'dateDetected': { $gte: ?2, $lt: ?3 } }")
    List<ShodanAlert> findInRange(String clientId, ShodanSubjectType subjectType, Instant from, Instant to);

    @Query("{ 'clientId': ?0, 'subjectType': ?1 }")
    List<ShodanAlert> findForTacticDistribution(String clientId, ShodanSubjectType subjectType);
}
