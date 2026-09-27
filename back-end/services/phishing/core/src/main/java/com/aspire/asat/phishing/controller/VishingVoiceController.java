package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.response.ClonedVoiceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Vishing Voices", description = "APIs for listing vishing campaign voice-setup clones")
@RequestMapping(value = WebApiUrlConstants.VISHING_VOICES_PATH)
public interface VishingVoiceController {

    @Operation(
            summary = "List vishing voice-setup clones",
            description = "Returns paginated voice clones created via campaign voice-setup. "
                    + "Client admins see only their own tenant. Platform admins "
                    + "(SUPER_ADMIN / ASPIRE_ADMIN / SYSTEM_USER) see all tenants, "
                    + "optionally narrowed by clientId.")
    @GetMapping
    ResponseEntity<AllResponseDto<List<ClonedVoiceDto>>> list(
            @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) VoiceCloneProvider provider,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) DeepfakeJobStatus status,
            @Parameter(description = "Search by voice name, sample file name, or external voice id")
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdBefore,
            @Parameter(description = "Tenant narrow for platform admins only")
            @RequestParam(required = false) String clientId);
}
