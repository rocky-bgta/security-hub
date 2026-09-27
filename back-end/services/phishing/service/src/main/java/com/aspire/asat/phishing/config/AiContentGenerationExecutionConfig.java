package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Threading beans for the AI content-generation SQS pipeline.
 *
 * <p>Mirrors {@link CampaignEmailExecutionConfig}: a dedicated single-threaded scheduler
 * drives the polling loop while a separate worker pool runs the actual AI vendor calls so
 * a slow generation does not stall polling.
 */
@Configuration
public class AiContentGenerationExecutionConfig {

    @Bean(name = "aiContentGenerationScheduler")
    public TaskScheduler aiContentGenerationScheduler(
            @Value("${aws.sqs.ai-content-generation-consumer.scheduler-pool-size:1}") int schedulerPoolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, schedulerPoolSize));
        scheduler.setThreadNamePrefix("ai-content-generation-scheduler-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }

    @Bean(name = "aiContentGenerationWorkerExecutor")
    public Executor aiContentGenerationWorkerExecutor(
            @Value("${ai.generation.worker.pool-size:4}") int poolSize,
            @Value("${ai.generation.worker.queue-capacity:50}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Math.max(1, poolSize));
        executor.setMaxPoolSize(Math.max(1, poolSize));
        executor.setQueueCapacity(Math.max(1, queueCapacity));
        executor.setThreadNamePrefix("ai-content-generation-worker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
