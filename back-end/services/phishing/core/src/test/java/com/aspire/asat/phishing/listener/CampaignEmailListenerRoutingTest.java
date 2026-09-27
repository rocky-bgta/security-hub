package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.dto.sqs.CampaignEmailMessage;
import com.aspire.asat.phishing.service.impl.CampaignEmailConsumer;
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
class CampaignEmailListenerRoutingTest {

    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123/phishing-campaign-emails-dev-queue";

    @Mock
    private SqsClient sqsClient;

    @Mock
    private CampaignEmailConsumer campaignEmailConsumer;

    @Mock
    private ObjectMapper objectMapper;

    private CampaignEmailListener listener;

    @BeforeEach
    void setUp() throws Exception {
        listener = new CampaignEmailListener(sqsClient, campaignEmailConsumer, objectMapper);
        setField(listener, "queueUrl", QUEUE_URL);
    }

    @Test
    void processesLegacyEmailMessage() throws Exception {
        CampaignEmailMessage emailMessage = CampaignEmailMessage.builder()
                .campaignId("camp-1")
                .recipientId("rec-1")
                .toEmail("user@example.com")
                .build();
        Message sqsMessage = sqsMessage("{\"campaignId\":\"camp-1\"}");

        when(objectMapper.readValue(eq(sqsMessage.body()), eq(CampaignEmailMessage.class))).thenReturn(emailMessage);

        invokeProcessMessage(sqsMessage);

        verify(campaignEmailConsumer).processEmailMessage(emailMessage);
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void doesNotDeleteMessageWhenConsumerThrows() throws Exception {
        CampaignEmailMessage emailMessage = CampaignEmailMessage.builder()
                .campaignId("camp-1")
                .recipientId("rec-1")
                .toEmail("user@example.com")
                .build();
        Message sqsMessage = sqsMessage("{\"campaignId\":\"camp-1\"}");

        when(objectMapper.readValue(eq(sqsMessage.body()), eq(CampaignEmailMessage.class))).thenReturn(emailMessage);
        doThrow(new RuntimeException("SMTP down"))
                .when(campaignEmailConsumer).processEmailMessage(emailMessage);

        invokeProcessMessage(sqsMessage);

        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    private void invokeProcessMessage(Message message) throws Exception {
        Method method = CampaignEmailListener.class.getDeclaredMethod("processMessage", Message.class);
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
