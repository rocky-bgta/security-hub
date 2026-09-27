package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class CampaignSmsExecutionConfig {

    @Bean(name = "campaignSmsScheduler")
    public TaskScheduler campaignSmsScheduler(
            @Value("${aws.sqs.campaign-sms-consumer.scheduler-pool-size:1}") int schedulerPoolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, schedulerPoolSize));
        scheduler.setThreadNamePrefix("campaign-sms-scheduler-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}
