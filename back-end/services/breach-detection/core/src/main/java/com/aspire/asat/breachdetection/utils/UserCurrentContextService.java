package com.aspire.asat.breachdetection.utils;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.util.SerializationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public CurrentUserContext getCurrentUserContext() {
        String header = httpServletRequest.getHeader(HEADER_CURRENT_USER_CONTEXT);
        if (header == null || header.isBlank()) {
            throw new IllegalStateException("Unauthorized resource access");
        }
        String jsonObject = SerializationUtils.toByteArrayToString(header);
        try {
            return objectMapper.readValue(jsonObject, CurrentUserContext.class);
        } catch (Exception e) {
            log.error("Error while parsing CurrentUserContext: {}", e.getMessage());
            throw new IllegalStateException("Unauthorized resource access");
        }
    }
}
