package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.TimezoneRequestDto;
import com.aspire.asat.registration.data.dropdown.TimezoneRespDto;

import java.util.List;

public interface TimezoneService {
    
    TimezoneRespDto createTimezone(TimezoneRequestDto request);

    List<TimezoneRespDto> getAllTimezones();

    List<TimezoneRespDto> getActiveTimezones();

    List<TimezoneRespDto> getTimezonesByStateId(String stateId);
    
    List<TimezoneRespDto> getActiveTimezonesByStateId(String stateId);
    
    List<TimezoneRespDto> getTimezonesByCountryId(String countryId);
    
    List<TimezoneRespDto> getActiveTimezonesByCountryId(String countryId);

    TimezoneRespDto getTimezoneById(String id);

    TimezoneRespDto getTimezoneByTimezoneId(String timezoneId);

    TimezoneRespDto updateTimezone(String id, TimezoneRequestDto request);
    
    void deleteTimezone(String id);
    
    boolean existsByTimezoneId(String timezoneId);
    
    boolean existsByStateIdAndTimezoneId(String stateId, String timezoneId);
}
