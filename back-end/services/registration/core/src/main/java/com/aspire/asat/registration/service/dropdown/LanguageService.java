package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.LanguageRequestDto;
import com.aspire.asat.registration.data.dropdown.LanguageRespDto;

import java.util.List;

public interface LanguageService {
    
    LanguageRespDto createLanguage(LanguageRequestDto request);
    
    LanguageRespDto getLanguageById(String id);
    
    List<LanguageRespDto> getActiveLanguages();
    
    LanguageRespDto updateLanguage(String id, LanguageRequestDto request);
    
    void deleteLanguage(String id);
}
