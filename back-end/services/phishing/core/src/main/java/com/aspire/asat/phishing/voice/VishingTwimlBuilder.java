package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VishingInteractionMode;
import com.aspire.asat.phishing.exception.ServiceException;
import com.twilio.http.HttpMethod;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Hangup;
import com.twilio.twiml.voice.Play;
import com.twilio.twiml.voice.Say;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

/**
 * Builds Twilio Voice Markup (TwiML) for the scripted vishing call: a playback +
 * {@code <Gather>} document that plays the cloned-voice audio (or a spoken
 * fallback) and captures the target's DTMF and/or speech response, plus a
 * closing document rendered after the response is handled.
 */
@Slf4j
@Component
public class VishingTwimlBuilder {

    @Value("${voice.twilio.gather.timeout-seconds:20}")
    private int gatherTimeoutSeconds;

    @Value("${voice.twilio.gather.num-digits:6}")
    private int numDigits;

    @Value("${voice.twilio.gather.finish-on-key:#}")
    private String finishOnKey;

    @Value("${voice.twilio.gather.speech-timeout:auto}")
    private String speechTimeout;

    @Value("${voice.twilio.gather.speech-hints:}")
    private String speechHints;

    @Value("${voice.twilio.messages.no-input:We did not receive your response. Goodbye.}")
    private String noInputMessage;

    /**
     * Playback + capture TwiML. When {@code audioUrl} is present it is played via
     * {@code <Play>}; otherwise {@code fallbackText} is spoken via {@code <Say>}.
     * The gather always posts to {@code actionUrl} (even on no input) so a single
     * outcome path handles every case.
     */
    public String buildGatherTwiml(String audioUrl, String fallbackText, String actionUrl,
                                   VishingInteractionMode mode) {
        VishingInteractionMode resolvedMode = mode != null ? mode : VishingInteractionMode.BOTH;

        Gather.Builder gather = new Gather.Builder()
                .inputs(resolveInputs(resolvedMode))
                .action(actionUrl)
                .method(HttpMethod.POST)
                .actionOnEmptyResult(true)
                .timeout(gatherTimeoutSeconds);

        if (resolvedMode != VishingInteractionMode.SPEECH) {
            gather.numDigits(numDigits).finishOnKey(finishOnKey);
        }
        if (resolvedMode != VishingInteractionMode.DTMF) {
            gather.speechTimeout(speechTimeout);
            if (StringUtils.hasText(speechHints)) {
                gather.hints(speechHints);
            }
        }

        if (StringUtils.hasText(audioUrl)) {
            gather.play(new Play.Builder(URI.create(audioUrl)).build());
        } else if (StringUtils.hasText(fallbackText)) {
            gather.say(new Say.Builder(fallbackText).build());
        }

        VoiceResponse response = new VoiceResponse.Builder()
                .gather(gather.build())
                .say(new Say.Builder(noInputMessage).build())
                .hangup(new Hangup.Builder().build())
                .build();
        return toXml(response);
    }

    /**
     * Closing TwiML spoken after the recipient's response is processed.
     */
    public String buildClosingTwiml(String message) {
        VoiceResponse.Builder builder = new VoiceResponse.Builder();
        if (StringUtils.hasText(message)) {
            builder.say(new Say.Builder(message).build());
        }
        return toXml(builder.hangup(new Hangup.Builder().build()).build());
    }

    private List<Gather.Input> resolveInputs(VishingInteractionMode mode) {
        return switch (mode) {
            case DTMF -> List.of(Gather.Input.DTMF);
            case SPEECH -> List.of(Gather.Input.SPEECH);
            case BOTH -> List.of(Gather.Input.DTMF, Gather.Input.SPEECH);
        };
    }

    private String toXml(VoiceResponse response) {
        try {
            return response.toXml();
        } catch (TwiMLException e) {
            throw new ServiceException("Failed to build TwiML response", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
