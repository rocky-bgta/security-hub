package com.aspire.asat.registration.repository.dropdown;

import com.aspire.asat.registration.model.dropdown.Language;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends MongoRepository<Language, String> {
    
    List<Language> findByActiveTrue();
    
    Optional<Language> findByCode(String code);
    
    boolean existsByCode(String code);

    boolean existsByDisplayNameIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(@NotBlank(message = "Language code is required") String code);
}
