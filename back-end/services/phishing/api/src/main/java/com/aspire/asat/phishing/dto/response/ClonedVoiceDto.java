package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A previously cloned voice available for reuse in the deepfake wizard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClonedVoiceDto {

    /** Internal voice clone identifier; pass this to Step 4 to reuse the voice. */
    private String voiceCloneId;

    /** User-provided display name for the cloned voice. */
    private String voiceName;

    private VoiceCloneProvider provider;

    /** Original uploaded sample file name, when available. */
    private String fileName;

    /** Time-limited presigned URL to play the original voice sample in the browser. */
    private String sampleUrl;

    private String language;

    private DeepfakeJobStatus status;

    /** True when this entry reused another provider voice due to clone quota fallback. */
    private boolean usedFallbackVoice;

    private Instant createdAt;
}
