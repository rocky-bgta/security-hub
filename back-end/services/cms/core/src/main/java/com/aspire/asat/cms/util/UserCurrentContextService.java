package com.aspire.asat.cms.util;


import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.ResponseMessage;
import com.aspire.asat.common.util.SerializationUtils;
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


    private String getCurrentUserContextHeaderValue() {
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
            throw new ResourceNotFoundException(ResponseMessage.UNAUTHORIZED_RESOURCE_ACCESS.getResponseMessage());
        }
    }

    /**
     * Best-effort client admin id from the current request context.
     * Returns empty when there is no CurrentContext header or no clientAdminId
     * (Aspire admin / internal service calls) — callers treat that as see-all.
     */
    public Optional<String> findClientAdminId() {
        try {
            Optional<String> header = getHeaderValue(HEADER_CURRENT_USER_CONTEXT);
            if (header.isEmpty()) {
                return Optional.empty();
            }
            String jsonObject = SerializationUtils.toByteArrayToString(header.get());
            CurrentUserContext context = toObject(jsonObject, CurrentUserContext.class);
            if (context == null || context.getClientAdminId() == null || context.getClientAdminId().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(context.getClientAdminId().trim());
        } catch (Exception e) {
            log.debug("No client admin context available: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
