package com.aspire.asat.registration.model;

import com.aspire.asat.common.enums.TokenActionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "user_login_history")
public class UserLoginHistory {

    @Id
    private UUID id;

    private String username;
    private String userId;
    private String clientAdminId;
    private String token;

    private String userType;
    private TokenActionType action; // LOGIN or LOGOUT

    private String requestIp;
    private String deviceInfo;
    private Instant loginTime;
    private Instant logoutTime;

}
