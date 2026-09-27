package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.model.EmailTemplate;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom repository for EmailTemplate with advanced filter/search.
 */
public interface EmailTemplateRepositoryCustom {

    /**
     * Find email templates for a client (including global templates), applying optional search and filters.
     */
    List<EmailTemplate> findWithFilters(
            String clientId,
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            boolean isAspireAdmin,
            Pageable pageable);

    /**
     * Count email templates for a client (including global templates) for the same filters.
     */
    long countWithFilters(
            String clientId,
            String searchParam,
            String difficultyLevelId,
            String payloadTypeId,
            String location,
            List<String> tags,
            String language,
            EmailTemplateStatus status,
            TemplateType templateType,
            boolean isAspireAdmin);
}
