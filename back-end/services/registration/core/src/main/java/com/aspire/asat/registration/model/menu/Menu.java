package com.aspire.asat.registration.model.menu;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;


import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "menus")
public class Menu {
    @Id
    private String id;
    private String name;

    @Indexed(unique = true)
    private String code;

    private Integer sequenceNumber;
    private String parentMenuId;
    private String menuType;
    private String url;
    private List<String> actions;
    private List<String> productIds;
    private String icon;
    private String status;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String updateBy;

}