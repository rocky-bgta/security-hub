package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.enums.VoiceResponseStage;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Maps vishing setup response stages to {@link SubPackageAssignedFor} training trigger policy.
 */
public final class VoiceResponseStageMapper {

    private VoiceResponseStageMapper() {
    }

    public static SubPackageAssignedFor toAssignedFor(List<VoiceResponseStage> stages) {
        if (stages == null || stages.isEmpty()) {
            return null;
        }
        Set<VoiceResponseStage> set = EnumSet.copyOf(stages);
        boolean engaged = set.contains(VoiceResponseStage.CALL_ENGAGED);
        boolean compromised = set.contains(VoiceResponseStage.COMPROMISED);
        if (engaged && compromised) {
            return SubPackageAssignedFor.PHISHING_TRAINING_FOR_ALL;
        }
        if (engaged) {
            return SubPackageAssignedFor.PHISHING_TRAINING_FOR_CLICKS;
        }
        if (compromised) {
            return SubPackageAssignedFor.PHISHING_TRAINING_FOR_COMPROMISES;
        }
        return null;
    }
}
