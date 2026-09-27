package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class CampaignEmailExecutionConfig {

    @Bean(name = "campaignEmailScheduler")
    public TaskScheduler campaignEmailScheduler(
            @Value("${aws.sqs.campaign-email-consumer.scheduler-pool-size:1}") int schedulerPoolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, schedulerPoolSize));
        scheduler.setThreadNamePrefix("campaign-email-scheduler-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}

