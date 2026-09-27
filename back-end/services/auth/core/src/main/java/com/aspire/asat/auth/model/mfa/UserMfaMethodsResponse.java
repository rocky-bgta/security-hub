package com.aspire.asat.auth.model.mfa;

import com.aspire.asat.auth.dto.UserMfaMethodItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMfaMethodsResponse {

    private List<UserMfaMethodItem> methods;
}
