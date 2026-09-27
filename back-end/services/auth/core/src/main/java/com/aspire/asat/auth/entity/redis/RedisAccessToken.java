package com.aspire.asat.auth.entity.redis;


import com.aspire.asat.auth.dto.enums.UserStatus;
import com.aspire.asat.common.dto.files.RoleData;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisAccessToken implements Serializable {
    private String tokenId; // UUID for JWT subject and Redis key
    private String userId;
    private String clientAdminId;
    private String mspId;
    private String countryId;
    private String email;
    private String phoneNumber;
    private String accessToken;
    private String refreshToken;
    private String userType;
    private String username;
    private UserStatus userStatus;
    private String fullName;
    private String clientAdminEmail;
    private String clientAdminFullName;

    private String coRelationId;
    private List<String> scope;
    private List<RoleData> roles; // User roles with ID and name
}
