package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayloadTypeChannelSupportTest {

    @Test
    void effectiveChannel_Null_ReturnsEmail() {
        assertEquals(PayloadTypeChannel.EMAIL, PayloadTypeChannelSupport.effectiveChannel(null));
    }

    @Test
    void effectiveChannel_Voice_ReturnsVoice() {
        assertEquals(PayloadTypeChannel.VOICE, PayloadTypeChannelSupport.effectiveChannel(PayloadTypeChannel.VOICE));
    }

    @Test
    void effectiveChannelForUpdate_NullRequest_PreservesExistingSms() {
        assertEquals(
                PayloadTypeChannel.SMS,
                PayloadTypeChannelSupport.effectiveChannelForUpdate(null, PayloadTypeChannel.SMS));
    }

    @Test
    void effectiveChannelForUpdate_NullRequestAndEntity_DefaultsToEmail() {
        assertEquals(
                PayloadTypeChannel.EMAIL,
                PayloadTypeChannelSupport.effectiveChannelForUpdate(null, null));
    }

    @Test
    void fromTemplateType_MapsSmsAndEmail() {
        assertEquals(PayloadTypeChannel.SMS, PayloadTypeChannelSupport.fromTemplateType(TemplateType.SMS));
        assertEquals(PayloadTypeChannel.EMAIL, PayloadTypeChannelSupport.fromTemplateType(TemplateType.EMAIL));
        assertEquals(PayloadTypeChannel.EMAIL, PayloadTypeChannelSupport.fromTemplateType(null));
    }
}
