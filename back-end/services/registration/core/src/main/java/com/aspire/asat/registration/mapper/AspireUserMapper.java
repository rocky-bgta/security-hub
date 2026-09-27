package com.aspire.asat.registration.mapper;

import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.model.AspireUser;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between AspireUser entity and DTOs
 */
@Component
public class AspireUserMapper {

    /**
     * Convert AspireUser entity to AspireUserDto
     */
    public AspireUserDto toDto(AspireUser aspireUser) {
        if (aspireUser == null) {
            return null;
        }

        return AspireUserDto.builder()
                .baseUserId(aspireUser.getUserId())
                .firstName(aspireUser.getFirstName())
                .lastName(aspireUser.getLastName())
                .username(aspireUser.getUsername())
                .email(aspireUser.getEmail())
                .password(aspireUser.getPassword())
                .phoneNumber(aspireUser.getPhoneNumber())
                .phoneCode(aspireUser.getPhoneCode())
                .country(aspireUser.getCountry())
                .countryCode(aspireUser.getCountryCode())
                .address(aspireUser.getAddress())
                .roles(aspireUser.getRoles())
                .userType(aspireUser.getUserType())
                .status(aspireUser.getStatus())
                .createdBy(aspireUser.getCreatedBy())
                .createdAt(aspireUser.getCreatedAt())
                .updatedAt(aspireUser.getUpdatedAt())
                .clientAdminId(aspireUser.getClientAdminId())
                .department(aspireUser.getDepartment())
                .companyName(aspireUser.getCompanyName())
                .supervisorName(aspireUser.getSupervisorName())
                .designation(aspireUser.getDesignation())
                .riskGroup(aspireUser.getRiskGroup())
                .lastLoginAt(aspireUser.getLastLoginAt())
                .profilePicture(aspireUser.getProfilePicture())
                .isDefault(aspireUser.getIsDefault())
                .tempPasswordExpiry(aspireUser.getTempPasswordExpiry())
                .isCredentialSent(aspireUser.getIsCredentialSent())
                .isRiskProfileExist(aspireUser.getIsRiskProfileExist())
                .build();
    }

}

