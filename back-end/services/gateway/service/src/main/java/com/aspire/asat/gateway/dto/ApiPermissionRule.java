package com.aspire.asat.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiPermissionRule {
    private String path;
    private String method;
    private List<String> permissions;
}
