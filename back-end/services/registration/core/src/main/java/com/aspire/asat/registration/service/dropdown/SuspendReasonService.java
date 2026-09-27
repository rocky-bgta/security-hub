package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.SuspendReasonRequestDto;
import com.aspire.asat.registration.data.dropdown.SuspendReasonRespDto;

import java.util.List;

public interface SuspendReasonService {
    
    SuspendReasonRespDto createSuspendReason(SuspendReasonRequestDto request);
    
    List<SuspendReasonRespDto> getAllSuspendReasons();
    
    List<SuspendReasonRespDto> getActiveSuspendReasons();
    
    SuspendReasonRespDto getSuspendReasonById(String id);
    
    SuspendReasonRespDto updateSuspendReason(String id, SuspendReasonRequestDto request);
    
    void deleteSuspendReason(String id);
    
    SuspendReasonRespDto updateSuspendReasonStatus(String id, Boolean active);
}

