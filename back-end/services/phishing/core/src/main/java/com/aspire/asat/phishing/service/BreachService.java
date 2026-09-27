package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import com.aspire.asat.phishing.dto.enums.BreachStatus;
import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import com.aspire.asat.phishing.dto.request.BreachConfigRequest;
import com.aspire.asat.phishing.dto.request.BreachStatusRequest;
import com.aspire.asat.phishing.dto.request.RecipientActionRequest;
import com.aspire.asat.phishing.dto.response.*;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for breach operations.
 */
public interface BreachService {

    // Breach Record Operations
    List<BreachRecordDto> getBreaches(int offset, int pageSize, String keyword, 
            String domain, BreachStatus status, BreachSeverity severity,
            Instant startDate, Instant endDate);
    
    long countBreaches(String keyword, String domain, BreachStatus status, 
            BreachSeverity severity, Instant startDate, Instant endDate);
    
    BreachRecordDto getBreachById(String id);
    
    BreachRecordDto updateBreachStatus(String id, BreachStatusRequest request);
    
    void deleteBreach(String id);
    
    byte[] exportBreaches(String format, String domain, BreachStatus status);

    // Recipient Breach Operations
    List<RecipientBreachDto> getRecipientBreaches(int offset, int pageSize, 
            String breachRecordId, String keyword, RecipientBreachStatus status);
    
    long countRecipientBreaches(String breachRecordId, String keyword, 
            RecipientBreachStatus status);
    
    RecipientBreachDto getRecipientBreachById(String id);
    
    RecipientBreachDto notifyRecipient(String id, RecipientActionRequest request);
    
    RecipientBreachDto resetRecipientPassword(String id, RecipientActionRequest request);
    
    RecipientBreachDto resolveRecipientBreach(String id, RecipientActionRequest request);

    // Configuration Operations
    BreachConfigDto getConfig();
    
    BreachConfigDto updateConfig(BreachConfigRequest request);

    // Sync Operations
    BreachSyncResultDto triggerManualSync();
}
