package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.phishing.controller.AiConfigController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.AiGlobalConfigRequest;
import com.aspire.asat.phishing.dto.response.AiGlobalConfigResponse;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.AiProviderSecretRef;
import com.aspire.asat.phishing.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AiConfigControllerImpl implements AiConfigController {

    private final AiConfigService aiConfigService;
    private final MessageService messageService;

    @Override
    public ResponseEntity<ApiResponseDto<List<AiProviderSecretRef>>> listAiProviderSecretRefs() {
        try {
            List<AiProviderSecretRef> body = aiConfigService.listAiProviderSecretRefsByClientAdminId();
            return ResponseEntity.ok(new ApiResponseDto<>("AI provider secret references retrieved", 200, body));
        } catch (ServiceException e) {
            log.warn("AI config list failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("AI config list error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to list AI provider secret references", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AiGlobalConfigResponse>> saveGlobalConfig(AiGlobalConfigRequest request) {
        try {
            AiGlobalConfigResponse body = aiConfigService.saveGlobalConfig(request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.AI_CONFIGURATION_UPDATED), 200, body));
        } catch (ServiceException e) {
            log.warn("AI config save failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("AI config save error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to save AI configuration", 500, null));
        }
    }
}
