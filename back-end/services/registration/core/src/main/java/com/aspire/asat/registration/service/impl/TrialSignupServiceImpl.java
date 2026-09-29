package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.client.NotificationClient;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.validation.PasswordValidationContext;
import com.aspire.asat.common.validation.PasswordValidationResult;
import com.aspire.asat.common.validation.PasswordValidator;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.CmsServiceClient;
import com.aspire.asat.registration.data.buynow.request.BuyNowPasswordCreationRequestDto;
import com.aspire.asat.registration.data.buynow.request.BuyNowSignupRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.data.cms.request.ClientDashboardRequestDto;
import com.aspire.asat.registration.data.cms.request.ClientProductData;
import com.aspire.asat.registration.data.cms.request.TrialSubPackageCreationRequestDto;
import com.aspire.asat.registration.data.cms.response.SubPackageResponseDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.enums.NextDirection;
import com.aspire.asat.registration.data.enums.OnboardBy;
import com.aspire.asat.registration.data.enums.PackageStatus;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.trial.request.AssignTrialProductsRequestDto;
import com.aspire.asat.registration.data.trial.request.EmailVerificationRequestDto;
import com.aspire.asat.registration.data.trial.request.PasswordCreationRequestDto;
import com.aspire.asat.registration.data.trial.request.ProductsData;
import com.aspire.asat.registration.data.trial.request.TrialSignupRequestDto;
import com.aspire.asat.registration.data.trial.response.EmailVerificationResponseDto;
import com.aspire.asat.registration.data.trial.response.ExistingAdminUserInfoDto;
import com.aspire.asat.registration.data.trial.response.ExistingUsersResponseDto;
import com.aspire.asat.registration.data.trial.response.ProductsDataResponse;
import com.aspire.asat.registration.data.trial.response.TrialSignupResponseDto;
import com.aspire.asat.registration.exception.DomainConflictException;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.EndUserPackage;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.TrialSignupService;
import com.aspire.asat.registration.utils.DefaultMspData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialSignupServiceImpl implements TrialSignupService {

    private final AspireUserRepository aspireUserRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final ClientProductRepository clientProductRepository;
    private final ClientProductRepositoryCustom clientProductRepositoryCustom;
    private final EndUserPackageRepository endUserPackageRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final NotificationClient notificationClient;
    private final WebClient webClient;
    private final CmsServiceClient cmsServiceClient;
    private final AspireUserService aspireUserService;
    private final RegistrationNotificationClient registrationNotificationClient;
    private final MessageService messageService;

    @Value("${trial.period-days:30}")
    private Integer trialPeriodDays;

    @Value("${trial.otp.validity-seconds:300}")
    private Integer otpValiditySeconds;

    @Value("${trial.otp.console-enabled:false}")
    private boolean otpConsoleEnabled;

    @Value("${trial.allow-onboarding-with-existing-domain:false}")
    private Boolean allowTrialOnboardingWithExistingDomain;

    @Value("${aspire.admin.email:admin@aspire.com}")
    private String aspireAdminEmail;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    // Temporary storage for OTP codes and signup data (in-memory, could use Redis in production)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, TrialSignupRequestDto> signupDataStorage = new ConcurrentHashMap<>();
    private final Set<String> verifiedEmails = ConcurrentHashMap.newKeySet(); // Track verified emails
    
    // Separate storage for buy-now signup flow
    private final Map<String, OtpData> buyNowOtpStorage = new ConcurrentHashMap<>();
    private final Map<String, BuyNowSignupRequestDto> buyNowSignupDataStorage = new ConcurrentHashMap<>();
    private final Set<String> buyNowVerifiedEmails = ConcurrentHashMap.newKeySet(); // Track verified emails for buy-now
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public EmailVerificationResponseDto submitAccountDetails(TrialSignupRequestDto request) {
        log.info("Submitting account details for trial signup: {}", request.getEmail());

        if (!hasProductsData(request.getProductsData())) {
            return submitLegacyAccountDetails(request);
        }

        Optional<AspireUser> existingUserOpt = aspireUserRepository.findByEmailIgnoreCase(request.getEmail().trim());
        if (existingUserOpt.isPresent()) {
            AspireUser existingUser = existingUserOpt.get();
            String clientAdminId = resolveClientAdminId(existingUser);
            List<ProductsDataResponse> productsData = buildExistingTrailFlags(clientAdminId, request.getProductsData());
            boolean anyExistingTrail = hasAnyExistingTrail(productsData);

            String emailKey = request.getEmail().toLowerCase();
            signupDataStorage.put(emailKey, request);

            if (anyExistingTrail) {
                log.info("Existing trial found for email {}. Skipping OTP.", request.getEmail());
                verifiedEmails.remove(emailKey);
                otpStorage.remove(emailKey);
                return EmailVerificationResponseDto.builder()
                        .verified(false)
                        .message("You already have a trial account for one or more selected products")
                        .email(request.getEmail())
                        .productsData(productsData)
                        .nextDirection(NextDirection.ONBOARD)
                        .build();
            }

            return sendTrialVerificationCode(request, productsData, NextDirection.ONBOARD);
        }

        enforceTrialDomainRestriction(request);

        List<ProductsDataResponse> productsData = toNewProductFlags(request.getProductsData());
        return sendTrialVerificationCode(request, productsData, NextDirection.VERIFY);
    }

    /**
     * Original trial signup: no productsData. Email uniqueness + domain check + OTP, unchanged.
     */
    private EmailVerificationResponseDto submitLegacyAccountDetails(TrialSignupRequestDto request) {
        if (aspireUserRepository.existsByUsernameIgnoreCase(request.getEmail().trim())) {
            throw new ResourceAlreadyExistsException("User with username/email " + request.getEmail() + " already exists");
        }

        enforceTrialDomainRestriction(request);

        return sendTrialVerificationCode(request, null, NextDirection.VERIFY);
    }

    private void enforceTrialDomainRestriction(TrialSignupRequestDto request) {
        if (!allowTrialOnboardingWithExistingDomain) {
            String emailDomain = extractDomainFromEmail(request.getEmail());
            String domainPattern = "@" + emailDomain.replace(".", "\\.") + "$";
            List<ClientAdmin> existingAdminsWithDomain = clientAdminRepository.findByEmailDomainRegex(domainPattern);

            if (!existingAdminsWithDomain.isEmpty()) {
                log.warn("Trial signup attempt with existing domain detected. Email: {}, Domain: {}",
                        request.getEmail(), emailDomain);

                ExistingUsersResponseDto existingUsersInfo = getExistingUsersByDomain(emailDomain);
                sendDomainConflictNotification(request, emailDomain, existingUsersInfo);

                throw new DomainConflictException(
                    "A free trial for this domain was activated within the last 6 months, so new trial requests aren’t available. Please use the Need Help? or Contact Support button below to explore next steps.",
                        new ExistingUsersResponseDto()
                );
            }
        }
    }

    @Override
    @Transactional
    public EmailVerificationResponseDto submitBuyNowAccountDetails(BuyNowSignupRequestDto request) {
        log.info("Submitting account details for buy now signup: {}", request.getEmail());

        // Check if user with this username/email already exists (case-insensitive; prevents duplicates)
        if (aspireUserRepository.existsByUsernameIgnoreCase(request.getEmail().trim())) {
            throw new ResourceAlreadyExistsException("User with username/email " + request.getEmail() + " already exists");
        }

        String emailKey = request.getEmail().toLowerCase();
        
        // Clear any previous verification status if resubmitting (both buy-now and trial storage)
        buyNowVerifiedEmails.remove(emailKey);
        buyNowOtpStorage.remove(emailKey);
        verifiedEmails.remove(emailKey);
        otpStorage.remove(emailKey);

        // Store buy-now signup data separately (for buy-now password creation)
        buyNowSignupDataStorage.put(emailKey, request);

        // Also convert and store in trial storage so trial verify-email API can be used
        TrialSignupRequestDto trialSignupRequest = TrialSignupRequestDto.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .phoneCode(request.getPhoneCode())
                .firstName("") // Empty as per requirement
                .lastName("") // Empty as per requirement
                .companyName(request.getCompanyName())
                .numberOfEmployees(request.getNumberOfEmployees())
                .howDidYouHearAboutUs(request.getHowDidYouHearAboutUs())
                .build();
        signupDataStorage.put(emailKey, trialSignupRequest);

        // Generate and send OTP - store in trial storage so trial verify-email API can be used
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(otpValiditySeconds);
        
        otpStorage.put(emailKey, new OtpData(otp, expiresAt));

        // Send verification email (use email as name since firstName is empty)
        sendVerificationEmail(request.getEmail(), otp, request.getCompanyName());

        log.info("Verification code sent to: {} for buy now signup (using trial verify-email API)", request.getEmail());

        return EmailVerificationResponseDto.builder()
                .verified(false)
                .message("Verification code sent to your email")
                .email(request.getEmail())
                .nextDirection(NextDirection.VERIFY)
                .build();
    }

    @Override
    @Transactional
    public EmailVerificationResponseDto verifyEmail(EmailVerificationRequestDto request) {
        log.info("Verifying email with code for: {}", request.getEmail());

        String emailKey = request.getEmail().toLowerCase();
        OtpData otpData = otpStorage.get(emailKey);

        if (otpData == null) {
            throw new IllegalArgumentException("No verification code found. Please request a new code.");
        }

        if (otpData.getExpiresAt().isBefore(Instant.now())) {
            otpStorage.remove(emailKey);
            throw new IllegalArgumentException("Verification code has expired. Please request a new code.");
        }

        if (!otpData.getOtp().equals(request.getVerificationCode())) {
            throw new IllegalArgumentException("Invalid verification code. Please try again.");
        }

        // Mark OTP as used (remove from storage) and mark email as verified
        otpStorage.remove(emailKey);
        verifiedEmails.add(emailKey);
        
        // Also mark buy-now as verified if it exists (for buy-now signup flow)
        if (buyNowSignupDataStorage.containsKey(emailKey)) {
            buyNowVerifiedEmails.add(emailKey);
        }

        log.info("Email verified successfully for: {}", request.getEmail());

        return EmailVerificationResponseDto.builder()
                .verified(true)
                .message(messageService.get(MessageKeys.AUTH_EMAIL_VERIFIED))
                .email(request.getEmail())
                .build();
    }

    @Override
    @Transactional
    public TrialSignupResponseDto createPassword(PasswordCreationRequestDto request) {
        log.info("Creating password and completing trial signup for: {}", request.getEmail());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Retrieve signup data
        String emailKey = request.getEmail().toLowerCase();
        TrialSignupRequestDto signupData = signupDataStorage.get(emailKey);
        
        if (signupData == null) {
            throw new ResourceNotFoundException("Signup data not found. Please start the signup process again.");
        }

        // Validate email matches the one used in signup
        if (!signupData.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email does not match the one used during signup");
        }

        // Check if email was verified
        if (!verifiedEmails.contains(emailKey)) {
            throw new IllegalArgumentException("Please verify your email first before creating password");
        }

        // Check if user already exists
        if (aspireUserRepository.existsByUsernameIgnoreCase(request.getEmail().trim())) {
            if (!hasProductsData(request.getProductsData()) && !hasProductsData(signupData.getProductsData())) {
                signupDataStorage.remove(emailKey);
                throw new ResourceAlreadyExistsException("User with username/email " + request.getEmail() + " already exists");
            }
            throw new ResourceAlreadyExistsException(
                    "A trial account already exists for this email. Use assign-products to add additional trial products.");
        }

        // Enforce password policy
        PasswordValidationContext context = PasswordValidationContext.builder()
                .email(signupData.getEmail())
                .firstName(signupData.getFirstName())
                .lastName(signupData.getLastName())
                .build();
        PasswordValidationResult validation = PasswordValidator.validate(request.getPassword(), context);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getCombinedMessage());
        }

        // Encrypt password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Get role IDs for CLIENT_ADMIN (trial users are client admins)
        List<String> roles = convertRoleNamesToIds(List.of(UserType.CLIENT_ADMIN.name()));

        // Create AspireUser // Create AspireUser with trial fields and persist via repository
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        AspireUser user = new AspireUser();
        user.setId(userId);
        user.setUserId(userId);
        user.setFirstName(signupData.getFirstName());
        user.setLastName(signupData.getLastName());
        user.setEmail(signupData.getEmail());
        user.setUsername(signupData.getEmail());
        user.setPassword(hashedPassword);
        String resolvedPhoneCode = resolvePhoneCode(signupData.getPhoneCode(), signupData.getPhoneNumber());
        user.setPhoneNumber(signupData.getPhoneNumber());
        user.setPhoneCode(resolvedPhoneCode);
        user.setCountry(""); // Can be enhanced to detect country from phone number
        user.setRoles(roles);
        user.setUserType(UserType.CLIENT_ADMIN.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setCreatedBy("TRIAL_SIGNUP");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setCompanyName(signupData.getCompanyName());
        user.setRiskGroup(RiskGroup.HIGH_RISK);

        user.setIsTrial(Boolean.TRUE);
        user.setTrialStartDate(now);
        user.setTrialEndDate(now.plusSeconds(trialPeriodDays * 24L * 60 * 60)); // Trial period in days
        user.setIsDefault(Boolean.FALSE); // User set their own password — do not force reset on first login

        // Persist AspireUser using repository
        AspireUser aspireUser = aspireUserRepository.save(user);

        // Create ClientAdmin record for trial user
        String clientAdminId = aspireUser.getUserId().toString();
        String mspId = DefaultMspData.getDefaultMspId();
        
        // Check if ClientAdmin already exists (shouldn't happen, but safety check)
        if (clientAdminRepository.existsByEmail(signupData.getEmail())) {
            log.warn("ClientAdmin already exists for email: {}, skipping creation", signupData.getEmail());
            // Fetch existing ClientAdmin to get mspId
            Optional<ClientAdmin> existingClientAdmin = clientAdminRepository.findById(clientAdminId);
            if (existingClientAdmin.isPresent()) {
                mspId = existingClientAdmin.get().getMspId();
            }
        } else {
            ClientAdmin clientAdmin = ClientAdmin.builder()
                    .id(clientAdminId)
                    .email(signupData.getEmail())
                    .mspId(mspId)
                    .hashedPassword(hashedPassword) // Store hashed password
                    .organizationName(signupData.getCompanyName())
                    .contactEmail(signupData.getEmail())
                    .phoneNumber(signupData.getPhoneNumber())
                    .phoneCode(resolvePhoneCode(signupData.getPhoneCode(), signupData.getPhoneNumber()))
                    .country("") // Can be enhanced to detect country from phone number
                    .organizationSize(signupData.getNumberOfEmployees())
                    .status(AdminStatus.ACTIVE) // Trial users are active
                    .createdAt(Instant.now())
                    .clientAdminId(clientAdminId) // Set clientAdminId to same as id
                    .onboardBy(OnboardBy.TRIAL) // Mark as trial onboarding
                    .build();

            clientAdminRepository.save(clientAdmin);
            log.info("ClientAdmin record created successfully with ID: {}", clientAdminId);
        }

        // Update AspireUser with clientAdminId and mspId
        aspireUser.setClientAdminId(clientAdminId);
        aspireUser.setMspId(mspId);
        aspireUser.setUpdatedAt(Instant.now());
        aspireUserRepository.save(aspireUser);
        log.info("Updated AspireUser with clientAdminId: {} and mspId: {}", clientAdminId, mspId);

        // Assign trial products and packages to the user
        boolean legacySingleProduct = isLegacySingleProductRequest(request, signupData);
        List<ProductsData> productsToAssign = resolveProductsForAssignment(request, signupData);
        if (!legacySingleProduct) {
            rejectIfAnyProductAlreadyAssigned(clientAdminId, productsToAssign);
        }
        assignTrialProductsToClient(clientAdminId, productsToAssign, aspireUser.getUserId().toString());

        // Clean up temporary data
        signupDataStorage.remove(emailKey);
        verifiedEmails.remove(emailKey);

        log.info("Trial user created successfully with ID: {}", aspireUser.getUserId());

        // Get MSP name and email - handle default MSP case
        String mspName;
        String mspEmail;
        if (DefaultMspData.DEFAULT_MSP_ID.equals(mspId)) {
            // Use default MSP data when mspId is "ASPIRE-DEFAULT"
            mspName = DefaultMspData.getDefaultMspName();
            mspEmail = DefaultMspData.getDefaultMspEmail();
        } else {
            // Try to parse mspId as UUID and find AspireUser
            try {
                UUID mspUUID = UUID.fromString(mspId);
                Optional<AspireUser> mspAspireUser = aspireUserRepository.findByUserId(mspUUID);
                mspName = mspAspireUser.isPresent() ? mspAspireUser.get().getCompanyName() : "";
                mspEmail = mspAspireUser.isPresent() ? mspAspireUser.get().getEmail() : "";
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format for mspId: {}, using empty values", mspId);
                mspName = DefaultMspData.getDefaultMspName();
                mspEmail = DefaultMspData.getDefaultMspEmail();
            }
        }
        String fullName = (aspireUser.getFirstName() != null ? aspireUser.getFirstName() : "") + " " + (aspireUser.getLastName() != null ? aspireUser.getLastName() : "");
        // Send welcome email
        registrationNotificationClient.sendWelcomeClientEmailWithoutPasswordNotification(aspireUser.getEmail(), aspireUser.getUserId().toString(),  clientAdminId, fullName, mspName, mspEmail);

        // Build response
        return TrialSignupResponseDto.builder()
                .trialPackage(legacySingleProduct ? "Security Awareness Training" : toTrialPackageNames(productsToAssign))
                .trialPeriod(trialPeriodDays + " Days")
                .licenses("Up to 5 users")
                .fullName(signupData.getFirstName() + " " + signupData.getLastName())
                .email(signupData.getEmail())
                .companyName(signupData.getCompanyName())
                .userId(aspireUser.getUserId().toString())
                .productsData(legacySingleProduct ? null : toNewProductFlags(productsToAssign))
                .build();
    }

    @Override
    @Transactional
    public TrialSignupResponseDto createBuyNowPassword(BuyNowPasswordCreationRequestDto request) {
        log.info("Creating password and completing buy now signup for: {}", request.getEmail());

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Retrieve buy-now signup data
        String emailKey = request.getEmail().toLowerCase();
        BuyNowSignupRequestDto signupData = buyNowSignupDataStorage.get(emailKey);
        
        if (signupData == null) {
            throw new ResourceNotFoundException("Signup data not found. Please start the signup process again.");
        }

        // Validate email matches the one used in signup
        if (!signupData.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email does not match the one used during signup");
        }

        // Check if email was verified
        if (!buyNowVerifiedEmails.contains(emailKey)) {
            throw new IllegalArgumentException("Please verify your email first before creating password");
        }

        // Check if user already exists
        if (aspireUserRepository.existsByUsernameIgnoreCase(request.getEmail().trim())) {
            buyNowSignupDataStorage.remove(emailKey);
            throw new ResourceAlreadyExistsException("User with username/email " + request.getEmail() + " already exists");
        }

        // Enforce password policy (buy-now signup has no first/last name, use email only)
        PasswordValidationContext buyNowContext = PasswordValidationContext.builder()
                .email(signupData.getEmail())
                .firstName("")
                .lastName("")
                .build();
        PasswordValidationResult buyNowValidation = PasswordValidator.validate(request.getPassword(), buyNowContext);
        if (!buyNowValidation.isValid()) {
            buyNowSignupDataStorage.remove(emailKey);
            throw new IllegalArgumentException(buyNowValidation.getCombinedMessage());
        }

        // Encrypt password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Get role IDs for CLIENT_ADMIN
        List<String> roles = convertRoleNamesToIds(List.of(UserType.CLIENT_ADMIN.name()));

        // Create AspireUser with empty firstName and lastName and persist via repository
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        AspireUser user = new AspireUser();
        user.setId(userId);
        user.setUserId(userId);
        user.setFirstName(""); // Empty as per requirement
        user.setLastName(""); // Empty as per requirement
        user.setEmail(signupData.getEmail());
        user.setUsername(signupData.getEmail());
        user.setPassword(hashedPassword);
        String resolvedPhoneCode = resolvePhoneCode(signupData.getPhoneCode(), signupData.getPhoneNumber());
        user.setPhoneNumber(signupData.getPhoneNumber());
        user.setPhoneCode(resolvedPhoneCode);
        user.setCountry(""); // Can be enhanced to detect country from phone number
        user.setRoles(roles);
        user.setUserType(UserType.CLIENT_ADMIN.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setCreatedBy("BUY_NOW_SIGNUP");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setCompanyName(signupData.getCompanyName());
        user.setRiskGroup(RiskGroup.HIGH_RISK);
        user.setIsBuyNow(Boolean.TRUE); // Set buy-now flag
        user.setIsDefault(Boolean.FALSE); // User set their own password — do not force reset on first login

        // Persist AspireUser using repository
        AspireUser aspireUser = aspireUserRepository.save(user);

        // Create ClientAdmin record
        String clientAdminId = aspireUser.getUserId().toString();
        String mspId = DefaultMspData.getDefaultMspId();
        
        // Check if ClientAdmin already exists (shouldn't happen, but safety check)
        if (clientAdminRepository.existsByEmail(signupData.getEmail())) {
            log.warn("ClientAdmin already exists for email: {}, skipping creation", signupData.getEmail());
            // Fetch existing ClientAdmin to get mspId
            Optional<ClientAdmin> existingClientAdmin = clientAdminRepository.findById(clientAdminId);
            if (existingClientAdmin.isPresent()) {
                mspId = existingClientAdmin.get().getMspId();
            }
        } else {
            ClientAdmin clientAdmin = ClientAdmin.builder()
                    .id(clientAdminId)
                    .email(signupData.getEmail())
                    .mspId(mspId)
                    .hashedPassword(hashedPassword) // Store hashed password
                    .organizationName(signupData.getCompanyName())
                    .contactEmail(signupData.getEmail())
                    .phoneNumber(signupData.getPhoneNumber())
                    .phoneCode(resolvePhoneCode(signupData.getPhoneCode(), signupData.getPhoneNumber()))
                    .country("") // Can be enhanced to detect country from phone number
                    .organizationSize(signupData.getNumberOfEmployees())
                    .status(AdminStatus.PENDING)
                    .createdAt(Instant.now())
                    .clientAdminId(clientAdminId) // Set clientAdminId to same as id
                    .onboardBy(OnboardBy.BUY_NOW) // Mark as buy-now onboarding
                    .build();

            // Save selected product/package IDs for tracking only (no ClientProduct created)
            if (request.getProductId() != null && !request.getProductId().isBlank()
                    || request.getPackageId() != null && !request.getPackageId().isBlank()) {
                clientAdmin.setSelectedProductId(request.getProductId());
                clientAdmin.setSelectedProductName(request.getProductName());
                clientAdmin.setSelectedPackageId(request.getPackageId());
                clientAdmin.setSelectedPackageName(request.getPackageName());
                clientAdmin.setSelectedUserRangeId(request.getUserRangeId());
                log.info("Saving selected product/package for tracking: productId={}, packageId={}", request.getProductId(), request.getPackageId());
            }

            clientAdminRepository.save(clientAdmin);
            log.info("ClientAdmin record created successfully with ID: {}", clientAdminId);
        }

        // Update AspireUser with clientAdminId and mspId
        aspireUser.setClientAdminId(clientAdminId);
        aspireUser.setMspId(mspId);
        aspireUser.setUpdatedAt(Instant.now());
        aspireUserRepository.save(aspireUser);
        log.info("Updated AspireUser with clientAdminId: {} and mspId: {}", clientAdminId, mspId);

        // Clean up temporary data
        buyNowSignupDataStorage.remove(emailKey);
        buyNowVerifiedEmails.remove(emailKey);
        log.info("Buy now user created successfully with ID: {}", aspireUser.getUserId());

        // Get MSP name and email - handle default MSP case
        String mspName;
        String mspEmail;
        if (DefaultMspData.DEFAULT_MSP_ID.equals(mspId)) {
            // Use default MSP data when mspId is "ASPIRE-DEFAULT"
            mspName = DefaultMspData.getDefaultMspName();
            mspEmail = DefaultMspData.getDefaultMspEmail();
        } else {
            // Try to parse mspId as UUID and find AspireUser
            try {
                UUID mspUUID = UUID.fromString(mspId);
                Optional<AspireUser> mspAspireUser = aspireUserRepository.findByUserId(mspUUID);
                mspName = mspAspireUser.isPresent() ? mspAspireUser.get().getCompanyName() : "";
                mspEmail = mspAspireUser.isPresent() ? mspAspireUser.get().getEmail() : "";
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format for mspId: {}, using empty values", mspId);
                mspName = DefaultMspData.getDefaultMspName();
                mspEmail = DefaultMspData.getDefaultMspEmail();
            }
        }


        // Send welcome email
        registrationNotificationClient.sendWelcomeClientEmailWithoutPasswordNotification(aspireUser.getEmail(), aspireUser.getUserId().toString(),  clientAdminId, signupData.getCompanyName(), mspName, mspEmail);

        // Build response
        return TrialSignupResponseDto.builder()
                .trialPackage("Buy Now Account")
                .trialPeriod("N/A")
                .licenses("N/A")
                .fullName("") // Empty as firstName and lastName are empty
                .email(signupData.getEmail())
                .companyName(signupData.getCompanyName())
                .userId(aspireUser.getUserId().toString())
                .build();
    }

    /**
     * Assigns trial products and packages to the user
     * Creates ClientProduct and EndUserPackage records. For trial SubPackage creation calls CMS service.
     */
    private void assignTrialProductsToClient(String clientAdminId, List<ProductsData> productsData, String userId) {
        for (ProductsData product : productsData) {
            assignTrialProductAndPackage(clientAdminId, product.getProductId(), product.getPackageId(), userId);
        }
    }

    private void assignTrialProductAndPackage(String clientAdminId, String productId, String packageId, String userId) {
        log.info("Assigning trial product {} and package {} to user {}", productId, packageId, userId);

        try {
            ClientProduct clientProduct = createTrialClientProduct(clientAdminId, productId, packageId);
            log.info("Created ClientProduct record for trial with ID: {}", clientProduct.getId());

            updateClientAdminWithProductIds(clientAdminId, clientProduct.getId());

            EndUserPackage endUserPackage = createTrialEndUserPackage(productId, packageId, userId, clientAdminId);
            log.info("Created EndUserPackage record for trial with ID: {}", endUserPackage.getId());

            updateTrialClientDashboardInCmsService(clientAdminId);

            String trialSubPackageId = createTrialSubPackage(productId, packageId, clientAdminId, clientProduct.getId());
            if (trialSubPackageId == null || trialSubPackageId.trim().isEmpty()) {
                throw new RegistrationServiceException("Failed to create trial subPackage");
            }

            log.info("Successfully assigned trial product and package to user {}", userId);
        } catch (RegistrationServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error assigning trial product and package to user {}: {}", userId, e.getMessage(), e);
            throw new RegistrationServiceException("Failed to assign trial product and package: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a trial subPackage in CMS service
     * @param productId The product ID
     * @param packageId The package ID (ProductPackage ID)
     * @param clientAdminId The client admin ID (used as clientId)
     * @return The created subPackage ID, or null if creation failed
     */
    private String createTrialSubPackage(String productId, String packageId, String clientAdminId, String productPackageId) {
        try {
            log.info("Creating trial subPackage for productId: {}, packageId: {}, clientAdminId: {}", 
                    productId, packageId, clientAdminId);

            // Create trial subPackage request
            TrialSubPackageCreationRequestDto request = TrialSubPackageCreationRequestDto.builder()
                    .productId(productId)
                    .packageId(packageId)
                    .clientAdminId(clientAdminId)
                    .productPackageId(productPackageId)
                    .trialPeriodDays(trialPeriodDays)
                    .build();

            // Call CMS service to create trial subPackage
            SubPackageResponseDto subPackageResponse = cmsServiceClient.createTrialSubPackage(request);
            
            if (subPackageResponse != null && subPackageResponse.getId() != null) {
                log.info("Successfully created trial subPackage with ID: {}, name: {}", 
                        subPackageResponse.getId(), subPackageResponse.getName());
                return subPackageResponse.getId();
            } else {
                log.error("Failed to create trial subPackage. Response was null or missing ID");
                return null;
            }

        } catch (Exception e) {
            log.error("Error creating trial subPackage for productId: {}, packageId: {}, clientAdminId: {}: {}", 
                    productId, packageId, clientAdminId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a ClientProduct record for the trial
     */
    private ClientProduct createTrialClientProduct(String clientAdminId, String productId, String packageId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(trialPeriodDays * 24L * 60 * 60); // Trial period in days

        ClientProduct clientProduct = new ClientProduct();
        clientProduct.setId(UUID.randomUUID().toString());
        clientProduct.setClientAdminId(clientAdminId);
        clientProduct.setProductId(productId);
        clientProduct.setPackageId(packageId);
        clientProduct.setLicenseCount(5); // Trial typically has 5 license
        clientProduct.setPricePerLicense(0.0); // Trial is free
        clientProduct.setTotalPrice(0.0); // Trial is free
        clientProduct.setValidityPeriod(trialPeriodDays);
        clientProduct.setValidityUnit("DAYS");
        clientProduct.setAssignedAt(now);
        clientProduct.setExpiryDate(expiryDate);
        clientProduct.setLicenseStatus("ACTIVE"); // Trial products are active immediately

        Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
        clientAdminOpt.ifPresent(clientAdmin -> {
            clientProduct.setCountryId(clientAdmin.getCountry());
            clientProduct.setMspId(clientAdmin.getMspId());
        });

        clientProductRepository.save(clientProduct);
        return clientProduct;
    }

    private EndUserPackage createTrialEndUserPackage(String productId, String packageId, String userId, String clientAdminId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(trialPeriodDays * 24L * 60 * 60); // Trial period in days

        EndUserPackage endUserPackage = new EndUserPackage();
        endUserPackage.setId(UUID.randomUUID().toString());
        endUserPackage.setUserId(userId);
        endUserPackage.setClientAdminId(clientAdminId);
        endUserPackage.setProductId(productId);
        endUserPackage.setSubPackageId(packageId);
        endUserPackage.setStatus(PackageStatus.ASSIGNED.name());
        endUserPackage.setProgress(0.0);
        endUserPackage.setAssignedAt(now);
        endUserPackage.setExpiryDate(expiryDate);
        endUserPackage.setActive(Boolean.TRUE);
        endUserPackage.setEnableFirstUserNotificationEmail(false);

        endUserPackageRepository.save(endUserPackage);
        return endUserPackage;
    }

    /**
     * Updates ClientAdmin record with clientProductIds
     */
    private void updateClientAdminWithProductIds(String clientAdminId, String clientProductId) {
        Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
        if (clientAdminOpt.isPresent()) {
            ClientAdmin clientAdmin = clientAdminOpt.get();
            List<String> productIds = clientAdmin.getClientProductIds();
            if (productIds == null) {
                productIds = new ArrayList<>();
            }
            if (!productIds.contains(clientProductId)) {
                productIds.add(clientProductId);
                clientAdmin.setClientProductIds(productIds);
                clientAdminRepository.save(clientAdmin);
                log.info("Updated ClientAdmin {} with product ID: {}", clientAdminId, clientProductId);
            }
        } else {
            log.warn("ClientAdmin not found with ID: {}", clientAdminId);
        }
    }

    /**
     * Updates client dashboard in CMS service for trial client admin
     * Similar to ClientAdminServiceImpl.updateClientDashboardInCmsService
     */
    private void updateTrialClientDashboardInCmsService(String clientAdminId) {
        log.info("Updating client dashboard in CMS service for trial clientAdminId: {}", clientAdminId);

        try {
            // 1. Get unique product count
            int totalProduct = getUniqueProductCountByClientAdminId(clientAdminId);

            // 2. Get license statistics
            int totalLicense = getLicenseStatisticsForTrial(clientAdminId);

            // 3. Set topic and certificate counts to 0
            int totalTopic = 0;
            int totalCertificate = 0;

            // 4. Get ClientAdmin email
            Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
            String email = clientAdminOpt.map(ClientAdmin::getEmail).orElse(null);

            // 5. Get ClientProduct data
            List<ClientProduct> clientProducts = clientProductRepository.findByClientAdminId(clientAdminId);
            List<ClientProductData> clientProductDataList = 
                    clientProducts.stream()
                            .map(cp -> ClientProductData.builder()
                                    .clientAdminId(cp.getClientAdminId())
                                    .productId(cp.getProductId())
                                    .packageId(cp.getPackageId())
                                    .assignedAt(cp.getAssignedAt())
                                    .expiryDate(cp.getExpiryDate())
                                    .email(email)
                                    .build())
                            .toList();

            // 6. Create dashboard request
            ClientDashboardRequestDto dashboardRequest = ClientDashboardRequestDto.builder()
                    .clientAdminId(clientAdminId)
                    .totalProduct(totalProduct)
                    .totalLicense(totalLicense)
                    .totalTopic(totalTopic)
                    .totalCertificate(totalCertificate)
                    .clientProductData(clientProductDataList)
                    .build();

            log.info("Dashboard data for trial clientAdminId {}: products={}, licenses={}, topics={}, certificates={}, clientProductData size={}",
                    clientAdminId, totalProduct, totalLicense, totalTopic, totalCertificate, clientProductDataList.size());

            // 7. Call CMS service
            webClient.post()
                    .uri(cmsServiceUrl + "/client-dashboard")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(dashboardRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Successfully updated client dashboard in CMS service for trial clientAdminId: {}", clientAdminId);

        } catch (WebClientResponseException e) {
            log.error("CMS service returned error for dashboard update: Status={}, Response={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            // Don't throw exception - dashboard update failure shouldn't rollback user creation
        } catch (Exception e) {
            log.error("Failed to update client dashboard in CMS service for trial clientAdminId: {}", clientAdminId, e);
            // Don't throw exception - dashboard update failure shouldn't rollback user creation
        }
    }

    /**
     * Gets unique product count for client admin (simplified version for trial)
     */
    private int getUniqueProductCountByClientAdminId(String clientAdminId) {
        try {
            int uniqueProductCount = clientProductRepositoryCustom.getUniqueProductCountByClientAdminId(clientAdminId);
            log.info("Unique product count for trial client admin {}: {}", clientAdminId, uniqueProductCount);
            return uniqueProductCount;
        } catch (Exception e) {
            log.error("Error retrieving unique product count for trial client admin: {}", clientAdminId, e);
            // Return 1 as default for trial (should have at least 1 product)
            return 1;
        }
    }

    /**
     * Gets license statistics for trial client admin (simplified version)
     */
    private int getLicenseStatisticsForTrial(String clientAdminId) {
        try {
            ClientProductRepositoryCustom.LicenseStatistics statistics =
                    clientProductRepositoryCustom.getLicenseStatisticsByClientAdminId(clientAdminId);
            int totalLicenseCount = statistics.getTotalLicenseCount();
            log.info("License count for trial client admin {}: {}", clientAdminId, totalLicenseCount);
            return totalLicenseCount;
        } catch (Exception e) {
            log.error("Error retrieving license statistics for trial client admin: {}", clientAdminId, e);
            // Return 1 as default for trial (should have at least 1 license)
            return 1;
        }
    }

    @Override
    @Transactional
    public EmailVerificationResponseDto resendVerificationCode(String email) {
        log.info("Resending verification code to: {}", email);

        String emailKey = email.toLowerCase();
        TrialSignupRequestDto signupData = signupDataStorage.get(emailKey);
        BuyNowSignupRequestDto buyNowSignupData = buyNowSignupDataStorage.get(emailKey);

        if (signupData == null && buyNowSignupData == null) {
            throw new ResourceNotFoundException("No signup data found. Please start the signup process again.");
        }

        // If email was already verified, remove from verified set (user might want to verify again)
        verifiedEmails.remove(emailKey);
        buyNowVerifiedEmails.remove(emailKey);

        // Generate new OTP (replaces any existing OTP)
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(otpValiditySeconds);
        
        otpStorage.put(emailKey, new OtpData(otp, expiresAt));

        // Send verification email - use firstName from trial signup if available, otherwise use email
        String firstName = (signupData != null && !signupData.getFirstName().isBlank()) ? signupData.getFirstName() : signupData.getCompanyName();
        sendVerificationEmail(email, otp, firstName);

        log.info("Verification code resent to: {}", email);

        return EmailVerificationResponseDto.builder()
                .verified(false)
                .message("Verification code resent to your email")
                .email(email)
                .nextDirection(NextDirection.VERIFY)
                .build();
    }

    private String generateOtp() {
        int min = 100000;
        int max = 999999;
        int otp = secureRandom.nextInt(max - min + 1) + min;
        return String.valueOf(otp);
    }

    private void sendVerificationEmail(String email, String otp, String firstName) {
        try {
            logOtpForLocalVerification(email, otp);

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("otp", otp);
            String userName = firstName == null ? "" : firstName;
            if (!userName.isEmpty()) {
                userName = " " + userName;
            } else {
                userName = "" ;
            }
            templateModel.put("userName", userName);
            templateModel.put("expiryMinutes", otpValiditySeconds / 60);

            // Use sendEmailNotification which doesn't require userId
            boolean sent = notificationClient.sendEmailNotification(
                    email,
                    NotificationType.SECURITY_ALERTS,
                    templateModel
            );
            
            if (sent) {
                log.info("Verification email sent successfully to: {}", email);
            } else {
                log.error("Failed to send verification email to: {}", email);
            }
        } catch (Exception e) {
            log.error("Error sending verification email to {}: {}", email, e.getMessage(), e);
            // Don't throw exception - OTP is already saved, user can request another one
        }
    }

    private void logOtpForLocalVerification(String email, String otp) {
        if (otpConsoleEnabled) {
            log.warn("LOCAL SIGNUP EMAIL OTP for {}: {}", email, otp);
        }
    }

    private List<String> convertRoleNamesToIds(List<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> {
                    var role = roleRepository.findByRoleName(roleName);
                    return role != null ? role.getId() : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private String resolvePhoneCode(String phoneCode, String phoneNumber) {
        if (phoneCode != null && !phoneCode.isBlank()) {
            return phoneCode.trim();
        }
        return extractPhoneCodeFromNumber(phoneNumber);
    }

    private String extractPhoneCodeFromNumber(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.startsWith("+")) {
            int spaceIndex = phoneNumber.indexOf(" ");
            if (spaceIndex > 0) {
                return phoneNumber.substring(0, spaceIndex);
            }
        }
        return "";
    }

    /**
     * Extracts the domain from an email address
     */
    private String extractDomainFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        String domain = email.substring(atIndex + 1).trim();
        if (domain.isEmpty()) {
            throw new IllegalArgumentException("Invalid email format: domain is empty");
        }

        return domain.toLowerCase();
    }

    /**
     * Gets existing admins and users for a given email domain
     */
    private ExistingUsersResponseDto getExistingUsersByDomain(String domain) {
        List<ExistingAdminUserInfoDto> existingAdmins = new ArrayList<>();
        List<ExistingAdminUserInfoDto> existingUsers = new ArrayList<>();

        try {
            // Find all ClientAdmins with emails matching the domain
            String domainPattern = "@" + domain.replace(".", "\\.") + "$";
            List<ClientAdmin> clientAdmins = clientAdminRepository.findByEmailDomainRegex(domainPattern);
            
            for (ClientAdmin admin : clientAdmins) {
                ExistingAdminUserInfoDto adminInfo = ExistingAdminUserInfoDto.builder()
                        .name(maskEmail(admin.getEmail())) // Use masked email as name
                        .email(maskEmail(admin.getEmail()))
                        .phoneNumber(maskPhoneNumber(admin.getPhoneNumber()))
                        .createdAt(admin.getCreatedAt())
                        .build();
                existingAdmins.add(adminInfo);

                // Get all users for this client admin
                List<AspireUserDto> users =
                        aspireUserService.getUsersByTypeAndClientAdminId(UserType.USER.name(), admin.getId());
                
                for (AspireUserDto user : users) {
                    String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + 
                                     " " + (user.getLastName() != null ? user.getLastName() : "");
                    // If name is empty or just spaces, use masked email
                    String displayName = (fullName.trim().isEmpty()) ? maskEmail(user.getEmail()) : fullName.trim();
                    
                    ExistingAdminUserInfoDto userInfo = ExistingAdminUserInfoDto.builder()
                            .name(displayName)
                            .email(maskEmail(user.getEmail()))
                            .phoneNumber(maskPhoneNumber(user.getPhoneNumber()))
                            .createdAt(user.getCreatedAt())
                            .build();
                    existingUsers.add(userInfo);
                }
            }

            boolean hasExistingUsers = !existingAdmins.isEmpty() || !existingUsers.isEmpty();
            
            return ExistingUsersResponseDto.builder()
                    .hasExistingUsers(hasExistingUsers)
                    .existingAdmins(existingAdmins)
                    .existingUsers(existingUsers)
                    .message(hasExistingUsers ? 
                            "A free trial for this domain was activated within the last 6 months, so new trial requests aren’t available. Please use the Need Help? or Contact Support button below to explore next steps." : 
                            "No existing accounts found.")
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving existing users for domain: {}", domain, e);
            return ExistingUsersResponseDto.builder()
                    .hasExistingUsers(false)
                    .existingAdmins(List.of())
                    .existingUsers(List.of())
                    .message("Error retrieving existing account information.")
                    .build();
        }
    }

    /**
     * Masks email address for display (e.g., "e1qtestdlkm@yopmail.com" -> "e1q*****lkm@yopmail.com")
     */
    private String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }
        
        email = email.trim();
        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex < 3) {
            // If no @ or too short, mask the whole thing
            return email.length() > 3 ? email.substring(0, 3) + "*****" : "*****";
        }
        
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        
        if (localPart.length() <= 6) {
            // If local part is 6 chars or less, show first 3 and mask the rest
            return localPart.substring(0, Math.min(3, localPart.length())) + "*****" + domainPart;
        } else {
            // Show first 3 chars, mask middle, show last 3 chars before @
            String firstPart = localPart.substring(0, 3);
            String lastPart = localPart.substring(localPart.length() - 3);
            return firstPart + "*****" + lastPart + domainPart;
        }
    }

    /**
     * Masks phone number for display (e.g., "10871234577" -> "1087****77")
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "";
        }
        
        // Remove spaces, dashes, parentheses, and plus signs for masking
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\+]", "");
        
        if (cleaned.length() <= 6) {
            // If too short, mask most of it
            return cleaned.length() > 2 ? cleaned.substring(0, 2) + "*****" : "*****";
        } else if (cleaned.length() <= 10) {
            // Show first 4, mask rest except last 2
            return cleaned.substring(0, 4) + "****" + cleaned.substring(cleaned.length() - 2);
        } else {
            // For longer numbers, show first 4 and last 2
            return cleaned.substring(0, 4) + "****" + cleaned.substring(cleaned.length() - 2);
        }
    }

    /**
     * Sends email notification to Aspire Admin about domain conflict during trial signup
     */
    private void sendDomainConflictNotification(TrialSignupRequestDto request, String domain, 
                                                ExistingUsersResponseDto existingUsersInfo) {
        try {
            log.info("Sending domain conflict notification to Aspire Admin: {}", aspireAdminEmail);

            // Build email content
            StringBuilder existingUsersContent = new StringBuilder();
            
            if (!existingUsersInfo.getExistingAdmins().isEmpty()) {
                existingUsersContent.append("\n\nExisting Admins:\n");
                for (ExistingAdminUserInfoDto admin : existingUsersInfo.getExistingAdmins()) {
                    existingUsersContent.append(String.format("- Name: %s, Email: %s, Phone: %s, Created: %s\n",
                            admin.getName(), admin.getEmail(), admin.getPhoneNumber(), admin.getCreatedAt()));
                }
            }
            
            if (!existingUsersInfo.getExistingUsers().isEmpty()) {
                existingUsersContent.append("\n\nExisting Users:\n");
                for (ExistingAdminUserInfoDto user : existingUsersInfo.getExistingUsers()) {
                    existingUsersContent.append(String.format("- Name: %s, Email: %s, Phone: %s, Created: %s\n",
                            user.getName(), user.getEmail(), user.getPhoneNumber(), user.getCreatedAt()));
                }
            }

            Map<String, Object> templateModel = new HashMap<>();

            String userName = request.getFirstName();
            if( !userName.isEmpty()){
                userName = " " + userName;
            } else {
                userName = "" ;
            }
            templateModel.put("userName", userName);
            templateModel.put("attemptedEmail", request.getEmail());
            templateModel.put("attemptedName", request.getFirstName() + " " + request.getLastName());
            templateModel.put("attemptedPhone", request.getPhoneNumber());
            templateModel.put("attemptedCompany", request.getCompanyName());
            templateModel.put("attemptedDomain", domain);
            templateModel.put("attemptDate", Instant.now().toString());
            templateModel.put("existingAdminsCount", existingUsersInfo.getExistingAdmins().size());
            templateModel.put("existingUsersCount", existingUsersInfo.getExistingUsers().size());
            templateModel.put("existingUsersDetails", existingUsersContent.toString());

            // Send email notification
            boolean sent = notificationClient.sendEmailNotification(
                    aspireAdminEmail,
                    NotificationType.SECURITY_ALERTS,
                    templateModel
            );

            if (sent) {
                log.info("Domain conflict notification sent successfully to Aspire Admin");
            } else {
                log.error("Failed to send domain conflict notification to Aspire Admin");
            }
        } catch (Exception e) {
            log.error("Error sending domain conflict notification to Aspire Admin: {}", e.getMessage(), e);
            // Don't throw exception - notification failure shouldn't block the signup process
        }
    }

    @Override
    @Transactional
    public TrialSignupResponseDto assignTrialProducts(AssignTrialProductsRequestDto request) {
        log.info("Assigning additional trial products for existing account: {}", request.getEmail());
        validateProductsData(request.getProductsData());

        AspireUser existingUser = aspireUserRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trial account found for email " + request.getEmail()));


        String clientAdminId = resolveClientAdminId(existingUser);
        ClientAdmin clientAdmin = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trial account found for email " + request.getEmail()));

        if (clientAdmin.getOnboardBy() != OnboardBy.TRIAL) {
            throw new ResourceNotFoundException(
                    "No trial account found for email " + request.getEmail());
        }

        List<ProductsDataResponse> productFlags = buildExistingTrailFlags(clientAdminId, request.getProductsData());
        List<ProductsData> productsToAssign = request.getProductsData().stream()
                .filter(product -> !isProductAlreadyAssigned(clientAdminId, product.getProductId(), product.getPackageId()))
                .toList();

        if (productsToAssign.isEmpty()) {
            throw new ResourceAlreadyExistsException(
                    "You already have a trial account for this product and package");
        }

        String userId = existingUser.getUserId() != null
                ? existingUser.getUserId().toString()
                : existingUser.getId().toString();
        assignTrialProductsToClient(clientAdminId, productsToAssign, userId);

        String fullName = ((existingUser.getFirstName() != null ? existingUser.getFirstName() : "") + " "
                + (existingUser.getLastName() != null ? existingUser.getLastName() : "")).trim();

        return TrialSignupResponseDto.builder()
                .trialPackage(toTrialPackageNames(productsToAssign))
                .trialPeriod(trialPeriodDays + " Days")
                .licenses("Up to 5 users")
                .fullName(fullName)
                .email(existingUser.getEmail())
                .companyName(existingUser.getCompanyName())
                .userId(userId)
                .productsData(productFlags)
                .build();
    }

    private EmailVerificationResponseDto sendTrialVerificationCode(
            TrialSignupRequestDto request, List<ProductsDataResponse> productsData, NextDirection nextDirection) {
        String emailKey = request.getEmail().toLowerCase();
        verifiedEmails.remove(emailKey);
        otpStorage.remove(emailKey);
        signupDataStorage.put(emailKey, request);

        String otp = generateOtp();
        Instant expiresAt = Instant.now().plusSeconds(otpValiditySeconds);
        otpStorage.put(emailKey, new OtpData(otp, expiresAt));

        String firstName = request.getFirstName() != null ? request.getFirstName() : request.getCompanyName();
        sendVerificationEmail(request.getEmail(), otp, firstName);
        log.info("Verification code sent to: {}", request.getEmail());

        return EmailVerificationResponseDto.builder()
                .verified(false)
                .message("Verification code sent to your email")
                .email(request.getEmail())
                .productsData(productsData)
                .nextDirection(nextDirection)
                .build();
    }

    private void validateProductsData(List<ProductsData> productsData) {
        if (productsData == null || productsData.isEmpty()) {
            throw new IllegalArgumentException("At least one product is required");
        }
        for (ProductsData product : productsData) {
            if (product == null
                    || product.getProductId() == null || product.getProductId().isBlank()
                    || product.getPackageId() == null || product.getPackageId().isBlank()) {
                throw new IllegalArgumentException("Each product must include productId and packageId");
            }
        }
    }

    private List<ProductsData> resolveProductsForAssignment(
            PasswordCreationRequestDto request, TrialSignupRequestDto signupData) {
        if (hasProductsData(request.getProductsData())) {
            validateProductsData(request.getProductsData());
            return request.getProductsData();
        }
        if (hasProductsData(signupData.getProductsData())) {
            validateProductsData(signupData.getProductsData());
            return signupData.getProductsData();
        }
        if (hasText(request.getProductId()) && hasText(request.getSubPackageId())) {
            return List.of(ProductsData.builder()
                    .productId(request.getProductId())
                    .packageId(request.getSubPackageId())
                    .productName(request.getProductName())
                    .packageName(request.getSubPackageName())
                    .build());
        }
        throw new IllegalArgumentException("At least one product is required");
    }

    private boolean isLegacySingleProductRequest(
            PasswordCreationRequestDto request, TrialSignupRequestDto signupData) {
        return !hasProductsData(request.getProductsData()) && !hasProductsData(signupData.getProductsData());
    }

    private boolean hasProductsData(List<ProductsData> productsData) {
        return productsData != null && !productsData.isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void rejectIfAnyProductAlreadyAssigned(String clientAdminId, List<ProductsData> productsData) {
        boolean alreadyAssigned = productsData.stream()
                .anyMatch(product -> isProductAlreadyAssigned(clientAdminId, product.getProductId(), product.getPackageId()));
        if (alreadyAssigned) {
            throw new ResourceAlreadyExistsException(
                    "You already have a trial account for this product and package");
        }
    }

    private List<ProductsDataResponse> buildExistingTrailFlags(String clientAdminId, List<ProductsData> productsData) {
        return productsData.stream()
                .map(product -> ProductsDataResponse.builder()
                        .productId(product.getProductId())
                        .packageId(product.getPackageId())
                        .existingTrail(isProductAlreadyAssigned(clientAdminId, product.getProductId(), product.getPackageId()))
                        .build())
                .toList();
    }

    private List<ProductsDataResponse> toNewProductFlags(List<ProductsData> productsData) {
        return productsData.stream()
                .map(product -> ProductsDataResponse.builder()
                        .productId(product.getProductId())
                        .packageId(product.getPackageId())
                        .existingTrail(false)
                        .build())
                .toList();
    }

    private boolean hasAnyExistingTrail(List<ProductsDataResponse> productsData) {
        return productsData.stream().anyMatch(product -> Boolean.TRUE.equals(product.getExistingTrail()));
    }

    private boolean isProductAlreadyAssigned(String clientAdminId, String productId, String packageId) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return false;
        }
        ClientProduct existing = clientProductRepository
                .findByClientAdminIdAndProductIdAndPackageId(clientAdminId, productId, packageId);
        if (existing == null) {
            return false;
        }
        String status = existing.getLicenseStatus();
        return "ACTIVE".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status);
    }

    private String resolveClientAdminId(AspireUser user) {
        if (user.getClientAdminId() != null && !user.getClientAdminId().isBlank()) {
            return user.getClientAdminId();
        }
        if (user.getUserId() != null) {
            return user.getUserId().toString();
        }
        return user.getId() != null ? user.getId().toString() : "";
    }

    private String toTrialPackageNames(List<ProductsData> productsData) {
        return productsData.stream()
                .map(product -> {
                    if (product.getProductName() != null && !product.getProductName().isBlank()) {
                        return product.getProductName();
                    }
                    return product.getProductId();
                })
                .collect(Collectors.joining(", "));
    }

    // Inner class to store OTP data
    private static class OtpData {
        private final String otp;
        private final Instant expiresAt;

        public OtpData(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        public String getOtp() {
            return otp;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }
    }
}

