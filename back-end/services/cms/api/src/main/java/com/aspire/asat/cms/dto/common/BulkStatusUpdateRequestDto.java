package com.aspire.asat.cms.dto.common;

import com.aspire.asat.cms.dto.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class BulkStatusUpdateRequestDto {

    @NotEmpty(message = "Ids list cannot be empty")
    private List<String> ids;

    @NotNull(message = "Status must be provided")
    private Status status;

}
