package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.phishing.controller.DomainController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.dto.request.AdminDomainAddRequest;
import com.aspire.asat.phishing.dto.request.DomainVerificationRequest;
import com.aspire.asat.phishing.dto.request.GenerateVerificationRequest;
import com.aspire.asat.phishing.dto.response.DomainDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.DomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of DomainController.
 * Handles HTTP requests for domain verification and locking.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class DomainControllerImpl implements DomainController {
    
    private final DomainService domainService;
    private final MessageService messageService;
    private final ToastMessageResolver toastMessageResolver;
    
    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<DomainDto>>>> getDomains(
            String search, List<DomainStatus> status, int offset, int pageSize, String sortBy, String sortDirection) {
        try {
            log.info("Getting domains with search: {}, status: {}, offset: {}, pageSize: {}",
                    search, status, offset, pageSize);
            
            List<DomainDto> domains = domainService.getDomains(search, status, offset, pageSize, sortBy, sortDirection);
            long total = domainService.countDomains(search, status);
            
            AllResponseDto<List<DomainDto>> response = new AllResponseDto<>(offset, pageSize, total, domains);
            return ResponseEntity.ok(new ApiResponseDto<>("Domains retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting domains: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve domains", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<DomainDto>> getDomainById(String domainId) {
        try {
            log.info("Getting domain by ID: {}", domainId);
            
            return domainService.getDomainById(domainId)
                    .map(domain -> ResponseEntity.ok(new ApiResponseDto<>("Domain retrieved successfully", 200, domain)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>("Domain not found", 404, null)));
        } catch (Exception e) {
            log.error("Error getting domain by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve domain", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<String>> generateVerificationEmail(GenerateVerificationRequest request) {
        try {
            log.info("Generating verification email for: {}", request.getEmailAddress());
            
            domainService.generateVerificationEmail(request);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    messageService.get(MessageKeys.VERIFICATION_EMAIL_SENT), 200,
                    "Please check your email for the verification code"));
        } catch (ServiceException e) {
            log.warn("Service error generating verification email: {}", e.getMessage());
            
            // Handle rate limiting
            if (e.getMessage().contains("Too many")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponseDto<>(e.getMessage(), 429, null));
            }
            // Handle locked domain
            if (MessageKeys.DOMAIN_LOCKED_ERROR.equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(toastMessageResolver.resolve(MessageKeys.DOMAIN_LOCKED_ERROR), 409, null));
            }
            HttpStatus status = e.getStatus() != null ? e.getStatus() : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(new ApiResponseDto<>(e.getMessage(), status.value(), null));
        } catch (Exception e) {
            log.error("Error generating verification email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to send verification email", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<DomainDto>> verifyDomain(DomainVerificationRequest request) {
        try {
            log.info("Verifying domain for email: {}", request.getEmailAddress());
            
            DomainDto domain = domainService.verifyDomain(request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.DOMAIN_VERIFIED), 200, domain));
        } catch (ServiceException e) {
            log.warn("Service error verifying domain: {}", e.getMessage());
            HttpStatus status = e.getStatus() != null ? e.getStatus() : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(new ApiResponseDto<>(e.getMessage(), status.value(), null));
        } catch (Exception e) {
            log.error("Error verifying domain: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to verify domain", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DomainDto>> adminAddVerifiedDomain(AdminDomainAddRequest request) {
        try {
            log.info("Admin add verified domain: {}", request.getDomain());
            DomainDto domain = domainService.adminAddVerifiedDomain(request);
            return ResponseEntity.ok(new ApiResponseDto<>("Domain registered as verified", 200, domain));
        } catch (ServiceException e) {
            log.warn("Service error admin add domain: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(e.getMessage(), 403, null));
            }
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponseDto<>(e.getMessage(), 409, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error admin add domain: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to register domain", 500, null));
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<DomainDto>> lockDomain(String domainId) {
        try {
            log.info("Locking domain: {}", domainId);
            DomainDto domain = domainService.lockDomain(domainId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.DOMAIN_LOCKED), 200, domain));
        } catch (RuntimeException e) {
            log.warn("Error locking domain {}: {}", domainId, e.getMessage());
            return domainErrorResponse(e);
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<DomainDto>> unlockDomain(String domainId) {
        try {
            log.info("Unlocking domain: {}", domainId);
            DomainDto domain = domainService.unlockDomain(domainId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.DOMAIN_UNLOCKED), 200, domain));
        } catch (RuntimeException e) {
            log.warn("Error unlocking domain {}: {}", domainId, e.getMessage());
            return domainErrorResponse(e);
        }
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteDomain(String domainId) {
        try {
            log.info("Deleting domain: {}", domainId);
            domainService.deleteDomain(domainId);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.DOMAIN_DELETED), 200, null));
        } catch (RuntimeException e) {
            log.warn("Error deleting domain {}: {}", domainId, e.getMessage());
            return domainErrorResponse(e);
        }
    }

    private static <T> ResponseEntity<ApiResponseDto<T>> domainErrorResponse(RuntimeException e) {
        HttpStatus status = domainErrorStatus(e);
        String message = (e.getMessage() == null || e.getMessage().isBlank())
                ? "Domain request could not be processed"
                : e.getMessage();
        return ResponseEntity.status(status)
                .body(new ApiResponseDto<>(message, status.value(), null));
    }

    private static HttpStatus domainErrorStatus(RuntimeException e) {
        if (e instanceof ServiceException se && se.getStatus() != null) {
            return se.getStatus();
        }
        if (e instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }
    
    @Override
    public ResponseEntity<ApiResponseDto<String>> resendVerificationEmail(GenerateVerificationRequest request) {
        try {
            log.info("Resending verification email for: {}", request.getEmailAddress());
            
            domainService.resendVerificationEmail(request);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Verification email resent successfully", 200, 
                    "Please check your email for the verification code"));
        } catch (ServiceException e) {
            log.warn("Service error resending verification email: {}", e.getMessage());
            
            if (e.getMessage().contains("Too many")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponseDto<>(e.getMessage(), 429, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error resending verification email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to resend verification email", 500, null));
        }
    }
}
