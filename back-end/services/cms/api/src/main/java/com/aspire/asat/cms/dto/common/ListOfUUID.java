package com.aspire.asat.cms.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ListOfUUID {

    @NotEmpty(message = "ID list cannot be empty")
    private List<String> ids;

}
