package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import com.aspire.asat.phishing.dto.enums.VoiceResponseStage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VoiceResponseStageMapperTest {

    @Test
    void mapsCallEngagedOnlyToTrainingForClicks() {
        assertEquals(SubPackageAssignedFor.PHISHING_TRAINING_FOR_CLICKS,
                VoiceResponseStageMapper.toAssignedFor(List.of(VoiceResponseStage.CALL_ENGAGED)));
    }

    @Test
    void mapsCompromisedOnlyToTrainingForCompromises() {
        assertEquals(SubPackageAssignedFor.PHISHING_TRAINING_FOR_COMPROMISES,
                VoiceResponseStageMapper.toAssignedFor(List.of(VoiceResponseStage.COMPROMISED)));
    }

    @Test
    void mapsBothStagesToTrainingForAll() {
        assertEquals(SubPackageAssignedFor.PHISHING_TRAINING_FOR_ALL,
                VoiceResponseStageMapper.toAssignedFor(
                        List.of(VoiceResponseStage.CALL_ENGAGED, VoiceResponseStage.COMPROMISED)));
    }

    @Test
    void returnsNullForEmptyStages() {
        assertNull(VoiceResponseStageMapper.toAssignedFor(List.of()));
        assertNull(VoiceResponseStageMapper.toAssignedFor(null));
    }
}
