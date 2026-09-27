package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.AdminDashboardAggregate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AdminDashboardAggregateRepository extends MongoRepository<AdminDashboardAggregate, String> {
    Optional<AdminDashboardAggregate> findByClientIdAndPeriodAndDate(String clientId, StatsPeriod period, LocalDate date);
    Optional<AdminDashboardAggregate> findTopByClientIdAndPeriodOrderByDateDesc(String clientId, StatsPeriod period);
}
