package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ContentsAvailableLanguage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentsAvailableLanguageRepository extends MongoRepository<ContentsAvailableLanguage, String> {

    boolean existsByCodeIgnoreCase(String code);

    List<ContentsAvailableLanguage> findByActiveTrue();
}
