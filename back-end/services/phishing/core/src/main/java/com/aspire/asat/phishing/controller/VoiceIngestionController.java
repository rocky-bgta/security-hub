package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.dto.request.VoiceCallStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Voice Ingestion", description = "Post-call webhook endpoints for vishing telephony service")
@RequestMapping(value = WebApiUrlConstants.VOICE_BASE_PATH)
public interface VoiceIngestionController {

    @Operation(summary = "Record call lifecycle status")
    @PostMapping(WebApiUrlConstants.VOICE_CALL_STATUS)
    ResponseEntity<ApiResponseDto<String>> recordCallStatus(
            @PathVariable String trackingId,
            @RequestHeader(value = "X-Voice-Ingestion-Key", required = false) String apiKey,
            @Valid @RequestBody VoiceCallStatusRequest request);

    @Operation(summary = "Process final call result")
    @PostMapping(WebApiUrlConstants.VOICE_CALL_RESULT)
    ResponseEntity<ApiResponseDto<String>> processCallResult(
            @PathVariable String trackingId,
            @RequestHeader(value = "X-Voice-Ingestion-Key", required = false) String apiKey,
            @Valid @RequestBody VoiceCallResultRequest request);
}
