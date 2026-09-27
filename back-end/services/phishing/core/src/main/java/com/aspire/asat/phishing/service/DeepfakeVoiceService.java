package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.dto.request.VishingVoiceFilter;
import com.aspire.asat.phishing.dto.response.ClonedVoiceDto;
import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

public interface DeepfakeVoiceService {

    /**
     * Step 4 - attach a voice to the video, either by cloning a freshly uploaded
     * sample or by reusing an existing cloned voice ({@code voiceCloneId}).
     * Exactly one of {@code audioSample} / {@code voiceCloneId} must be supplied.
     * {@code voiceName} is optional when uploading a new sample.
     * When {@code providerId} is set (new clone only), provider name, credentials,
     * and model come from {@code provider_credentials} and {@code provider} is ignored;
     * otherwise {@code provider} is parsed as a {@link VoiceCloneProvider}.
     */
    DeepfakeVideoStepResponse updateStep4(UUID videoId, MultipartFile audioSample, String provider,
                                          String language, UUID voiceCloneId, String voiceName, String providerId);

    /**
     * Paginated list of the current client's previously cloned voices, most
     * recent first, with optional provider and creation-date filters.
     */
    Page<ClonedVoiceDto> listClonedVoices(VoiceCloneProvider provider, LocalDate createdAfter, LocalDate createdBefore,
                                          int offset, int pageSize);

    /**
     * Paginated list of vishing campaign voice-setup clones only. Non-platform
     * users are limited to their own tenant; platform admins see all (or a
     * narrowed {@code filter.clientId}).
     */
    Page<ClonedVoiceDto> listVishingVoices(VishingVoiceFilter filter, int offset, int pageSize);

    /**
     * Soft-deletes a cloned voice owned by the current client. When the provider
     * voice is not shared by other non-deleted clones, also deletes it on the provider.
     */
    void deleteClonedVoice(UUID voiceCloneId);
}
