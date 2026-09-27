package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.sqs.CampaignSmsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignSmsProducerQueueTest {

    private static final String SMS_QUEUE_URL =
            "https://sqs.us-east-1.amazonaws.com/123/phishing-campaign-sms-dev-queue";

    private static final String SMS_MESSAGE_JSON = """
            {"channel":"SMS","campaignId":"camp-1","recipientId":"rec-1",\
            "toPhone":"+15551234567","messageBody":"Click link"}""";

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    private CampaignSmsProducer producer;

    @BeforeEach
    void setUp() throws Exception {
        producer = new CampaignSmsProducer(
                sqsClient,
                objectMapper,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        setField(producer, "queueUrl", SMS_QUEUE_URL);
        setField(producer, "publishMaxAttempts", 1);
        setField(producer, "publishInitialBackoffMs", 1L);
    }

    @Test
    void publishesSmsMessageToSmsQueue() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn(SMS_MESSAGE_JSON);
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("sqs-1").build());

        CampaignSmsMessage message = CampaignSmsMessage.builder()
                .channel(CampaignChannel.SMS)
                .campaignId("camp-1")
                .recipientId("rec-1")
                .toPhone("+15551234567")
                .messageBody("Click link")
                .build();

        Method publishToSqs = CampaignSmsProducer.class.getDeclaredMethod(
                "publishToSqs", CampaignSmsMessage.class, int.class);
        publishToSqs.setAccessible(true);
        boolean published = (boolean) publishToSqs.invoke(producer, message, 0);
        assertTrue(published);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());
        assertEquals(SMS_QUEUE_URL, captor.getValue().queueUrl());
        assertTrue(captor.getValue().messageBody().contains("\"channel\":\"SMS\""));
        assertTrue(captor.getValue().messageBody().contains("\"toPhone\":\"+15551234567\""));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
