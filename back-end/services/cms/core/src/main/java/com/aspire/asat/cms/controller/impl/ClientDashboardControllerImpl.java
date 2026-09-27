package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.ClientDashboardController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardRequestDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardResponseDto;
import com.aspire.asat.cms.service.ClientDashboardService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClientDashboardControllerImpl implements ClientDashboardController {

    private final ClientDashboardService clientDashboardService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<ClientDashboardResponseDto>> createOrUpdateClientDashboard(ClientDashboardRequestDto requestDto) {
        try {
            log.info("Creating or updating client dashboard for clientAdminId: {}", requestDto.getClientAdminId());

            ClientDashboardResponseDto responseDto = clientDashboardService.createOrUpdateClientDashboard(requestDto);

            return ResponseEntity.ok(new ApiResponseDto<>("Client dashboard created or updated successfully", 200, responseDto));

        } catch (Exception e) {
            log.error("Error creating or updating client dashboard for clientAdminId: {}", requestDto.getClientAdminId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to create or update client dashboard: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientDashboardResponseDto>> getClientDashboardByClientAdminId() {
        String clientAdminId = null;
        try {
            CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
            clientAdminId = userContext.getUserId();

            log.info("Retrieving client dashboard for clientAdminId: {}", clientAdminId);


            ClientDashboardResponseDto responseDto = clientDashboardService.getClientDashboardByClientAdminId(clientAdminId);

            return ResponseEntity.ok(new ApiResponseDto<>("Client dashboard retrieved successfully", 200, responseDto));

        } catch (RuntimeException e) {
            log.error("Client dashboard not found for clientAdminId: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving client dashboard for clientAdminId: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve client dashboard: " + e.getMessage(), 500, null));
        }
    }
}
