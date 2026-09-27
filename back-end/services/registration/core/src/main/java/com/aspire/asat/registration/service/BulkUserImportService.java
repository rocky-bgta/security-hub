package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportOnboardResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportRowStatus;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUpdateRequestDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUserDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportValidateResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BulkUserImportService {

    BulkImportValidateResponseDto validateImport(MultipartFile file, String clientAdminId, int offset, int pageSize);

    AllResponseDto<List<BulkImportUserDto>> getSessionUsers(
            String sessionId, BulkImportRowStatus status, int offset, int pageSize);

    BulkImportValidateResponseDto updateSessionUsers(
            String sessionId, BulkImportUpdateRequestDto request, int offset, int pageSize);

    BulkImportOnboardResponseDto onboardSession(String sessionId);
}
