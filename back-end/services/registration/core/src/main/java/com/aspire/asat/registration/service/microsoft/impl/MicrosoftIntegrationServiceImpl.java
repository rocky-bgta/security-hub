package com.aspire.asat.registration.service.microsoft.impl;

import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.config.MicrosoftEntraConfig;
import com.aspire.asat.registration.data.phishing.request.UserRiskProfileSaveRequestDto;
import com.aspire.asat.registration.data.enums.UserSource;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.microsoft.*;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.*;
import com.aspire.asat.registration.repository.*;
import com.aspire.asat.registration.service.microsoft.MicrosoftGraphService;
import com.aspire.asat.registration.service.microsoft.MicrosoftIntegrationService;
import com.aspire.asat.registration.service.microsoft.MicrosoftTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MicrosoftIntegrationServiceImpl implements MicrosoftIntegrationService {

    private final MicrosoftEntraConfig entraConfig;
    private final MicrosoftIntegrationRepository integrationRepository;
    private final UserImportJobRepository importJobRepository;
    private final MicrosoftGraphService graphService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MicrosoftTokenService tokenService;
    private final AspireUserRepository aspireUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationNotificationClient registrationNotificationClient;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PhishingServiceClient phishingServiceClient;

    @Override
    public String getAuthorizationUrl(String clientAdminId) {
        log.info("Generating authorization URL for clientAdminId: {}", clientAdminId);

        // State contains clientAdminId for callback
        String state = Base64.getEncoder().encodeToString(
                clientAdminId.getBytes(StandardCharsets.UTF_8));

        // Use 'common' or 'organizations' for multi-tenant
        String authUrl = String.format(
                "https://login.microsoftonline.com/%s/oauth2/v2.0/authorize" +
                        "?client_id=%s" +
                        "&response_type=code" +
                        "&redirect_uri=%s" +
                        "&scope=%s" +
                        "&state=%s" +
                        "&response_mode=query" +
                        "&prompt=consent",  // Force consent for multi-tenant
                entraConfig.getTenantId(),  // Should be 'common' or 'organizations'
                entraConfig.getClientId(),
                URLEncoder.encode(entraConfig.getRedirectUri(), StandardCharsets.UTF_8),
                URLEncoder.encode(entraConfig.getScopes(), StandardCharsets.UTF_8),
                state
        );

        return authUrl;
    }


    @Override
    public ConnectionStatusDto handleCallback(String code, String state) {
        // Decode clientAdminId from state
        String clientAdminId = new String(
                Base64.getDecoder().decode(state), StandardCharsets.UTF_8);

        log.info("Handling callback for clientAdminId: {}", clientAdminId);

        // Exchange code for tokens using customer's tenant
        MicrosoftTokenResponseDto tokenResponse = tokenService.exchangeCodeForTokens(code);

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        Integer expiresIn = tokenResponse.getExpiresIn();

        // Extract customer's tenant ID from the token (optional but useful)
        String customerTenantId = extractTenantIdFromToken(accessToken);

        Instant now = Instant.now();

        // Save integration with customer's tenant info
        MicrosoftIntegration integration = integrationRepository
                .findByClientAdminId(clientAdminId)
                .orElse(MicrosoftIntegration.builder()
                        .clientAdminId(clientAdminId)
                        .createdAt(now)
                        .build());

        integration.setTenantId(customerTenantId);  // Store customer's tenant ID
        integration.setAccessToken(accessToken);
        integration.setRefreshToken(refreshToken);
        integration.setTokenExpiresAt(now.plusSeconds(expiresIn));
        integration.setActive(true);
        integration.setConnectedAt(now);
        integration.setUpdatedAt(now);

        integrationRepository.save(integration);

        log.info("Microsoft Entra connected for clientAdminId: {} with tenant: {}",
                clientAdminId, customerTenantId);

        return ConnectionStatusDto.builder()
                .connected(true)
                .tenantId(customerTenantId)
                .connectedAt(now)
                .build();
    }

    private String extractTenantIdFromToken(String accessToken) {
        try {
            // Decode JWT payload (middle part)
            String[] parts = accessToken.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                // Parse JSON to get tid (tenant ID)
                // Simple extraction - consider using a JSON library
                int tidStart = payload.indexOf("\"tid\":\"") + 7;
                int tidEnd = payload.indexOf("\"", tidStart);
                return payload.substring(tidStart, tidEnd);
            }
        } catch (Exception e) {
            log.warn("Could not extract tenant ID from token", e);
        }
        return "unknown";
    }

    @Override
    public ConnectionStatusDto getConnectionStatus(String clientAdminId) {
        return integrationRepository
                .findByClientAdminIdAndActive(clientAdminId, true)
                .map(integration -> ConnectionStatusDto.builder()
                        .connected(true)
                        .tenantId(integration.getTenantId())
                        .connectedAt(integration.getConnectedAt())
                        .lastSyncAt(integration.getLastSyncAt())
                        .build())
                .orElse(ConnectionStatusDto.builder()
                        .connected(false)
                        .build());
    }

    @Override
    public void disconnect(String clientAdminId) {
        MicrosoftIntegration integration = integrationRepository
                .findByClientAdminIdAndActive(clientAdminId, true)
                .orElseThrow(() -> new ResourceNotFoundException("No active integration found"));

        integration.setActive(false);
        integration.setUpdatedAt(Instant.now());
        integrationRepository.save(integration);

        log.info("Disconnected Microsoft Entra for clientAdminId: {}", clientAdminId);
    }

    @Override
    public List<MicrosoftGroupDto> getGroups(String clientAdminId) {
        String accessToken = getValidAccessToken(clientAdminId);
        return graphService.getAllGroups(accessToken);
    }

    @Override
    public List<MicrosoftUserDto> getGroupMembers(String clientAdminId, String groupId) {
        String accessToken = getValidAccessToken(clientAdminId);
        return graphService.getGroupMembers(groupId, accessToken);
    }

    @Override
    public ImportResultDto importUsers(String clientAdminId, ImportRequestDto request) {
        validateConnection(clientAdminId);

        Instant now = Instant.now();

        // Create job
        UserImportJob job = UserImportJob.builder()
                .clientAdminId(clientAdminId)
                .status("IN_PROGRESS")
                .groupIds(request.getGroupIds())
                .startedAt(now)
                .createdAt(now)
                .build();

        job = importJobRepository.save(job);

        // Process import
        processImportAsync(job.getId(), clientAdminId, request);

        return mapToImportResultDto(job);
    }

    @Async
    protected void processImportAsync(String jobId, String clientAdminId, ImportRequestDto request) {
        UserImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // Fetch USER role from repository
        Role userRole = roleRepository.findByRoleName("USER");
        if (userRole == null) {
            throw new ResourceNotFoundException("Role 'USER' not found in the system");
        }
        String userRoleId = userRole.getId();
        log.debug("Found USER role with ID: {}", userRoleId);

        String accessToken = getValidAccessToken(clientAdminId);
        Set<String> processedEmails = new HashSet<>();
        int usersCreated = 0;
        int usersUpdated = 0;
        int usersSkipped = 0;
        int usersFailed = 0;

        try {
            for (String groupId : request.getGroupIds()) {
                log.info("Fetching users from group: {}", groupId);
                List<MicrosoftUserDto> members = graphService.getGroupMembers(groupId, accessToken);

                for (MicrosoftUserDto msUser : members) {
                    // Skip if email is null or already processed
                    if (msUser.getEmail() == null || msUser.getEmail().isBlank()) {
                        log.warn("Skipping user with null or blank email from group: {}", groupId);
                        usersSkipped++;
                        continue;
                    }
                    
                    String email = msUser.getEmail().trim().toLowerCase();
                    if (processedEmails.contains(email)) {
                        log.debug("Skipping duplicate email: {}", email);
                        usersSkipped++;
                        continue;
                    }
                    processedEmails.add(email);

                    try {
                        // Check if user already exists
                        Optional<AspireUser> existingUserOpt = aspireUserRepository.findByEmailIgnoreCase(email);
                        if (existingUserOpt.isPresent()) {
                            // Update existing user with name, department, and designation
                            AspireUser existingUser = existingUserOpt.get();
                            updateExistingAspireUser(existingUser, msUser);
                            log.info("Updated existing AspireUser with email: {}", email);
                            usersUpdated++;
                            continue;
                        }
                        
                        // Generate temporary password
                        String tempPassword = generateTemporaryPassword();
                        
                        // Create and save AspireUser
                        UserCreationResult result = createAndSaveAspireUser(
                                msUser, email, clientAdminId, userRoleId, tempPassword);
                        UUID userId = result.userId();
                        
                        log.info("Created AspireUser for email: {} with userId: {}", email, userId);
                        
                        // Send welcome email notification
                        String userName = buildFullName(msUser.getGivenName(), msUser.getSurname());
                        if (userName == null || userName.isBlank()) {
                            userName = msUser.getDisplayName() != null ? msUser.getDisplayName() : email;
                        }
                        
                        registrationNotificationClient.sendWelcomeEmailNotification(
                                email,
                                userId.toString(),
                                clientAdminId,
                                userName,
                                result.tempPassword()
                        );
                        
                        usersCreated++;
                    } catch (Exception e) {
                        log.error("Error processing user: {}", email, e);
                        usersFailed++;
                    }
                }
            }

            job.setStatus("COMPLETED");

        } catch (Exception e) {
            log.error("Import job failed: {}", jobId, e);
            job.setStatus("FAILED");
        }

        job.setTotalUsersProcessed(processedEmails.size());
        job.setUsersCreated(usersCreated);
        job.setUsersUpdated(usersUpdated);
        job.setUsersSkipped(usersSkipped);
        job.setUsersFailed(usersFailed);
        job.setCompletedAt(Instant.now());

        importJobRepository.save(job);
    }

    @Override
    public ImportResultDto getImportJobStatus(String jobId) {
        UserImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return mapToImportResultDto(job);
    }

    @Override
    public List<ImportResultDto> getImportHistory(String clientAdminId) {
        return importJobRepository
                .findByClientAdminIdOrderByCreatedAtDesc(clientAdminId)
                .stream()
                .map(this::mapToImportResultDto)
                .collect(Collectors.toList());
    }

    private void validateConnection(String clientAdminId) {
        if (!integrationRepository.existsByClientAdminIdAndActive(clientAdminId, true)) {
            throw new IllegalStateException("Microsoft Entra is not connected");
        }
    }

    private String getValidAccessToken(String clientAdminId) {
        MicrosoftIntegration integration = integrationRepository
                .findByClientAdminIdAndActive(clientAdminId, true)
                .orElseThrow(() -> new IllegalStateException("Microsoft Entra is not connected"));

        // Check if token is expired or about to expire (within 5 minutes)
        if (integration.getTokenExpiresAt() != null &&
                integration.getTokenExpiresAt().isBefore(Instant.now().plusSeconds(300))) {

            log.info("Access token expired or expiring soon, refreshing for clientAdminId: {}", clientAdminId);

            try {
                MicrosoftTokenResponseDto newTokens = tokenService.refreshAccessToken(integration.getRefreshToken());

                integration.setAccessToken(newTokens.getAccessToken());
                if (newTokens.getRefreshToken() != null) {
                    integration.setRefreshToken(newTokens.getRefreshToken());
                }
                integration.setTokenExpiresAt(Instant.now().plusSeconds(newTokens.getExpiresIn()));
                integration.setUpdatedAt(Instant.now());

                integrationRepository.save(integration);

                log.info("Token refreshed successfully for clientAdminId: {}", clientAdminId);

            } catch (Exception e) {
                log.error("Failed to refresh token for clientAdminId: {}", clientAdminId, e);
                throw new IllegalStateException("Token expired and refresh failed. Please reconnect.");
            }
        }

        return integration.getAccessToken();
    }

    @Override
    public ImportUsersResponseDto importUsersFromGroups(String clientAdminId, ImportUsersRequestDto request) {
        log.info("Importing users from groups for clientAdminId: {}", clientAdminId);
        
        validateConnection(clientAdminId);
        
        // Fetch USER role from repository
        Role userRole = roleRepository.findByRoleName("USER");
        if (userRole == null) {
            throw new ResourceNotFoundException("Role 'USER' not found in the system");
        }
        String userRoleId = userRole.getId();
        log.debug("Found USER role with ID: {}", userRoleId);
        
        String accessToken = getValidAccessToken(clientAdminId);
        Set<String> processedEmails = new HashSet<>();
        int usersCreated = 0;
        int usersUpdated = 0;
        int usersSkipped = 0;
        int usersFailed = 0;
        
        try {
            for (String groupId : request.getGroupIds()) {
                log.info("Fetching users from group: {}", groupId);
                List<MicrosoftUserDto> members = graphService.getGroupMembers(groupId, accessToken);
                
                for (MicrosoftUserDto msUser : members) {
                    // Skip if email is null or already processed
                    if (msUser.getEmail() == null || msUser.getEmail().isBlank()) {
                        log.warn("Skipping user with null or blank email from group: {}", groupId);
                        usersSkipped++;
                        continue;
                    }
                    
                    String email = msUser.getEmail().trim().toLowerCase();
                    if (processedEmails.contains(email)) {
                        log.debug("Skipping duplicate email: {}", email);
                        usersSkipped++;
                        continue;
                    }
                    processedEmails.add(email);
                    
                    try {
                        // Check if user already exists
                        Optional<AspireUser> existingUserOpt = aspireUserRepository.findByEmailIgnoreCase(email);
                        if (existingUserOpt.isPresent()) {
                            // Update existing user with name, department, and designation
                            AspireUser existingUser = existingUserOpt.get();
                            updateExistingAspireUser(existingUser, msUser);
                            log.info("Updated existing AspireUser with email: {}", email);
                            usersUpdated++;
                            continue;
                        }
                        
                        // Generate temporary password
                        String tempPassword = generateTemporaryPassword();
                        
                        // Create and save AspireUser
                        UserCreationResult result = createAndSaveAspireUser(
                                msUser, email, clientAdminId, userRoleId, tempPassword);
                        UUID userId = result.userId();
                        
                        log.info("Created AspireUser for email: {} with userId: {}", email, userId);
                        
                        // Send welcome email notification
                        String userName = buildFullName(msUser.getGivenName(), msUser.getSurname());
                        if (userName == null || userName.isBlank()) {
                            userName = msUser.getDisplayName() != null ? msUser.getDisplayName() : email;
                        }
                        
                        registrationNotificationClient.sendWelcomeEmailNotification(
                                email,
                                userId.toString(),
                                clientAdminId,
                                userName,
                                result.tempPassword()
                        );
                        
                        usersCreated++;
                        
                    } catch (Exception e) {
                        log.error("Error processing user: {}", email, e);
                        usersFailed++;
                    }
                }
            }

            // Todo: Send summary notification to admin
            
            log.info("Import completed - Created: {}, Updated: {}, Skipped: {}, Failed: {}", 
                    usersCreated, usersUpdated, usersSkipped, usersFailed);
            
        } catch (Exception e) {
            log.error("Error during user import", e);
            throw new RuntimeException("Failed to import users: " + e.getMessage(), e);
        }
        
        return ImportUsersResponseDto.builder()
                .totalUsersProcessed(processedEmails.size())
                .usersCreated(usersCreated)
                .usersSkipped(usersSkipped)
                .usersFailed(usersFailed)
                .completedAt(Instant.now())
                .build();
    }
    
    /**
     * Generate a temporary password for new users
     */
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * Create and save AspireUser from Microsoft user data
     *
     * @param msUser Microsoft user DTO
     * @param email User email address
     * @param clientAdminId Client admin ID
     * @param userRoleId Role ID to assign to the user
     * @param tempPassword Temporary password (plain text, will be encoded)
     * @return UserCreationResult containing userId and tempPassword
     */
    private UserCreationResult createAndSaveAspireUser(
            MicrosoftUserDto msUser,
            String email,
            String clientAdminId,
            String userRoleId,
            String tempPassword) {
        if (aspireUserRepository.existsByUsernameIgnoreCase(email)) {
            throw new ResourceAlreadyExistsException("User with username/email " + email + " already exists");
        }
        String encodedPassword = passwordEncoder.encode(tempPassword);
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        
        // Create department asynchronously if it doesn't exist
        if (msUser.getDepartment() != null && !msUser.getDepartment().isBlank()) {
            createDepartmentIfNotExists(msUser.getDepartment().trim(), clientAdminId);
        }
        
        AspireUser aspireUser = AspireUser.builder()
                .id(userId)
                .userId(userId)
                .firstName(msUser.getGivenName() != null ? msUser.getGivenName() : "")
                .lastName(msUser.getSurname() != null ? msUser.getSurname() : "")
                .username(email)
                .email(email)
                .password(encodedPassword)
                .phoneNumber(msUser.getMobilePhone())
                .userType(UserType.USER.getValue())
                .status(UserStatus.ACTIVE.name())
                .clientAdminId(clientAdminId)
                .department(msUser.getDepartment())
                .designation(msUser.getJobTitle())
                .userSource(UserSource.MICROSOFT_ENTRA)
                .createdAt(now)
                .updatedAt(now)
                .roles(List.of(userRoleId)) // Assign USER role ID
                .build();
        
        // Save user
        aspireUserRepository.save(aspireUser);
        
        return new UserCreationResult(userId, tempPassword);
    }
    
    /**
     * Record to hold user creation result
     */
    private record UserCreationResult(UUID userId, String tempPassword) {
    }
    
    /**
     * Update existing AspireUser with name, department, and designation from Microsoft user data
     *
     * @param existingUser Existing AspireUser to update
     * @param msUser Microsoft user DTO with updated information
     */
    private void updateExistingAspireUser(AspireUser existingUser, MicrosoftUserDto msUser) {
        // Update first name
        if (msUser.getGivenName() != null && !msUser.getGivenName().isBlank()) {
            existingUser.setFirstName(msUser.getGivenName());
        }
        
        // Update last name
        if (msUser.getSurname() != null && !msUser.getSurname().isBlank()) {
            existingUser.setLastName(msUser.getSurname());
        }
        
        // Update department
        if (msUser.getDepartment() != null && !msUser.getDepartment().isBlank()) {
            existingUser.setDepartment(msUser.getDepartment());
        }
        
        // Update designation (job title)
        if (msUser.getJobTitle() != null && !msUser.getJobTitle().isBlank()) {
            existingUser.setDesignation(msUser.getJobTitle());
        }
        
        // Update timestamp
        existingUser.setUpdatedAt(Instant.now());
        
        // Save updated user
        aspireUserRepository.save(existingUser);
        
        log.debug("Updated existing AspireUser with email: {}", existingUser.getEmail());
    }
    
    /**
     * Build full name from first and last name
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Asynchronously create a department if it doesn't exist
     *
     * @param departmentName The name of the department
     * @param clientAdminId The client admin ID
     */
    @Async
    private void createDepartmentIfNotExists(String departmentName, String clientAdminId) {
        try {
            Optional<Department> departmentOpt = departmentRepository.findByNameIgnoreCase(departmentName);
            if (departmentOpt.isEmpty()) {
                Instant now = Instant.now();
                Department department = Department.builder()
                        .name(departmentName)
                        .description("Department for " + departmentName)
                        .isSystemDefined(false)
                        .clientAdminId(clientAdminId)
                        .active(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .createdBy(clientAdminId)
                        .build();
                departmentRepository.save(department);
                log.debug("Created department asynchronously: {} for clientAdminId: {}", departmentName, clientAdminId);
            } else {
                log.debug("Department already exists: {} for clientAdminId: {}", departmentName, clientAdminId);
            }
        } catch (Exception e) {
            log.error("Error creating department asynchronously: {} for clientAdminId: {}", departmentName, clientAdminId, e);
            // Don't throw exception - department creation failure shouldn't block user creation
        }
    }

    private ImportResultDto mapToImportResultDto(UserImportJob job) {
        return ImportResultDto.builder()
                .importId(job.getId())
                .status(job.getStatus())
                .totalUsersProcessed(job.getTotalUsersProcessed())
                .usersCreated(job.getUsersCreated())
                .usersUpdated(job.getUsersUpdated())
                .usersSkipped(job.getUsersSkipped())
                .usersFailed(job.getUsersFailed())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }
}

