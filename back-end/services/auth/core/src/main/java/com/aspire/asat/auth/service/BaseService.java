package com.aspire.asat.auth.service;

import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.exception.ResourceNotFoundException;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.logger.ServiceLogger;
import com.aspire.asat.auth.util.IPUtils;
import com.aspire.asat.auth.util.SerializationUtils;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class BaseService {

    protected ObjectMapper objectMapper;
    protected ServiceLogger logger;
    protected LocaleMessageService messageService;
    protected HttpServletRequest httpServletRequest;

    public static final String HEADER_CURRENT_USER_CONTEXT = "CurrentContext";


    @Autowired
    public void setLogger(ServiceLogger logger) {
        this.logger = logger;
    }

    @Autowired
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    @Lazy
    @Autowired
    public void setMessageService(LocaleMessageService messageService) {
        this.messageService = messageService;
    }

    @Lazy
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getMessage(String key) {
        return messageService.getLocalMessage(key);
    }

    public String getMessage(ResponseMessage key) {
        return messageService.getLocalMessage(key);
    }

    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public Optional<String> getHeaderValue(String headerName) {
        try {
            return Optional.ofNullable(httpServletRequest.getHeader(headerName));
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage(), ex);
        }

        return Optional.empty();
    }


    public String getRemoteIPAddress() {
        try {
            String realIp = IPUtils.getClientRealIpAddress(httpServletRequest);
            if (io.micrometer.common.util.StringUtils.isNotBlank(realIp)) {
                return realIp;
            } else {
                return httpServletRequest.getRemoteAddr();
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public <T> T toObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public <T> void printTrace(T obj) {
        logger.trace(writeJsonString(obj));
    }

    public <T> byte[] writeJsonByte(T obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(obj);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return new byte[]{};
    }

    public <T> String writeJsonString(T obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return StringUtils.EMPTY;
    }

    public static long getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.getTime();
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public String getCurrentUserContextHeaderValue() {
        Optional<String> userTokenOpt = getHeaderValue(HEADER_CURRENT_USER_CONTEXT);
        if (userTokenOpt.isEmpty()) {
            throw new UnauthorizedResourceException(ResponseMessage.UNAUTHORIZED_RESOURCE_ACCESS.getResponseMessage());
        }
        return userTokenOpt.get();
    }


    public CurrentUserContext getCurrentUserContext() {
        String base64Data = getCurrentUserContextHeaderValue();
        String jsonObject = SerializationUtils.toByteArrayToString(base64Data);
        try {
            return toObject(jsonObject, CurrentUserContext.class);
        } catch (Exception e) {
            logger.error("Error while parsing CurrentUserContext: {}", e.getMessage());
            throw new ResourceNotFoundException(ResponseMessage.UNAUTHORIZED_RESOURCE_ACCESS.getResponseMessage());
        }
    }

}
