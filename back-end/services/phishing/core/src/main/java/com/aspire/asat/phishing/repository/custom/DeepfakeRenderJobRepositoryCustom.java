package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.model.DeepfakeRenderJob;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

/**
 * Custom queries for deepfake video list/count with tenant scope and optional
 * filtering by upload date range, title and description.
 */
public interface DeepfakeRenderJobRepositoryCustom {

    List<DeepfakeRenderJob> findWithFilters(
            String clientId,
            Instant uploadedFrom,
            Instant uploadedTo,
            String title,
            String description,
            Pageable pageable);

    long countWithFilters(
            String clientId,
            Instant uploadedFrom,
            Instant uploadedTo,
            String title,
            String description);
}
