package com.aspire.asat.registration.data.department.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUserCountResponseDTO {

    private String departmentName;
    private long userCount;
}
