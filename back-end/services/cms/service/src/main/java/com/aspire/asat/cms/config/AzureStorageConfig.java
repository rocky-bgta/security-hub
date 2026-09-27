package com.aspire.asat.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    public String getConnectionString() {
        return connectionString;
    }

    public String getContainerName() {
        return containerName;
    }

}
