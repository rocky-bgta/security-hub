package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.dto.request.AdminDomainAddRequest;
import com.aspire.asat.phishing.dto.request.DomainVerificationRequest;
import com.aspire.asat.phishing.dto.request.GenerateVerificationRequest;
import com.aspire.asat.phishing.dto.response.DomainDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.phishing.mapper.DomainMapper;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.model.DomainVerificationCode;
import com.aspire.asat.phishing.repository.DomainRepository;
import com.aspire.asat.phishing.repository.DomainVerificationCodeRepository;
import com.aspire.asat.phishing.service.DomainService;
import com.aspire.asat.phishing.client.PhishingNotificationClient;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of DomainService.
 * Handles domain verification and locking operations.
 * Based on BRD Use Case 2.1.1.1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DomainServiceImpl implements DomainService {
    
    private final DomainRepository domainRepository;
    private final DomainVerificationCodeRepository verificationCodeRepository;
    private final DomainMapper domainMapper;
    private final UserCurrentContextService userCurrentContextService;
    private final PhishingNotificationClient phishingNotificationClient;
    
    // Configuration constants from BRD
    private static final int CODE_EXPIRY_MINUTES = 15;          // BR-06
    private static final int MAX_REQUESTS_PER_HOUR = 5;         // BR-07
    private static final int VERIFICATION_CODE_LENGTH = 6;
    
    // Subscription tiers that allow domain locking (BR-02)
    private static final List<String> LOCKABLE_TIERS = List.of("PROFESSIONAL", "ENTERPRISE");
    private static final String ROLE_CLIENT_ADMIN = UserType.CLIENT_ADMIN.getValue();
    
    @Override
    public List<DomainDto> getDomains(String search, List<DomainStatus> status, int offset, int pageSize,
                                       String sortBy, String sortDirection) {
        String clientId = getCurrentClientId();
        boolean canLock = canLockDomains();
        List<DomainStatus> statuses = normalizeStatuses(status);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(offset, pageSize, sort);
        
        Page<Domain> domains;
        List<Domain> domainList = domainRepository.findWithFilters(clientId, search, statuses, pageable);
        domains = new org.springframework.data.domain.PageImpl<>(domainList, pageable, countDomains(search, status));
        
        return domains.getContent().stream()
                .map(domain -> domainMapper.toDto(domain, canLock))
                .collect(Collectors.toList());
    }
    
    @Override
    public long countDomains(String search, List<DomainStatus> status) {
        String clientId = getCurrentClientId();
        
        return domainRepository.countWithFilters(clientId, search, normalizeStatuses(status));
    }
    
    @Override
    public Optional<DomainDto> getDomainById(String domainId) {
        String clientId = getCurrentClientId();
        boolean canLock = canLockDomains();
        
        return domainRepository.findByIdAndClientIdOrGlobal(domainId, clientId)
                .map(domain -> domainMapper.toDto(domain, canLock));
    }
    
    @Override
    @Transactional
    public void generateVerificationEmail(GenerateVerificationRequest request) {
        String email = request.getEmailAddress().toLowerCase().trim();
        String domainName = extractDomain(email);
        String clientId = getCurrentClientId();
        
        log.info("Generating verification email for domain {} to {}", domainName, email);
        
        // Check rate limit (BR-07: max 5 per hour)
        long recentRequests = verificationCodeRepository.countByDomainAndCreatedAtAfter(
                domainName, Instant.now().minus(1, ChronoUnit.HOURS));
        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            throw new ServiceException("Too many verification attempts. Please try again later");
        }
        
        // Check if domain is locked by another tenant
        if (domainRepository.existsByDomainAndIsLockedTrueAndClientIdNot(domainName, clientId)) {
            throw new ServiceException(MessageKeys.DOMAIN_LOCKED_ERROR);
        }

        ensureDomainNotAlreadyVerified(domainName);
        
        // Generate 6-digit verification code
        String code = generateVerificationCode();
        
        // Create and save verification code entity
        DomainVerificationCode verificationCode = DomainVerificationCode.builder()
                .clientId(clientId)
                .domain(domainName)
                .emailAddress(email)
                .verificationCode(code) // TODO: Hash the code before storing
                .expiresAt(Instant.now().plus(CODE_EXPIRY_MINUTES, ChronoUnit.MINUTES))
                .isUsed(false)
                .build();
        
        verificationCodeRepository.save(verificationCode);
        
        phishingNotificationClient.sendDomainVerificationEmail(email, code, domainName);
        
        log.info("Verification code generated and email sent for domain {}", domainName);
    }
    
    @Override
    @Transactional
    public DomainDto verifyDomain(DomainVerificationRequest request) {
        String email = request.getEmailAddress().toLowerCase().trim();
        String domainName = extractDomain(email);
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        UserType userType = getCurrentUserType();
        
        log.info("Verifying domain {} with code", domainName);
        
        // Find valid verification code
        DomainVerificationCode verificationCode = verificationCodeRepository
                .findByDomainAndEmailAddressAndIsUsedFalseAndExpiresAtAfter(
                        domainName, email, Instant.now())
                .orElseThrow(() -> new ServiceException("Invalid or expired verification code. Please request a new one"));
        
        // Verify the code matches
        if (!request.getVerificationCode().equals(verificationCode.getVerificationCode())) {
            throw new ServiceException("Invalid verification code");
        }

        ensureDomainNotAlreadyVerified(domainName);
        
        // Mark code as used
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);
        
        // Create or update domain record
        Domain domain = domainRepository.findByDomainAndClientId(domainName, clientId)
                .orElse(Domain.builder()
                        .clientId(clientId)
                        .domain(domainName)
                        .createdBy(userId)
                        .createdByRole(userType != null ? userType.getValue() : "SYSTEM_USER")
                        .isGlobal(isGlobalDomain(userType))
                        .isLocked(false)
                        .build());
        
        domain.setStatus(DomainStatus.VERIFIED);
        domain.setVerifiedBy(userId);
        domain.setVerifiedAt(Instant.now());
        
        Domain savedDomain = domainRepository.save(domain);
        
        log.info("Domain {} verified successfully by user {}", domainName, userId);
        
        return domainMapper.toDto(savedDomain, canLockDomains());
    }

    @Override
    @Transactional
    public DomainDto adminAddVerifiedDomain(AdminDomainAddRequest request) {
        UserType userType = getCurrentUserType();
        if (userType != UserType.SUPER_ADMIN
                && userType != UserType.ASPIRE_ADMIN
                && userType != UserType.SYSTEM_USER) {
            throw new ServiceException("You do not have permission to add domains this way");
        }

        String domainName = normalizeDomainName(request.getDomain());
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        Instant now = Instant.now();

        Optional<Domain> existingDomain = domainRepository.findByDomain(domainName);
        if (existingDomain.isPresent()) {
            Domain existing = existingDomain.get();
            if (isVerifiedStatus(existing.getStatus())) {
                throw new ServiceException("Domain already exists");
            }
            Domain updated = applyAdminVerifiedUpdate(existing, userId, now);
            Domain saved = domainRepository.save(updated);
            log.info("Admin updated existing unverified domain {} to VERIFIED (id={})", domainName, saved.getId());
            return domainMapper.toDto(saved, canLockDomains());
        }

        Domain domain = Domain.builder()
                .clientId(clientId)
                .domain(domainName)
                .createdBy(userId)
                .createdByRole(userType != null ? userType.getValue() : "SYSTEM_USER")
                .isGlobal(isGlobalDomain(userType))
                .isLocked(false)
                .status(DomainStatus.VERIFIED)
                .verifiedBy(userId)
                .verifiedAt(now)
                .build();

        Domain saved = domainRepository.save(domain);
        log.info("Admin registered domain {} as VERIFIED (id={})", domainName, saved.getId());
        return domainMapper.toDto(saved, canLockDomains());
    }

    private boolean isVerifiedStatus(DomainStatus status) {
        return status == DomainStatus.VERIFIED || status == DomainStatus.VERIFIED_AND_LOCKED;
    }

    private void ensureDomainNotAlreadyVerified(String domainName) {
        domainRepository.findByDomain(domainName)
                .filter(domain -> isVerifiedStatus(domain.getStatus()))
                .ifPresent(domain -> {
                    throw new ServiceException("This domain is already verified", HttpStatus.CONFLICT);
                });
    }

    private Domain applyAdminVerifiedUpdate(Domain existing, String userId, Instant now) {
        existing.setStatus(DomainStatus.VERIFIED);
        existing.setVerifiedBy(userId);
        existing.setVerifiedAt(now);
        if (existing.isLocked()) {
            existing.setLocked(false);
            existing.setLockedByTenantId(null);
            existing.setLockedBy(null);
            existing.setLockedAt(null);
        }
        return existing;
    }

    /**
     * Normalize admin input: trim, lowercase, and extract host from an email if an {@code @} is present.
     */
    private String normalizeDomainName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ServiceException("Domain is required");
        }
        String trimmed = raw.trim().toLowerCase();
        if (trimmed.contains("@")) {
            return extractDomain(trimmed);
        }
        return trimmed;
    }
    
    @Override
    @Transactional
    public DomainDto lockDomain(String domainId) {
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        
        // Check if tenant can lock domains (BR-02)
        if (!canLockDomains()) {
            throw new ServiceException(
                    "Domain locking is available only for Professional or Enterprise plans",
                    HttpStatus.FORBIDDEN);
        }
        
        Domain domain = domainRepository.findById(domainId)
                .filter(d -> d.getClientId().equals(clientId))
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found"));
        
        // Can only lock verified domains
        if (domain.getStatus() != DomainStatus.VERIFIED) {
            throw new ServiceException("Domain must be verified before locking");
        }
        
        domain.setStatus(DomainStatus.VERIFIED_AND_LOCKED);
        domain.setLocked(true);
        domain.setLockedByTenantId(clientId);
        domain.setLockedBy(userId);
        domain.setLockedAt(Instant.now());
        
        Domain savedDomain = domainRepository.save(domain);
        
        log.info("Domain {} locked by user {}", domain.getDomain(), userId);
        
        return domainMapper.toDto(savedDomain, true);
    }
    
    @Override
    @Transactional
    public DomainDto unlockDomain(String domainId) {
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        
        Domain domain = domainRepository.findById(domainId)
                .filter(d -> d.getClientId().equals(clientId))
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found"));
        
        // Can only unlock locked domains
        if (!domain.isLocked()) {
            throw new ServiceException("Domain is not locked");
        }
        
        domain.setStatus(DomainStatus.VERIFIED);
        domain.setLocked(false);
        domain.setLockedByTenantId(null);
        
        Domain savedDomain = domainRepository.save(domain);
        
        log.info("Domain {} unlocked by user {}", domain.getDomain(), userId);
        
        return domainMapper.toDto(savedDomain, canLockDomains());
    }
    
    @Override
    @Transactional
    public void deleteDomain(String domainId) {
        String clientId = getCurrentClientId();
        String userId = getCurrentUserId();
        UserType currentUserType = getCurrentUserType();
        
        Domain domain = domainRepository.findByIdAndClientIdOrGlobal(domainId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found"));
        
        // Check if user can delete this domain (BR-08)
        if (!canDelete(domain, currentUserType)) {
            throw new ServiceException(
                    "You do not have permission to delete this domain",
                    HttpStatus.FORBIDDEN);
        }
        
        domainRepository.delete(domain);
        
        log.info("Domain {} deleted by user {}", domain.getDomain(), userId);
    }
    
    @Override
    public void resendVerificationEmail(GenerateVerificationRequest request) {
        // Reuse the same logic as generateVerificationEmail
        generateVerificationEmail(request);
    }
    
    @Override
    public boolean canLockDomains() {
        try {
            // TODO: Integrate with subscription/tier service to check if tenant has Professional/Enterprise tier
            // For now, returning true to allow testing. 
            // In production, call RegistrationService or TierConfigurationService to get subscription tier
            // Example:
            // CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
            // TierConfiguration tier = tierClient.getTierByClientId(context.getClientAdminId());
            // return tier != null && LOCKABLE_TIERS.contains(tier.getName().toUpperCase());
            
            log.debug("Checking if tenant can lock domains - defaulting to true for development");
            return true;
        } catch (Exception e) {
            log.warn("Could not determine subscription tier: {}", e.getMessage());
            return false;
        }
    }
    
    // --- Helper methods ---
    
    private List<DomainStatus> normalizeStatuses(List<DomainStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Extract domain name from email address
     * e.g., "admin@aspiretech.com" -> "aspiretech.com"
     */
    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            throw new ServiceException("Invalid email address format");
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }
    
    /**
     * Generate a secure random verification code
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt((int) Math.pow(10, VERIFICATION_CODE_LENGTH));
        return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", code);
    }
    
    /**
     * Get current client ID from user context
     */
    private String getCurrentClientId() {
        return userCurrentContextService.getCurrentUserContext().getClientAdminId();
    }
    
    /**
     * Get current user ID from user context
     */
    private String getCurrentUserId() {
        return userCurrentContextService.getCurrentUserContext().getUserId();
    }

    private UserType getCurrentUserType() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private boolean isGlobalDomain(UserType userType) {
        return userType == UserType.SUPER_ADMIN
                || userType == UserType.ASPIRE_ADMIN
                || userType == UserType.SYSTEM_USER;
    }

    private boolean canDelete(Domain domain, UserType currentUserType) {
        if (currentUserType == null) {
            return false;
        }
        if (currentUserType == UserType.SUPER_ADMIN
                || currentUserType == UserType.ASPIRE_ADMIN
                || currentUserType == UserType.SYSTEM_USER) {
            return true;
        }
        if (currentUserType == UserType.CLIENT_ADMIN) {
            return !domain.isGlobal()
                    && ROLE_CLIENT_ADMIN.equalsIgnoreCase(domain.getCreatedByRole())
                    && getCurrentUserId().equals(domain.getCreatedBy());
        }
        return false;
    }
}
