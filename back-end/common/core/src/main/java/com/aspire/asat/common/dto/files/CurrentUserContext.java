package com.aspire.asat.common.dto.files;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentUserContext implements Serializable {
    private String userId;
    private String tokenId;
    private String clientAdminId;
    private List<String> clientAdminIds;
    private String mspId;
    private String countryId;
    private String email;
    private String username;
    private String phoneNumber;
    private String userType;
    private String userStatus;
    private String coRelationId;
    private String fullName;
    private String clientAdminEmail;
    private String clientAdminFullName;
    private List<String> scope;
    private List<RoleData> roles;
    private String onboardBy;
}
