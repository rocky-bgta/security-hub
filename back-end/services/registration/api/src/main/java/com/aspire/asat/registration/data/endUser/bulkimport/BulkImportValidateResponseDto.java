package com.aspire.asat.registration.data.endUser.bulkimport;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportValidateResponseDto {

    private String importSessionId;
    private long totalValid;
    private long totalInvalid;
    private AllResponseDto<List<BulkImportUserDto>> validUsers;
    private AllResponseDto<List<BulkImportUserDto>> invalidUsers;
}
