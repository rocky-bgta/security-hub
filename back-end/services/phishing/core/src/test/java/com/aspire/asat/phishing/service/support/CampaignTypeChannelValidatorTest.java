package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CampaignTypeChannelValidatorTest {

    @Test
    void allowsEmailTypesForEmailChannel() {
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                CampaignChannel.EMAIL, CampaignType.SIMULATED_PHISHING));
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                CampaignChannel.EMAIL, CampaignType.PHISHING_WITH_TRAINING));
    }

    @Test
    void defaultsNullChannelToEmailForLegacyClients() {
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                null, CampaignType.SIMULATED_PHISHING));
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                null, CampaignType.PHISHING_WITH_TRAINING));
        assertThrows(PhishingValidationException.class, () -> CampaignTypeChannelValidator.validate(
                null, CampaignType.SMISHING_SIMULATION));
    }

    @Test
    void allowsSmsTypesForSmsChannel() {
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                CampaignChannel.SMS, CampaignType.SMISHING_SIMULATION));
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                CampaignChannel.SMS, CampaignType.SMISHING_WITH_TRAINING));
    }

    @Test
    void rejectsMismatchedChannelAndType() {
        assertThrows(PhishingValidationException.class, () -> CampaignTypeChannelValidator.validate(
                CampaignChannel.SMS, CampaignType.SIMULATED_PHISHING));
        assertThrows(PhishingValidationException.class, () -> CampaignTypeChannelValidator.validate(
                CampaignChannel.EMAIL, CampaignType.SMISHING_SIMULATION));
        assertThrows(PhishingValidationException.class, () -> CampaignTypeChannelValidator.validate(
                CampaignChannel.VOICE, CampaignType.SIMULATED_PHISHING));
        assertThrows(PhishingValidationException.class, () -> CampaignTypeChannelValidator.validate(
                CampaignChannel.EMAIL, CampaignType.VISHING_SIMULATION));
    }

    @Test
    void allowsVoiceTypesForVoiceChannel() {
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                CampaignChannel.VOICE, CampaignType.VISHING_SIMULATION));
        assertDoesNotThrow(() -> CampaignTypeChannelValidator.validate(
                CampaignChannel.VOICE, CampaignType.VISHING_WITH_TRAINING));
    }
}
