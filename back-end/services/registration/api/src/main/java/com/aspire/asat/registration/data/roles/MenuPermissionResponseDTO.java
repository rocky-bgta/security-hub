package com.aspire.asat.registration.data.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuPermissionResponseDTO {
    private String menuId;
    private String menuCode;
    private String name;
    private String url;
    private String menuType;
    private Integer sequenceNumber;
    private String parentMenuId;
    private boolean selected;
    private List<ActionPermissionDTO> actions;
}
