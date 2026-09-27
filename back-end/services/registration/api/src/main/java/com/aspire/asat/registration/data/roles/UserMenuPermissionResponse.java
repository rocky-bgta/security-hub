package com.aspire.asat.registration.data.roles;

import com.aspire.asat.registration.data.menu.UserMenuResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserMenuPermissionResponse {
    @NotNull(message = "Role ID cannot be null")
    private String roleId;
    private String roleName;
    private List<UserMenuResponse> userMenuResponses;
}
