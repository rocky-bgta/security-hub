package com.aspire.asat.registration.data.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionPermissionDTO {
    private String action;
    private Boolean selected;
}
