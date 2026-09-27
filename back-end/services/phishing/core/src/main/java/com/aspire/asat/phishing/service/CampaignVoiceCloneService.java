package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import com.aspire.asat.phishing.model.DeepfakeVoiceClone;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Creates a one-shot voice clone for AI vishing campaigns, or resolves an existing one for reuse.
 */
public interface CampaignVoiceCloneService {

    /**
     * Validates the audio sample, uploads it, invokes the configured clone provider,
     * and returns a completed {@link DeepfakeVoiceClone}.
     */
    DeepfakeVoiceClone createClone(MultipartFile audioSample,
                                   VoiceCloneProvider provider,
                                   String language,
                                   String clientId,
                                   String voiceName);

    /**
     * Returns an existing completed clone owned by {@code clientId} for reuse on a campaign.
     * Does not upload or re-clone.
     */
    DeepfakeVoiceClone getExistingClone(UUID voiceCloneId, String clientId);
}
