package com.aspire.asat.registration.data.menu;

import lombok.Data;

import java.util.List;

@Data
public class MenuResponseDto {
    private String id;
    private String name;
    private String code;
    private Integer sequenceNumber;
    private String parentMenuId;
    private String menuType;
    private String url;
    private String icon;
    private String status;
    private List<String> actions;
    private List<String> productIds;
}