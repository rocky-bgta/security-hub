package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.UserLicenceController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.userLicence.UserLicenceResponseDto;
import com.aspire.asat.cms.dto.userLicence.BulkUserLicenceRequestDto;
import com.aspire.asat.cms.service.UserLicenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UserLicenceControllerImpl implements UserLicenceController {

    private final UserLicenceService userLicenceService;

    @Override
    public ResponseEntity<ApiResponseDto<List<UserLicenceResponseDto>>> createBulkUserLicences(BulkUserLicenceRequestDto requestDto) {
        log.info("Creating {} user licences in bulk", requestDto.getUserLicenceData().size());
        
        List<UserLicenceResponseDto> responseDtos = userLicenceService.createBulkUserLicences(requestDto);
        
        return ResponseEntity.ok(new ApiResponseDto<>("User licences created successfully", 200, responseDtos));
    }
}
