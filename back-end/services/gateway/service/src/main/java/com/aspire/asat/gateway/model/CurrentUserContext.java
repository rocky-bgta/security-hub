package com.aspire.asat.gateway.model;

import com.aspire.asat.gateway.dto.RoleData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
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
