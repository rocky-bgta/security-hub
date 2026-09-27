package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.validation.PasswordValidationContext;
import com.aspire.asat.common.validation.PasswordValidationResult;
import com.aspire.asat.common.validation.PasswordValidator;
import com.aspire.asat.registration.data.dto.AspireUserCreateRequestDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.dto.AspireUserUpdateRequestDto;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.enums.OnboardBy;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.mapper.AspireUserMapper;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.Client;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.model.User;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.data.phishing.request.UserRiskProfileSaveRequestDto;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AspireUserServiceImpl implements AspireUserService {

    private final AspireUserRepository aspireUserRepository;
    private final AspireUserMapper aspireUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final PhishingServiceClient phishingServiceClient;

    @Value("${trial.period-days:30}")
    private Integer trialPeriodDays;

    @Override
    public AspireUserDto createUser(AspireUserCreateRequestDto requestDto) {
        log.info("Creating new AspireUser with email: {}", requestDto.getEmail());

        // Check if a user already exists (username/email uniqueness)
        if (aspireUserRepository.existsByUsernameIgnoreCase(requestDto.getEmail().trim())) {
            throw new ResourceAlreadyExistsException("User with username/email " + requestDto.getEmail() + " already exists");
        }

        // Enforce password policy when plain password is provided and not skipped (sync/system-generated use skipPasswordValidation=true)
        String plainToValidate = StringUtils.hasText(requestDto.getPlainPassword()) ? requestDto.getPlainPassword() : null;
        if (plainToValidate == null && requestDto.getPassword() != null && !requestDto.getPassword().startsWith("$2")) {
            plainToValidate = requestDto.getPassword();
        }
        if (plainToValidate != null && !Boolean.TRUE.equals(requestDto.getSkipPasswordValidation())) {
            PasswordValidationContext context = PasswordValidationContext.builder()
                    .email(requestDto.getEmail())
                    .username(requestDto.getEmail())
                    .firstName(requestDto.getFirstName())
                    .lastName(requestDto.getLastName())
                    .build();
            PasswordValidationResult validation = PasswordValidator.validate(plainToValidate, context);
            if (!validation.isValid()) {
                throw new com.aspire.asat.registration.exception.RegistrationServiceException(validation.getCombinedMessage());
            }
        }

        // Fetch mspId from ClientAdmin if clientAdminId is present
        String mspId = requestDto.getMspId();
        if (mspId == null && StringUtils.hasText(requestDto.getClientAdminId())) {
            mspId = fetchMspIdFromClientAdmin(requestDto.getClientAdminId());
        }

        // Create a new AspireUser
        AspireUser aspireUser = AspireUser.builder()
                .id(requestDto.getBaseUserId() != null ? requestDto.getBaseUserId() : UUID.randomUUID())
                .userId(requestDto.getBaseUserId())
                .firstName(requestDto.getFirstName())
                .lastName(requestDto.getLastName())
                .username(requestDto.getEmail()) // Username is set as email
                .email(requestDto.getEmail())
                .password(requestDto.getPassword()) // Password is already encoded from conversion methods
                .phoneNumber(requestDto.getPhoneNumber())
                .phoneCode(requestDto.getPhoneCode())
                .country(requestDto.getCountry())
                .countryCode(requestDto.getCountryCode())
                .address(requestDto.getAddress())
                .roles(requestDto.getRoles())
                .userType(requestDto.getUserType())
                .status(requestDto.getStatus())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .clientAdminId(requestDto.getClientAdminId())
                .mspId(mspId)
                .department(requestDto.getDepartment())
                .riskGroup(requestDto.getRiskGroup())
                .companyName(requestDto.getCompanyName())
                .designation(requestDto.getDesignation())
                .supervisorName(requestDto.getSupervisorName())
                .profilePicture(requestDto.getProfilePicture())
                .isCredentialSent(requestDto.getIsCredentialSent())
                .isDefault(requestDto.getIsDefault())
                .build();

        AspireUser savedUser = aspireUserRepository.save(aspireUser);
        log.info("Created AspireUser with ID: {}", savedUser.getUserId());

        AspireUserDto userMapperDto = aspireUserMapper.toDto(savedUser);
        userMapperDto.setPlainPassword(requestDto.getPlainPassword());
        return userMapperDto;
    }

    /**
     * Sends the newly created USER AspireUser to Phishing module as UserRiskProfile.
     * Failures are logged and do not affect the main flow.
     */
    @Override
    public void saveUserRiskProfileInPhishing(AspireUser savedUser) {
        try {
            UserRiskProfileSaveRequestDto request = UserRiskProfileSaveRequestDto.builder()
                    .clientId(savedUser.getClientAdminId())
                    .userId(savedUser.getUserId() != null ? savedUser.getUserId().toString() : null)
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .department(savedUser.getDepartment())
                    .isTrainingEnabled(true)
                    .build();
            phishingServiceClient.saveUserRiskProfile(request);
        } catch (Exception e) {
            log.warn("Failed to save UserRiskProfile in Phishing for userId={}, clientId={}: {}",
                    savedUser.getUserId(), savedUser.getClientAdminId(), e.getMessage());
        }
    }

    public String encodeIfNeeded(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return rawPassword;
        }

        // BCrypt encoded passwords always start with $2a$, $2b$, or $2y$
        if (rawPassword.startsWith("$2a$")
                || rawPassword.startsWith("$2b$")
                || rawPassword.startsWith("$2y$")) {
            return rawPassword; // already encoded
        }

        // Otherwise encode
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public AspireUserDto createAspireUser(String userId, UserType userType, Object domainUserData) {
        log.info("Creating AspireUser from domain data for baseUserId: {}, userType: {}", userId, userType);

        // Check if a user already exists
        if (aspireUserRepository.existsByUserId(UUID.fromString(userId))) {
            log.warn("AspireUser already exists for baseUserId: {}", userId);
            return null;
        }

        // Convert domain user data to AspireUserCreateRequestDto using utility
        AspireUserCreateRequestDto createRequest = convertDomainDataToCreateRequest(userId, userType, domainUserData);

        if (createRequest == null) {
            log.error("Failed to convert domain data to create request for baseUserId: {}", userId);
            return null;
        }

        // Create the AspireUser
        return this.createUser(createRequest);
    }

    @Override
    @Transactional
    public void updateAspireUser(String baseUserId, UserType userType, Object domainUserData) {
        log.info("Updating AspireUser from domain data for baseUserId: {}, userType: {}", baseUserId, userType);

        // Find existing AspireUser
        Optional<AspireUser> existingUser = aspireUserRepository.findByUserId(UUID.fromString(baseUserId));

        if (existingUser.isEmpty()) {
            log.warn("AspireUser not found for baseUserId: {}, creating new record", baseUserId);
            this.createAspireUser(baseUserId, userType, domainUserData);
            return;
        }

        // Convert domain user data to AspireUserUpdateRequestDto
        AspireUserUpdateRequestDto updateRequest = convertDomainDataToUpdateRequest(userType, domainUserData);

        if (updateRequest == null) {
            log.error("Failed to convert domain data to update request for baseUserId: {}", baseUserId);
            return;
        }

        // Update the AspireUser
        updateUser(existingUser.get().getUserId(), updateRequest);
    }

    /**
     * Convert domain-specific user data to AspireUserUpdateRequestDto
     */
    private AspireUserUpdateRequestDto convertDomainDataToUpdateRequest(UserType userType, Object domainUserData) {
        try {
            switch (userType) {
                case CLIENT:
                    if (domainUserData instanceof Client) {
                        return convertClientToUpdateRequest((Client) domainUserData);
                    }
                    break;
                case CLIENT_ADMIN:
                    if (domainUserData instanceof ClientAdmin) {
                        return convertClientAdminToUpdateRequest((ClientAdmin) domainUserData);
                    }
                    break;
                case USER:
                    if (domainUserData instanceof User) {
                        return convertUserToUpdateRequest((User) domainUserData);
                    }
                    break;
                case MSP:
                    if (domainUserData instanceof MspUser) {
                        return convertMspUserToUpdateRequest((MspUser) domainUserData);
                    }
                    if (domainUserData instanceof ClientAdmin) {
                        return convertMspToUpdateRequest((ClientAdmin) domainUserData);
                    }
                    break;
                default:
                    log.warn("Unknown user type: {}", userType);
                    return null;
            }
        } catch (Exception e) {
            log.error("Error converting domain data to update request", e);
        }

        return null;
    }

    private AspireUserUpdateRequestDto convertClientToUpdateRequest(Client client) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.CLIENT.name()));

        return AspireUserUpdateRequestDto.builder()
                .firstName(extractFirstName(client.getName()))
                .lastName(extractLastName(client.getName()))
                .email(client.getEmail())
                .phoneNumber(client.getPhone())
                .country(client.getCountry())
                .address(client.getAddress())
                .roles(roleIds)
                .userType(UserType.CLIENT.name())
                .status(UserStatus.ACTIVE.name())
                .profilePicture(client.getLogo())
                .build();
    }

    private AspireUserUpdateRequestDto convertClientAdminToUpdateRequest(ClientAdmin clientAdmin) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.CLIENT_ADMIN.name()));

        return AspireUserUpdateRequestDto.builder()
                .firstName(extractFirstName(clientAdmin.getOrganizationName()))
                .lastName(extractLastName(clientAdmin.getOrganizationName()))
                .email(clientAdmin.getEmail())
                .phoneNumber(clientAdmin.getPhoneNumber())
                .phoneCode(clientAdmin.getPhoneCode())
                .country(clientAdmin.getCountry())
                .countryCode(clientAdmin.getCountryCode())
                .address(buildAddressString(clientAdmin))
                .roles(roleIds)
                .userType(UserType.CLIENT_ADMIN.name())
                .status(clientAdmin.getStatus() != null ? clientAdmin.getStatus().toString() : UserStatus.ACTIVE.name())
                .profilePicture(clientAdmin.getLogoUrl())
                .clientAdminId(clientAdmin.getId())
                .mspId(clientAdmin.getMspId())
                .build();
    }

    private AspireUserUpdateRequestDto convertUserToUpdateRequest(User user) {
        String roleName = user.getRole() != null ? user.getRole().toString() : UserType.USER.name();
        List<String> roleIds = convertRoleNamesToIds(List.of(roleName));

        return AspireUserUpdateRequestDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .country(user.getCountry())
                .roles(roleIds)
                .userType(UserType.USER.name())
                .profilePicture(user.getLogo())
                .status(user.getUserStatus() != null ? user.getUserStatus().toString() : UserStatus.ACTIVE.name())
                .build();
    }

    private AspireUserUpdateRequestDto convertMspToUpdateRequest(ClientAdmin mspAdmin) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.MSP.name()));

        return AspireUserUpdateRequestDto.builder()
                .firstName(extractFirstName(mspAdmin.getOrganizationName()))
                .lastName(extractLastName(mspAdmin.getOrganizationName()))
                .email(mspAdmin.getEmail())
                .phoneNumber(mspAdmin.getPhoneNumber())
                .country(mspAdmin.getCountry())
                .countryCode(mspAdmin.getCountryCode())
                .address(buildAddressString(mspAdmin))
                .roles(roleIds)
                .userType(UserType.MSP.name())
                .status(mspAdmin.getStatus() != null ? mspAdmin.getStatus().toString() : UserStatus.ACTIVE.name())
                .profilePicture(mspAdmin.getLogoUrl())
                .clientAdminId(mspAdmin.getId())
                .mspId(mspAdmin.getMspId())
                .build();
    }

    private AspireUserUpdateRequestDto convertMspUserToUpdateRequest(MspUser mspUser) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.MSP.name()));

        return AspireUserUpdateRequestDto.builder()
                .firstName(extractFirstName(mspUser.getOrganizationName()))
                .lastName(extractLastName(mspUser.getOrganizationName()))
                .email(mspUser.getMspAdminEmail() != null ? mspUser.getMspAdminEmail() : mspUser.getContactEmail())
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .country(mspUser.getCountry())
                .address(buildMspAddressString(mspUser))
                .roles(roleIds)
                .userType(UserType.MSP.name())
                .status(mspUser.getStatus() != null ? mspUser.getStatus() : UserStatus.ACTIVE.name())
                .profilePicture(mspUser.getLogoUrl())
                .mspId(mspUser.getMspId() != null ? mspUser.getMspId() : mspUser.getId())
                .department(mspUser.getDepartment())
                .build();
    }

    private String buildMspAddressString(MspUser mspUser) {
        StringBuilder address = new StringBuilder();
        if (StringUtils.hasText(mspUser.getOrganizationStreetAddress())) {
            address.append(mspUser.getOrganizationStreetAddress());
        }
        if (StringUtils.hasText(mspUser.getOrganizationStreetAddressLine2())) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(mspUser.getOrganizationStreetAddressLine2());
        }
        if (StringUtils.hasText(mspUser.getOrganizationCity())) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(mspUser.getOrganizationCity());
        }
        if (StringUtils.hasText(mspUser.getOrganizationZipPostalCode())) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(mspUser.getOrganizationZipPostalCode());
        }
        if (address.isEmpty() && StringUtils.hasText(mspUser.getCompanyAddress())) {
            return mspUser.getCompanyAddress();
        }
        return address.toString();
    }

    /**
     * Convert domain-specific user data to AspireUserCreateRequestDto
     */
    private AspireUserCreateRequestDto convertDomainDataToCreateRequest(String baseUserId, UserType userType, Object domainUserData) {
        try {
            switch (userType) {
                case CLIENT:
                    if (domainUserData instanceof Client) {
                        return convertClientToCreateRequest(baseUserId, (Client) domainUserData);
                    }
                    break;
                case CLIENT_ADMIN:
                    if (domainUserData instanceof ClientAdmin) {
                        return convertClientAdminToCreateRequest(baseUserId, (ClientAdmin) domainUserData);
                    }
                    break;
                case USER:
                    if (domainUserData instanceof User) {
                        return convertUserToCreateRequest(baseUserId, (User) domainUserData);
                    }
                    break;
                case MSP:
                    if (domainUserData instanceof MspUser) {
                        return convertMspUserToCreateRequest(baseUserId, (MspUser) domainUserData);
                    }
                    if (domainUserData instanceof ClientAdmin) {
                        return convertMspToCreateRequest(baseUserId, (ClientAdmin) domainUserData);
                    }
                    break;
                default:
                    log.error("Unknown user type: {}", userType);
                    return null;
            }
        } catch (Exception e) {
            log.error("Error converting domain data to create request for baseUserId: {}", baseUserId, e);
        }

        return null;
    }

    private AspireUserCreateRequestDto convertClientToCreateRequest(String baseUserId, Client client) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.CLIENT.name()));
        String tempPassword = CommonUtils.generateTemporaryPassword();
        String encodedPassword = encodeIfNeeded(tempPassword);
        log.info("Generated temporary password for CLIENT user with email: {}", client.getEmail());

        return AspireUserCreateRequestDto.builder()
                .baseUserId(UUID.fromString(baseUserId))
                .firstName(extractFirstName(client.getName()))
                .lastName(extractLastName(client.getName()))
                .email(client.getEmail())
                .password(encodedPassword)
                .plainPassword(tempPassword)
                .phoneNumber(client.getPhone())
                .country(client.getCountry())
                .countryCode("") // Extract from phone if needed
                .address(client.getAddress())
                .roles(roleIds)
                .userType(UserType.CLIENT.name())
                .status(UserStatus.ACTIVE.name())
                .status(client.getLogo())
                .build();
    }

    private AspireUserCreateRequestDto convertClientAdminToCreateRequest(String baseUserId, ClientAdmin clientAdmin) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.CLIENT_ADMIN.name()));
        // BUY_NOW / TRIAL users set their own password during onboarding — never force reset.
        boolean selfServeOnboard = clientAdmin.getOnboardBy() == OnboardBy.BUY_NOW
                || clientAdmin.getOnboardBy() == OnboardBy.TRIAL;
        boolean systemGeneratedTempPassword = !selfServeOnboard;
        String tempPassword = ObjectUtils.isEmpty(clientAdmin.getHashedPassword())
                ? CommonUtils.generateTemporaryPassword()
                : clientAdmin.getHashedPassword();
        String encodedPassword = encodeIfNeeded(tempPassword);
        log.info("Creating CLIENT_ADMIN AspireUser for email: {}, forcePasswordReset={}",
                clientAdmin.getEmail(), systemGeneratedTempPassword);

        return AspireUserCreateRequestDto.builder()
                .baseUserId(UUID.fromString(baseUserId))
                .firstName(extractFirstName(clientAdmin.getOrganizationName()))
                .lastName(extractLastName(clientAdmin.getOrganizationName()))
                .email(clientAdmin.getEmail())
                .password(encodedPassword) //Set encoded temporary password
                .plainPassword(tempPassword) // Set plain password
                .phoneNumber(clientAdmin.getPhoneNumber())
                .phoneCode(clientAdmin.getPhoneCode())
                .country(clientAdmin.getCountry())
                .countryCode(clientAdmin.getCountryCode())
                .address(buildAddressString(clientAdmin))
                .roles(roleIds)
                .userType(UserType.CLIENT_ADMIN.name())
                .status(clientAdmin.getStatus() != null ? clientAdmin.getStatus().toString() : UserStatus.ACTIVE.name())
                .profilePicture(clientAdmin.getLogoUrl())
                .clientAdminId(clientAdmin.getId())
                .mspId(clientAdmin.getMspId())
                .isDefault(systemGeneratedTempPassword)
                .skipPasswordValidation(systemGeneratedTempPassword)
                .build();
    }

    private AspireUserCreateRequestDto convertUserToCreateRequest(String baseUserId, User user) {
        String roleName = user.getRole() != null ? user.getRole().toString() : UserType.USER.name();
        List<String> roleIds = convertRoleNamesToIds(List.of(roleName));
        String tempPassword = CommonUtils.generateTemporaryPassword();
        String encodedPassword = encodeIfNeeded(tempPassword);
        log.info("Generated temporary password for USER with email: {}", user.getEmail());

        return AspireUserCreateRequestDto.builder()
                .baseUserId(UUID.fromString(baseUserId))
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(encodedPassword) //Set encoded temporary password
                .plainPassword(tempPassword) // Set plain password                .phoneNumber("") // User model doesn't have phone
                .country(user.getCountry())
                .countryCode("")
                .address("") // User model doesn't have address
                .roles(roleIds)
                .userType(UserType.USER.name())
                .status(user.getUserStatus() != null ? user.getUserStatus().toString() : UserStatus.ACTIVE.name())
                .profilePicture(user.getLogo())
                .build();
    }

    private AspireUserCreateRequestDto convertMspToCreateRequest(String baseUserId, ClientAdmin mspAdmin) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.MSP.name()));
        // BUY_NOW / TRIAL users set their own password during onboarding — never force reset.
        boolean selfServeOnboard = mspAdmin.getOnboardBy() == OnboardBy.BUY_NOW
                || mspAdmin.getOnboardBy() == OnboardBy.TRIAL;
        boolean systemGeneratedTempPassword = !selfServeOnboard;
        String tempPassword = CommonUtils.generateTemporaryPassword();
        String encodedPassword = encodeIfNeeded(tempPassword);
        log.info("Creating MSP AspireUser for email: {}, forcePasswordReset={}",
                mspAdmin.getEmail(), systemGeneratedTempPassword);

        return AspireUserCreateRequestDto.builder()
                .baseUserId(UUID.fromString(baseUserId))
                .firstName(extractFirstName(mspAdmin.getOrganizationName()))
                .lastName(extractLastName(mspAdmin.getOrganizationName()))
                .email(mspAdmin.getEmail())
                .password(encodedPassword) //Set an encoded temporary password
                .plainPassword(tempPassword) // Set plain password                .phoneNumber(mspAdmin.getPhoneNumber())
                .country(mspAdmin.getCountry())
                .countryCode(mspAdmin.getCountryCode())
                .address(buildAddressString(mspAdmin))
                .roles(roleIds)
                .userType(UserType.MSP.name())
                .status(mspAdmin.getStatus() != null ? mspAdmin.getStatus().toString() : UserStatus.ACTIVE.name())
                .profilePicture(mspAdmin.getLogoUrl())
                .clientAdminId(mspAdmin.getId())
                .mspId(mspAdmin.getMspId())
                .isDefault(systemGeneratedTempPassword)
                .skipPasswordValidation(systemGeneratedTempPassword)
                .build();
    }

    private AspireUserCreateRequestDto convertMspUserToCreateRequest(String baseUserId, MspUser mspUser) {
        List<String> roleIds = convertRoleNamesToIds(List.of(UserType.MSP.name()));
        String tempPassword = CommonUtils.generateTemporaryPassword();
        String encodedPassword = encodeIfNeeded(tempPassword);
        String email = mspUser.getMspAdminEmail() != null ? mspUser.getMspAdminEmail() : mspUser.getContactEmail();
        log.info("Creating MSP AspireUser for email: {}", email);

        return AspireUserCreateRequestDto.builder()
                .baseUserId(UUID.fromString(baseUserId))
                .firstName(extractFirstName(mspUser.getOrganizationName()))
                .lastName(extractLastName(mspUser.getOrganizationName()))
                .email(email)
                .password(encodedPassword)
                .plainPassword(tempPassword)
                .phoneNumber(mspUser.getPhoneNumber())
                .phoneCode(mspUser.getPhoneCode())
                .country(mspUser.getCountry())
                .address(buildMspAddressString(mspUser))
                .roles(roleIds)
                .userType(UserType.MSP.name())
                .status(mspUser.getStatus() != null ? mspUser.getStatus() : UserStatus.ACTIVE.name())
                .profilePicture(mspUser.getLogoUrl())
                .mspId(mspUser.getMspId() != null ? mspUser.getMspId() : mspUser.getId())
                .department(mspUser.getDepartment())
                .isDefault(true)
                .skipPasswordValidation(true)
                .build();
    }

    private String extractFirstName(String fullName) {
        if (StringUtils.hasText(fullName)) {
            String[] names = fullName.trim().split("\\s+");
            return names[0];
        }
        return "";
    }

    private String extractLastName(String fullName) {
        if (StringUtils.hasText(fullName)) {
            String[] names = fullName.trim().split("\\s+");
            if (names.length > 1) {
                return String.join(" ", Arrays.copyOfRange(names, 1, names.length));
            }
        }
        return "";
    }

    @Override
    public AspireUserDto updateUser(UUID id, AspireUserUpdateRequestDto requestDto) {
        log.info("Updating AspireUser with ID: {}", id);

        AspireUser existingUser = aspireUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        updateFieldIfPresent(requestDto.getFirstName(), existingUser::setFirstName);
        updateFieldIfPresent(requestDto.getLastName(), existingUser::setLastName);

        updateFieldIfPresent(requestDto.getPassword(), password ->
                existingUser.setPassword(encodeIfNeeded(password))
        );
        updateFieldIfPresent(requestDto.getPhoneNumber(), existingUser::setPhoneNumber);
        updateFieldIfPresent(requestDto.getPhoneCode(), existingUser::setPhoneCode);
        updateFieldIfPresent(requestDto.getCountry(), existingUser::setCountry);
        updateFieldIfPresent(requestDto.getCountryCode(), existingUser::setCountryCode);
        updateFieldIfPresent(requestDto.getAddress(), existingUser::setAddress);
        updateFieldIfPresent(requestDto.getDepartment(), existingUser::setDepartment);
        updateFieldIfPresent(requestDto.getProfilePicture(), existingUser::setProfilePicture);

        if (requestDto.getRoles() != null && !requestDto.getRoles().isEmpty()) {
            List<String> roleIds = requestDto.getRoles().stream()
                    .map(role -> role.matches("[a-fA-F0-9]{24}") ? role : getRoleIdByName(role))
                    .filter(Objects::nonNull)
                    .toList();
            existingUser.setRoles(roleIds);
        }

        updateFieldIfPresent(requestDto.getUserType(), existingUser::setUserType);
        updateFieldIfPresent(requestDto.getStatus(), existingUser::setStatus);

        // Update clientAdminId and fetch mspId if clientAdminId is being updated
        if (requestDto.getClientAdminId() != null) {
            existingUser.setClientAdminId(requestDto.getClientAdminId());
            // Fetch mspId from ClientAdmin if clientAdminId is present
            String mspId = requestDto.getMspId();
            if (mspId == null && StringUtils.hasText(requestDto.getClientAdminId())) {
                mspId = fetchMspIdFromClientAdmin(requestDto.getClientAdminId());
            }
            existingUser.setMspId(mspId);
        } else if (requestDto.getMspId() != null) {
            // Allow direct mspId update if provided
            existingUser.setMspId(requestDto.getMspId());
        }

        existingUser.setUpdatedAt(Instant.now());
        existingUser.setUpdatedBy(requestDto.getUpdateBy());

        AspireUser updatedUser = aspireUserRepository.save(existingUser);
        log.info("Updated AspireUser with ID: {}", updatedUser.getUserId());
        return aspireUserMapper.toDto(updatedUser);
    }

    private void updateFieldIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (isNotNullOrEmpty(value)) {
            setter.accept(value);
        }
    }

    private String getRoleIdByName(String roleName) {
        Role roleEntity = roleRepository.findByRoleName(roleName);
        return roleEntity != null ? roleEntity.getId() : null;
    }

    // Utility method for null or empty string check
    private boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public Optional<AspireUserDto> getUserById(UUID userId) {
        return aspireUserRepository.findByUserId(userId)
                .map(aspireUserMapper::toDto);
    }

    @Override
    public Optional<AspireUserDto> getUserByEmail(String email) {
        return aspireUserRepository.findByEmailIgnoreCase(email.trim())
                .map(aspireUserMapper::toDto);
    }


    @Override
    public Optional<AspireUserDto> getUserByUsername(String username) {
        return aspireUserRepository.findByUsername(username)
                .map(aspireUserMapper::toDto);
    }

    @Override
    public List<AspireUserDto> getAllUsers(int offset, int pageSize) {
        // Convert offset to page number for Spring Data pagination

        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<AspireUser> userPage = aspireUserRepository.findAll(pageable);

        return userPage.getContent().stream()
                .map(aspireUserMapper::toDto)
                .toList();
    }

    @Override
    public List<AspireUserDto> getUsersByType(String userType) {
        return aspireUserRepository.findByUserType(userType).stream()
                .map(aspireUserMapper::toDto)
                .toList();
    }

    @Override
    public List<AspireUserDto> getUsersByTypeAndClientAdminId(String userType, String clientAdminId) {
        return aspireUserRepository.findByUserTypeAndClientAdminId(userType, clientAdminId)
                .stream()
                .map(aspireUserMapper::toDto)
                .toList();
    }

    @Override
    public List<AspireUserDto> getUsersByStatus(String status) {
        return aspireUserRepository.findByStatus(status).stream()
                .map(aspireUserMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AspireUserDto updateRiskGroup(String userId, RiskGroup riskGroup) {
        log.info("Updating risk group for AspireUser with base userId: {}", userId);
        AspireUser existingUser = aspireUserRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userId: " + userId));
        existingUser.setRiskGroup(riskGroup);
        existingUser.setUpdatedAt(Instant.now());
        AspireUser updatedUser = aspireUserRepository.save(existingUser);
        return aspireUserMapper.toDto(updatedUser);
    }

    /**
     * Convert role names to role IDs using RoleRepository
     */
    private List<String> convertRoleNamesToIds(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return List.of();
        }

        return roleNames.stream()
                .map(roleName -> {
                    Role role = roleRepository.findByRoleName(roleName);
                    return role != null ? role.getId() : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Fetch mspId from ClientAdmin table using clientAdminId
     */
    private String fetchMspIdFromClientAdmin(String clientAdminId) {
        try {
            Optional<ClientAdmin> clientAdmin = clientAdminRepository.findById(clientAdminId);
            if (clientAdmin.isPresent() && StringUtils.hasText(clientAdmin.get().getMspId())) {
                log.info("Retrieved mspId: {} for clientAdminId: {}", clientAdmin.get().getMspId(), clientAdminId);
                return clientAdmin.get().getMspId();
            } else {
                log.warn("ClientAdmin not found or mspId is empty for clientAdminId: {}", clientAdminId);
            }
        } catch (Exception e) {
            log.error("Error fetching mspId for clientAdminId: {}", clientAdminId, e);
        }
        return null;
    }

    /**
     * Build address string from structured address fields in ClientAdmin
     */
    private String buildAddressString(ClientAdmin clientAdmin) {
        if (clientAdmin == null) {
            return "";
        }
        StringBuilder address = new StringBuilder();
        if (StringUtils.hasText(clientAdmin.getStreetAddress())) {
            address.append(clientAdmin.getStreetAddress());
        }
        if (StringUtils.hasText(clientAdmin.getStreetAddressLine2())) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(clientAdmin.getStreetAddressLine2());
        }
        if (StringUtils.hasText(clientAdmin.getCity())) {
            if (address.length() > 0) {
                address.append(", ");
            }
            address.append(clientAdmin.getCity());
        }
        if (StringUtils.hasText(clientAdmin.getZipPostalCode())) {
            if (address.length() > 0) {
                address.append(" ");
            }
            address.append(clientAdmin.getZipPostalCode());
        }
        return address.toString();
    }
}
