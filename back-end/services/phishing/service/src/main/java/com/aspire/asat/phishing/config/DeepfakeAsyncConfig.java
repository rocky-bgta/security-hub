package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class DeepfakeAsyncConfig {

    @Bean(name = "deepfakeRenderWorkerExecutor")
    public Executor deepfakeRenderWorkerExecutor(
            @Value("${deepfake.worker.pool-size:4}") int poolSize,
            @Value("${deepfake.worker.queue-capacity:50}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("deepfake-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean(name = "microContentWorkerExecutor")
    public Executor microContentWorkerExecutor(
            @Value("${deepfake.micro-content.worker.pool-size:2}") int poolSize,
            @Value("${deepfake.micro-content.worker.queue-capacity:50}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("micro-content-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
