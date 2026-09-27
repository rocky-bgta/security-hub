package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.StateRequestDto;
import com.aspire.asat.registration.data.dropdown.StateRespDto;

import java.util.List;

public interface StateService {
    
    StateRespDto createState(StateRequestDto request);
    
    List<StateRespDto> getAllStates();
    
    List<StateRespDto> getActiveStates();
    
    List<StateRespDto> getStatesByCountryId(String countryId);
    
    List<StateRespDto> getActiveStatesByCountryId(String countryId);

    StateRespDto getStateById(String id);

    StateRespDto getStateByCode(String code);

    StateRespDto updateState(String id, StateRequestDto request);
    
    void deleteState(String id);
    
    boolean existsByCode(String code);
    
    boolean existsByCountryIdAndCode(String countryId, String code);
}
