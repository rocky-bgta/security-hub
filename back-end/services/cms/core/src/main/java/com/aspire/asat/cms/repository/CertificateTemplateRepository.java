package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.model.CertificateTemplate;
import com.aspire.asat.cms.repository.custom.CertificateTemplateRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateTemplateRepository extends MongoRepository<CertificateTemplate, String>, CertificateTemplateRepositoryCustom {

    Optional<CertificateTemplate> findByTemplateName(String templateName);

    List<CertificateTemplate> findByStatus(Status status);

    boolean existsByTemplateName(String templateName);

    Optional<CertificateTemplate> findByIsDefaultTrue();
    Optional<CertificateTemplate> findByIsTrialTemplateTrue();
}
