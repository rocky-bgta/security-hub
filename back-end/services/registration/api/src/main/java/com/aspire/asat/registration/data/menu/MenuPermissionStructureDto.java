package com.aspire.asat.registration.data.menu;

import lombok.Data;
import java.util.List;

@Data
public class MenuPermissionStructureDto {
    private String id;
    private String name;
    private String code;
    private String url;
    private Integer sequenceNumber;
    private String parentMenuId;
    private String menuType;
    private List<String> actions;
    private List<PermissionDto> permissions;
}
