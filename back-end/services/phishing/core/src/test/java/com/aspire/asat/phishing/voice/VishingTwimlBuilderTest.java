package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VishingTwimlBuilderTest {

    private static final String AUDIO_URL = "https://s3.example.com/audio.mp3?sig=abc";
    private static final String ACTION_URL = "https://dev.example.com/gateway/phishing/v/calls/trk-1/gather";

    private VishingTwimlBuilder builder;

    @BeforeEach
    void setUp() throws Exception {
        builder = new VishingTwimlBuilder();
        setField(builder, "gatherTimeoutSeconds", 20);
        setField(builder, "numDigits", 6);
        setField(builder, "finishOnKey", "#");
        setField(builder, "speechTimeout", "auto");
        setField(builder, "speechHints", "yes,no");
        setField(builder, "noInputMessage", "Goodbye.");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void bothMode_playsAudioAndGathersDtmfAndSpeech() {
        String xml = builder.buildGatherTwiml(AUDIO_URL, null, ACTION_URL, VishingInteractionMode.BOTH);

        assertTrue(xml.contains("<Gather"));
        assertTrue(xml.contains("timeout=\"20\""));
        assertTrue(xml.contains("input=\"dtmf speech\""));
        assertTrue(xml.contains("<Play>"));
        assertTrue(xml.contains(ACTION_URL));
        assertTrue(xml.contains("actionOnEmptyResult=\"true\""));
    }

    @Test
    void dtmfMode_usesDtmfInputOnly() {
        String xml = builder.buildGatherTwiml(AUDIO_URL, null, ACTION_URL, VishingInteractionMode.DTMF);

        assertTrue(xml.contains("input=\"dtmf\""));
        assertFalse(xml.contains("speech"));
    }

    @Test
    void speechMode_usesSpeechInputOnly() {
        String xml = builder.buildGatherTwiml(AUDIO_URL, null, ACTION_URL, VishingInteractionMode.SPEECH);

        assertTrue(xml.contains("input=\"speech\""));
    }

    @Test
    void noAudio_fallsBackToSay() {
        String xml = builder.buildGatherTwiml(null, "Hello there", ACTION_URL, VishingInteractionMode.BOTH);

        assertTrue(xml.contains("<Say>Hello there</Say>"));
        assertFalse(xml.contains("<Play>"));
    }

    @Test
    void closingTwiml_containsMessageAndHangup() {
        String xml = builder.buildClosingTwiml("Thank you. Goodbye.");

        assertTrue(xml.contains("<Say>Thank you. Goodbye.</Say>"));
        assertTrue(xml.contains("<Hangup/>"));
    }
}
