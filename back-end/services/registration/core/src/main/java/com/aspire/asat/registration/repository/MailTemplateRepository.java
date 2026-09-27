package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.MailTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MailTemplateRepository extends MongoRepository<MailTemplate, String> {

    Optional<MailTemplate> findByType(String type);

    List<MailTemplate> findByStatus(String status);
}
