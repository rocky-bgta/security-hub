package com.aspire.asat.registration.data.microsoft;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequestDto {

    @NotEmpty(message = "At least one group ID is required")
    private List<String> groupIds;

    private boolean createNewUsers;
    private boolean updateExistingUsers;
    private String defaultRole;
}

