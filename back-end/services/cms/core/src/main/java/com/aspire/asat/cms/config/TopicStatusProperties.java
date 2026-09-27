package com.aspire.asat.cms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "topic.status")
public class TopicStatusProperties {
    
    private Duration duration = new Duration();
    private RequiredQuestions requiredQuestions = new RequiredQuestions();
    
    @Data
    public static class Duration {
        private int shortThreshold = 5;
        private int longThreshold = 10;
    }
    
    @Data
    public static class RequiredQuestions {
        private int shortDuration = 5;
        private int longDuration = 10;
    }
}
