package com.aspire.asat.vps.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    public String getConnectionString() {
        return connectionString;
    }

}
