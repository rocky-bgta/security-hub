package com.aspire.asat.auth.controller.base;


import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.service.LocaleMessageService;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.service.files.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;

import java.util.Map;

public class BaseController {

    protected LocaleMessageService localeMessageService;
    protected MessageService messageService;
    protected FileService fileService;
    protected FileProps fileProps;

    @Autowired
    public void setLocaleMessageService(LocaleMessageService localeMessageService) {
        this.localeMessageService = localeMessageService;
    }

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Autowired(required = false)
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired(required = false)
    public void setFileProps(FileProps fileProps) {
        this.fileProps = fileProps;
    }

    public String getMessage(ResponseMessage key) {
        if (messageService != null && key == ResponseMessage.OPERATION_SUCCESSFUL) {
            return messageService.get(MessageKeys.GENERAL_OPERATION_COMPLETED);
        }
        return localeMessageService.getLocalMessage(key);
    }

    /**
     * Set signed cookies for CloudFront access
     * @param response HTTP response
     */
    protected void setSignedCookies(HttpServletResponse response) {
        if (fileService == null || fileProps == null) {
            // Log warning if dependencies are not available (but don't throw exception to avoid breaking the flow)
            return;
        }
        try {
            Map<String, String> cookies = fileService.generateSignedCookies();
            if (cookies == null || cookies.isEmpty()) {
                return; // No cookies to set
            }
            String cookieDomain = fileProps.getAws().getCloudFront().getDistributionMainDomain();
            if (cookieDomain == null || cookieDomain.trim().isEmpty()) {
                return; // Invalid domain configuration
            }
            cookies.forEach((name, value) -> {
                if (name != null && value != null) {
                    ResponseCookie cookie = ResponseCookie.from(name, value)
                            .domain(cookieDomain)
                            .path("/")
                            .secure(true)
                            .httpOnly(true)
                            .sameSite("None")
                            .maxAge(24 * 60 * 60)
                            .build();
                    response.addHeader("Set-Cookie", cookie.toString());
                }
            });
        } catch (Exception e) {
            // Log error but don't throw exception to avoid breaking the authentication flow
            // Cookies are not critical for authentication, they're for CloudFront access
        }
    }

    /**
     * Clear signed cookies
     * @param response HTTP response
     */
    protected void clearSignedCookies(HttpServletResponse response) {
        if (fileProps == null) {
            return; // Skip if dependencies not available
        }
        try {
            String[] cookieNames = {"CloudFront-Policy", "CloudFront-Signature", "CloudFront-Key-Pair-Id"};
            String cookieDomain = fileProps.getAws().getCloudFront().getDistributionMainDomain();
            if (cookieDomain == null || cookieDomain.trim().isEmpty()) {
                return; // Invalid domain configuration
            }
            for (String cookieName : cookieNames) {
                ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                        .domain(cookieDomain)
                        .path("/")
                        .secure(true)
                        .httpOnly(true)
                        .sameSite("None")
                        .maxAge(0)
                        .build();
                response.addHeader("Set-Cookie", cookie.toString());
            }
        } catch (Exception e) {
            // Log error but don't throw exception to avoid breaking the logout flow
        }
    }
}
