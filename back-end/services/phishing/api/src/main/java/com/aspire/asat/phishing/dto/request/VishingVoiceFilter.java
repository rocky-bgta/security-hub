package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.DeepfakeJobStatus;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Filter criteria for listing vishing voice-setup clones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingVoiceFilter {

    /** Optional tenant narrow for platform admins; ignored for non-admin callers. */
    private String clientId;

    private VoiceCloneProvider provider;

    private String language;

    private DeepfakeJobStatus status;

    /** Case-insensitive match against sample file name or external voice id. */
    private String search;

    private LocalDate createdAfter;

    private LocalDate createdBefore;
}
