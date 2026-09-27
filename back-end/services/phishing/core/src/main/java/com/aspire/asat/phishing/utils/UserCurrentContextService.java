package com.aspire.asat.phishing.utils;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.util.SerializationUtils;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class UserCurrentContextService {
    public static final String HEADER_CURRENT_USER_CONTEXT = "CurrentContext";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServletRequest httpServletRequest;

    @Autowired
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    private Optional<String> getHeaderValue(String headerName) {
        try {
            return Optional.ofNullable(httpServletRequest.getHeader(headerName));
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return Optional.empty();
    }

    public <T> T toObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public String getCurrentUserContextHeaderValue() {
        Optional<String> userTokenOpt = getHeaderValue(HEADER_CURRENT_USER_CONTEXT);
        if (userTokenOpt.isEmpty()) {
            throw new ResourceNotFoundException("Unauthorized resource access");
        }
        return userTokenOpt.get();
    }

    public CurrentUserContext getCurrentUserContext() {
        String base64Data = getCurrentUserContextHeaderValue();
        String jsonObject = SerializationUtils.toByteArrayToString(base64Data);
        try {
            return toObject(jsonObject, CurrentUserContext.class);
        } catch (Exception e) {
            log.error("Error while parsing CurrentUserContext: {}", e.getMessage());
            throw new ResourceNotFoundException("Unauthorized resource access");
        }
    }
    
    /**
     * Wrapper class that extends CurrentUserContext with additional convenience methods.
     */
    public static class PhishingUserContext {
        private final CurrentUserContext context;
        
        public PhishingUserContext(CurrentUserContext context) {
            this.context = context;
        }
        
        public String getClientId() {
            return context.getClientAdminId();
        }
        
        public String getUserId() {
            return context.getUserId();
        }
        
        public String getEmail() {
            return context.getEmail();
        }
        
        public CurrentUserContext getContext() {
            return context;
        }
    }
    
    public PhishingUserContext getPhishingUserContext() {
        return new PhishingUserContext(getCurrentUserContext());
    }
}

