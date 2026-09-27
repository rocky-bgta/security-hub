package com.aspire.asat.cms.dto.clientAdmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignPackageRequestDTO {

    @NotBlank
    private String clientId;

    @NotBlank
    private String packageId;

    @NotEmpty
    private List<String> userIds;
}
