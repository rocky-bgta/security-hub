package com.aspire.asat.registration.model.menu;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "menu_permissions")
public class Permission {
    @Id
    private String id;
    private String name;      // e.g., "orders:view"
    private String menuId;    // actual ObjectId of Menu
    private String menuCode;  // e.g., "orders"
    private String action;    // e.g., "view"
}