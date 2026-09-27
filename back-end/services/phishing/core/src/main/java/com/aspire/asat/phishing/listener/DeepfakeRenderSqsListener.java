package com.aspire.asat.phishing.listener;

import com.aspire.asat.phishing.service.DeepfakeRenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeepfakeRenderSqsListener {

    private final SqsClient sqsClient;
    private final DeepfakeRenderService deepfakeRenderService;
    @Qualifier("deepfakeRenderWorkerExecutor")
    private final Executor deepfakeRenderWorkerExecutor;

    @Value("${aws.sqs.deepfake-render-queue-url}")
    private String queueUrl;

    @Scheduled(fixedDelayString = "${deepfake.poll-interval-ms:3000}")
    public void pollMessages() {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(5)
                    .waitTimeSeconds(10)
                    .build();
            List<Message> messages = sqsClient.receiveMessage(request).messages();
            for (Message message : messages) {
                deepfakeRenderWorkerExecutor.execute(() -> process(message));
            }
        } catch (Exception e) {
            log.error("Failed polling deepfake render queue {}", queueUrl, e);
        }
    }

    private void process(Message message) {
        try {
            if (deepfakeRenderService.processRender(message)) {
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            }
        } catch (Exception e) {
            // Intentionally no delete. SQS retry + DLQ policy will handle failures.
            log.error("Failed processing deepfake render SQS messageId={}", message.messageId(), e);
        }
    }
}
