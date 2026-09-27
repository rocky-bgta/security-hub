package com.aspire.asat.notification;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.aspire.asat", exclude = { SqsAutoConfiguration.class })
@EnableScheduling
@EnableAsync
public class NotificationApplication {
    public static void main(String[] args) {
        // SpringApplication.run(NotificationApplication.class, args);
        SpringApplication app = new SpringApplication(NotificationApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
