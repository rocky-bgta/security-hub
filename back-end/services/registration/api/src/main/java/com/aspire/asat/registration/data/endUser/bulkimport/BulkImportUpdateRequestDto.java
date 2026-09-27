package com.aspire.asat.registration.data.endUser.bulkimport;

import jakarta.validation.Valid;
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
public class BulkImportUpdateRequestDto {

    @NotEmpty
    @Valid
    private List<BulkImportUserDto> users;
}
