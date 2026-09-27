package com.aspire.asat.common.config;



import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcsConfig {
    @Bean Storage gcsStorage() { return StorageOptions.getDefaultInstance().getService(); }
}
