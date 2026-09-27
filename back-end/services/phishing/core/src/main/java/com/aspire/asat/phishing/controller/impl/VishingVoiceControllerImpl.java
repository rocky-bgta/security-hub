package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.VishingVoiceController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.VishingVoiceFilter;
import com.aspire.asat.phishing.dto.response.ClonedVoiceDto;
import com.aspire.asat.phishing.service.DeepfakeVoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class VishingVoiceControllerImpl implements VishingVoiceController {

    private final DeepfakeVoiceService deepfakeVoiceService;

    @Override
    public ResponseEntity<AllResponseDto<List<ClonedVoiceDto>>> list(
            int offset,
            int pageSize,
            VoiceCloneProvider provider,
            String language,
            DeepfakeJobStatus status,
            String search,
            LocalDate createdAfter,
            LocalDate createdBefore,
            String clientId) {
        VishingVoiceFilter filter = VishingVoiceFilter.builder()
                .clientId(clientId)
                .provider(provider)
                .language(language)
                .status(status)
                .search(search)
                .createdAfter(createdAfter)
                .createdBefore(createdBefore)
                .build();
        Page<ClonedVoiceDto> page = deepfakeVoiceService.listVishingVoices(filter, offset, pageSize);
        return ResponseEntity.ok(AllResponseDto.<List<ClonedVoiceDto>>builder()
                .items(page.getContent())
                .total(page.getTotalElements())
                .offset(offset)
                .pageSize(pageSize)
                .build());
    }
}
