package com.aspire.asat.registration.service.dropdown;


import com.aspire.asat.registration.data.dropdown.CountryRequestDto;
import com.aspire.asat.registration.data.dropdown.CountryRespDto;

import java.util.List;

public interface CountryService {
    
    CountryRespDto createCountry(CountryRequestDto request);
    
    List<CountryRespDto> getAllCountries();
    
    List<CountryRespDto> getActiveCountries();

    CountryRespDto getCountryById(String id);

    CountryRespDto getCountryByCode(String code);

    CountryRespDto getCountryByPhoneCode(String phoneCode);

    CountryRespDto updateCountry(String id, CountryRequestDto request);
    
    void deleteCountry(String id);
    
    boolean existsByCode(String code);
}
