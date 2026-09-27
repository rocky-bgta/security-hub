package com.aspire.asat.auth.mapper;

import com.aspire.asat.auth.dto.PurchaseProductDto;
import com.aspire.asat.auth.dto.UserDetailsResponse;
import com.aspire.asat.auth.dto.apiResponses.AccessTokenResponse;
import com.aspire.asat.auth.dto.enums.AdminStatus;
import com.aspire.asat.auth.dto.enums.OnboardBy;
import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.ClientAdmin;
import com.aspire.asat.auth.entity.UserLoginHistory;
import com.aspire.asat.auth.repository.ClientAdminRepository;
import com.aspire.asat.auth.repository.RoleRepository;
import com.aspire.asat.auth.repository.UserRepository;
import com.aspire.asat.common.dto.files.RoleData;
import com.aspire.asat.common.enums.TokenActionType;
import com.aspire.asat.common.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ClientAdminRepository clientAdminRepository;


    public AccessTokenResponse mapToTokenResponse(AspireUser aspireUser) {
        AccessTokenResponse response = new AccessTokenResponse();
        BeanUtils.copyProperties(aspireUser, response);

        response.setUserId(aspireUser.getUserId().toString());
        response.setClientAdminId(ObjectUtils.isEmpty(aspireUser.getClientAdminId()) ? "" : aspireUser.getClientAdminId());
        response.setMspId(ObjectUtils.isEmpty(aspireUser.getMspId()) ? "" : aspireUser.getMspId());
        response.setCountryId(ObjectUtils.isEmpty(aspireUser.getCountry()) ? "" : aspireUser.getCountry());

        response.setUserType(aspireUser.getUserType());
        response.setUsername(aspireUser.getUsername());
        response.setPhoneNumber(aspireUser.getPhoneNumber());
        response.setRiskGroup(aspireUser.getRiskGroup());

        // Set the full name by combining firstName and lastName
        String fullName = buildFullName(aspireUser.getFirstName(), aspireUser.getLastName());
        response.setFullName(fullName);

        // Fetch role data (ID and name) from the role table using role IDs
        List<RoleData> roles = fetchRoleData(aspireUser.getRoles());
        response.setRoles(roles);

        // Fetch client admin information if clientAdminId is present
        if (!ObjectUtils.isEmpty(aspireUser.getClientAdminId())) {
            Optional<AspireUser> clientAdminOpt = userRepository.findById(UUID.fromString(aspireUser.getClientAdminId()));
            if (clientAdminOpt.isPresent()) {
                AspireUser clientAdmin = clientAdminOpt.get();
                Optional<ClientAdmin> clientAdminOpt1= clientAdminRepository.findById(aspireUser.getClientAdminId());
                if (clientAdminOpt1.isPresent()) {
                    ClientAdmin admin = clientAdminOpt1.get();
                    // Set onboardBy only if it's not null to avoid NullPointerException
                    response.setOnboardBy(admin.getOnboardBy() != null ? admin.getOnboardBy().name() : null);
                }
                response.setClientAdminEmail(clientAdmin.getEmail());
                response.setClientAdminFullName(buildFullName(clientAdmin.getFirstName(), clientAdmin.getLastName()));
            }
        }

        // Set buy-now flag
        response.setIsBuyNow(aspireUser.getIsBuyNow());

        return response;
    }

    /**
     * Build a full name by combining firstName and lastName
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "";
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
     * Fetch role data (ID and name) from the role table using role IDs
     */
    private List<RoleData> fetchRoleData(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        return roleIds.stream()
                .map(roleRepository::findById)
                .filter(Optional::isPresent)
                .map(role -> RoleData.builder()
                        .roleId(role.get().getId())
                        .roleName(role.get().getRoleName())
                        .build())
                .toList();
    }


    public UserLoginHistory mapToLoginHistoryEntity(AspireUser aspireUser, String token, String requestIp, String deviceInfo) {
        UserLoginHistory loginHistory = new UserLoginHistory();
        loginHistory.setUsername(aspireUser.getUsername())
                .setId(UUID.randomUUID())
                .setClientAdminId(getClientAdminId(aspireUser))
                .setUserId(aspireUser.getUserId().toString())
                .setToken(token)
                .setUserType(aspireUser.getUserType())
                .setRequestIp(requestIp)
                .setDeviceInfo(deviceInfo)
                .setAction(TokenActionType.LOGIN)
                .setLoginTime(Instant.now());
        return loginHistory;
    }

    private String getClientAdminId(AspireUser aspireUser) {
        return aspireUser.getUserType().equalsIgnoreCase(UserType.USER.getValue())
                ? aspireUser.getClientAdminId() :
                aspireUser.getUserId().toString();
    }


    public UserDetailsResponse mapToResponse(AspireUser aspireUser, Boolean pendingPayment,
                                            List<String> clientProductTags,
                                            List<PurchaseProductDto> purchaseProducts) {
        Boolean selfOnboardingUser = aspireUser.getIsBuyNow();
        String timeZone = null;

        String clientAdminId = resolveClientAdminId(aspireUser);
        if (clientAdminId != null) {
            Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
            if (clientAdminOpt.isPresent()) {
                ClientAdmin clientAdmin = clientAdminOpt.get();
                timeZone = clientAdmin.getTimeZone();
                if (UserType.CLIENT_ADMIN.getValue().equals(aspireUser.getUserType())
                        && AdminStatus.ACTIVE.equals(clientAdmin.getStatus())) {
                    selfOnboardingUser = false;
                }
            }
        }

        OnboardBy onboardBy = null;
        if (Boolean.TRUE.equals(aspireUser.getIsTrial())) {
            onboardBy = OnboardBy.TRIAL;
        } else if (Boolean.TRUE.equals(aspireUser.getIsBuyNow())) {
            onboardBy = OnboardBy.BUY_NOW;
        }

        return UserDetailsResponse.builder()
                .userId(aspireUser.getUserId().toString())
                .clientAdminId(aspireUser.getClientAdminId())
                .username(aspireUser.getUsername())
                .email(aspireUser.getEmail())
                .phoneNumber(aspireUser.getPhoneNumber())
                .fullName(buildFullName(aspireUser.getFirstName(), aspireUser.getLastName()))
                .userStatus(aspireUser.getStatus())
                .riskGroup(aspireUser.getRiskGroup())
                .passwordExpiryDate(null)
                .profilePicture(aspireUser.getProfilePicture())
                .clientProductTags(clientProductTags)
                .selfOnboardingUser(selfOnboardingUser)
                .pendingPayment(pendingPayment)
                .onboardBy(onboardBy)
                .timeZone(timeZone)
                .purchaseProducts(purchaseProducts)
                .build();
    }

    private String resolveClientAdminId(AspireUser aspireUser) {
        if (UserType.CLIENT_ADMIN.getValue().equals(aspireUser.getUserType())) {
            return aspireUser.getUserId() != null ? aspireUser.getUserId().toString() : null;
        }
        if (!ObjectUtils.isEmpty(aspireUser.getClientAdminId())) {
            return aspireUser.getClientAdminId();
        }
        return null;
    }
}
