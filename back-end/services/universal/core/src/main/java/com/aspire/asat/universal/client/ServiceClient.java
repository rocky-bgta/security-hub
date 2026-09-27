package com.aspire.asat.universal.client;

/**
 * Base interface for service clients
 * Implement this interface for type-safe service clients
 * 
 * @param <T> The response type this client primarily handles
 */
public interface ServiceClient<T> {
    
    /**
     * Get the base URL for this service
     * @return The base URL
     */
    String getBaseUrl();
    
    /**
     * Get the service name for logging purposes
     * @return The service name
     */
    default String getServiceName() {
        return this.getClass().getSimpleName();
    }
}

