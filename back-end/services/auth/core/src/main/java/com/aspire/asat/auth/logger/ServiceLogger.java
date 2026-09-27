package com.aspire.asat.auth.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceLogger {

    private final Logger errorLogger;
    private final Logger traceLogger;

    public ServiceLogger() {
        this.errorLogger = LoggerFactory.getLogger("errorLogger");
        this.traceLogger = LoggerFactory.getLogger("traceLogger");
    }

    public void trace(String message) {
        traceLogger.trace(message);
    }

    public void error(String message) {
        errorLogger.error(message);
    }

    public void trace(String message, Object... args) {
        traceLogger.trace(message, args);
    }

    public void error(String message, Object... args) {
        errorLogger.error(message, args);
    }

    public void error(String message, Exception ex) {
        errorLogger.error(message, ex);
    }

    public void info(String message) {
        traceLogger.info(message);
    }

    public void info(String message, Object... args) {
        traceLogger.info(message, args);
    }

    public void warn(String message) {
        traceLogger.warn(message);
    }

    public void warn(String message, Object... args) {
        traceLogger.warn(message, args);
    }
}
