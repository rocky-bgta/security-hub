package com.aspire.asat.phishing.dto;

import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignVoiceData {

    private boolean consentConfirmed;
    private String consentText;
    private Instant consentConfirmedAt;
    /** Internal voice clone document id (DeepfakeVoiceClone). */
    private String voiceCloneId;
    /** Provider-side voice id (e.g. ElevenLabs voice_id). */
    private String externalVoiceId;
    private String voiceDisplayName;
    private VoiceCloneProvider cloningEngine;
    private String callerId;
    /** True when the campaign is using a reused provider voice due to clone quota fallback. */
    private boolean usedFallbackVoice;
}
