package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class CampaignVoiceExecutionConfig {

    @Bean(name = "campaignVoiceScheduler")
    public TaskScheduler campaignVoiceScheduler(
            @Value("${aws.sqs.campaign-voice-consumer.scheduler-pool-size:1}") int schedulerPoolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, schedulerPoolSize));
        scheduler.setThreadNamePrefix("campaign-voice-scheduler-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}
