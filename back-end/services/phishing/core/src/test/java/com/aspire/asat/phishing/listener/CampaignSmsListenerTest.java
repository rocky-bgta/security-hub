package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.aspire.asat.phishing.service.impl.CampaignSmsConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignSmsListenerTest {

    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123/phishing-campaign-sms-dev-queue";

    @Mock
    private SqsClient sqsClient;

    @Mock
    private CampaignSmsConsumer campaignSmsConsumer;

    @Mock
    private ObjectMapper objectMapper;

    private CampaignSmsListener listener;

    @BeforeEach
    void setUp() throws Exception {
        listener = new CampaignSmsListener(sqsClient, campaignSmsConsumer, objectMapper);
        setField(listener, "queueUrl", QUEUE_URL);
    }

    @Test
    void processesSmsMessage() throws Exception {
        CampaignSmsMessage smsMessage = CampaignSmsMessage.builder()
                .channel(CampaignChannel.SMS)
                .campaignId("camp-sms")
                .recipientId("rec-sms")
                .toPhone("+15551234567")
                .messageBody("Hello")
                .build();
        Message sqsMessage = sqsMessage("{\"channel\":\"SMS\"}");

        when(objectMapper.readValue(eq(sqsMessage.body()), eq(CampaignSmsMessage.class))).thenReturn(smsMessage);

        invokeProcessMessage(sqsMessage);

        verify(campaignSmsConsumer).processSmsMessage(smsMessage);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void doesNotDeleteMessageWhenConsumerThrows() throws Exception {
        CampaignSmsMessage smsMessage = CampaignSmsMessage.builder()
                .channel(CampaignChannel.SMS)
                .campaignId("camp-sms")
                .toPhone("+15551234567")
                .build();
        Message sqsMessage = sqsMessage("{\"channel\":\"SMS\"}");

        when(objectMapper.readValue(eq(sqsMessage.body()), eq(CampaignSmsMessage.class))).thenReturn(smsMessage);
        doThrow(new RuntimeException("Twilio down"))
                .when(campaignSmsConsumer).processSmsMessage(smsMessage);

        invokeProcessMessage(sqsMessage);

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    private void invokeProcessMessage(Message message) throws Exception {
        Method method = CampaignSmsListener.class.getDeclaredMethod("processMessage", Message.class);
        method.setAccessible(true);
        method.invoke(listener, message);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Message sqsMessage(String body) {
        return Message.builder()
                .messageId("msg-1")
                .receiptHandle("receipt-1")
                .body(body)
                .build();
    }
}
