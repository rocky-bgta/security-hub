package com.aspire.asat.registration.data.systemUser.request;

import com.aspire.asat.registration.data.enums.UserStatus;
import lombok.Data;

import java.util.List;

@Data
public class SystemUserSearchRequestDTO {

    private String search; // search by name or email
    private UserStatus status; // ACTIVE or INACTIVE
    private List<String> departments; // filter by departments
    private Integer offset = 0;
    private Integer pageSize = 10;
}
