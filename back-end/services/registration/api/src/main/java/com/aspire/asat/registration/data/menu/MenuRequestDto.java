package com.aspire.asat.registration.data.menu;


import com.aspire.asat.registration.data.enums.MenuStatus;
import com.aspire.asat.registration.data.enums.MenuType;
import com.aspire.asat.registration.data.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MenuRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    @NotNull(message = "Sequence number is required")
    private Integer sequenceNumber;

    private String parentMenuId;

    @NotNull(message = "Url is required")
    private String url;

    @ValidEnum(enumClass = MenuType.class,message = "Please use Correct Menu Type", required = true)
    private String menuType;

    @NotNull(message = "Icon is required")
    private String icon;

   @ValidEnum(enumClass = MenuStatus.class,message = "Please use Correct Status", required = true)
    private String status;

    @NotEmpty(message = "At least one action is required")
    private List<@NotBlank(message = "Action cannot be blank") String> actions;

    /** Optional CMS catalog product IDs this menu applies to. Empty/null = shared (all clients). */
    private List<String> productIds;
}
