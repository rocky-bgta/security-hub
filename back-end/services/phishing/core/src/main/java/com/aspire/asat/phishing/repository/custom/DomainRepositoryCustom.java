package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.model.Domain;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom repository for Domain list/search criteria queries.
 */
public interface DomainRepositoryCustom {

    List<Domain> findWithFilters(String clientId, String search, List<DomainStatus> statuses, Pageable pageable);

    long countWithFilters(String clientId, String search, List<DomainStatus> statuses);
}
