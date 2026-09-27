package com.aspire.asat.registration.data.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMenuResponse {
    private String id;
    private String name;
    private String code;
    private Integer sequenceNumber;
    private String parentMenuId;
    private String menuType;
    private String url;
    private String icon;
    private List<String> permittedActions;
    private List<String> permissions;
}
