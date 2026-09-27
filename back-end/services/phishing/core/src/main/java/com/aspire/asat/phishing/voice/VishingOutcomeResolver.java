package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VishingCallOutcome;
import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps a captured {@code <Gather>} response (DTMF digits and/or speech) to a
 * {@link VishingCallOutcome}, honoring the scenario's interaction mode and the
 * campaign's configured success keywords.
 *
 * <ul>
 *   <li>DTMF digits captured (mode allows DTMF) -&gt; {@code COMPROMISED}.</li>
 *   <li>Speech matches a success keyword (mode allows speech) -&gt; {@code COMPROMISED}.</li>
 *   <li>Speech present but no keyword match -&gt; {@code ENGAGED}.</li>
 *   <li>No usable input -&gt; {@code ANSWERED}.</li>
 * </ul>
 */
@Component
public class VishingOutcomeResolver {

    public VishingOutcomeResolution resolve(String digits, String speechResult,
                                            VishingInteractionMode mode, List<String> successKeywords) {
        VishingInteractionMode resolvedMode = mode != null ? mode : VishingInteractionMode.BOTH;
        Map<String, Object> sensitive = new HashMap<>();
        List<String> detected = new ArrayList<>();

        boolean acceptsDtmf = resolvedMode != VishingInteractionMode.SPEECH;
        boolean acceptsSpeech = resolvedMode != VishingInteractionMode.DTMF;

        if (acceptsDtmf && StringUtils.hasText(digits)) {
            sensitive.put("dtmfDigits", digits.trim());
            return new VishingOutcomeResolution(VishingCallOutcome.COMPROMISED, sensitive, detected);
        }

        if (acceptsSpeech && StringUtils.hasText(speechResult)) {
            String speech = speechResult.trim();
            sensitive.put("speechResult", speech);
            List<String> matched = matchKeywords(speech, successKeywords);
            if (!matched.isEmpty()) {
                detected.addAll(matched);
                return new VishingOutcomeResolution(VishingCallOutcome.COMPROMISED, sensitive, detected);
            }
            return new VishingOutcomeResolution(VishingCallOutcome.ENGAGED, sensitive, detected);
        }

        return new VishingOutcomeResolution(VishingCallOutcome.ANSWERED, sensitive, detected);
    }

    private List<String> matchKeywords(String speech, List<String> successKeywords) {
        List<String> matched = new ArrayList<>();
        if (successKeywords == null || successKeywords.isEmpty()) {
            return matched;
        }
        String haystack = speech.toLowerCase(Locale.ROOT);
        for (String keyword : successKeywords) {
            if (StringUtils.hasText(keyword)
                    && haystack.contains(keyword.trim().toLowerCase(Locale.ROOT))) {
                matched.add(keyword.trim());
            }
        }
        return matched;
    }
}
