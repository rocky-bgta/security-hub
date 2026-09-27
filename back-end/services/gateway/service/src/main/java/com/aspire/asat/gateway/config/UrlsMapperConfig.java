package com.aspire.asat.gateway.config;

import com.aspire.asat.gateway.dto.ApiPermissionRule;
import com.aspire.asat.gateway.dto.PublicUrls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class UrlsMapperConfig {
    private static final Logger logger = LoggerFactory.getLogger(UrlsMapperConfig.class);

    /**
     * Dynamically loads all permission JSON files from the 'resources/urls' directory,
     * merges them, and provides a single map of all service permissions.
     * This allows for adding new service permission files without changing Java code.
     *
     * @return A map where the key is the service name and the value is its list of permission rules.
     */
    @Bean(name = "servicePermissionsMap")
    public Map<String, List<ApiPermissionRule>> servicePermissionsMap(ObjectMapper objectMapper) {
        Map<String, List<ApiPermissionRule>> allPermissions = new HashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:urls/*.json");
            logger.info("Found {} permission files in 'resources/urls'.", resources.length);

            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null || filename.equals("public-urls.json")) {
                    logger.debug("Skipping public URLs file: {}", filename);
                    continue;
                }

                try (InputStream inputStream = resource.getInputStream()) {
                    TypeReference<Map<String, List<ApiPermissionRule>>> typeReference = new TypeReference<>() {
                    };
                    Map<String, List<ApiPermissionRule>> singleFilePermissions = objectMapper.readValue(inputStream, typeReference);
                    logger.info("Loading permissions from: {}", filename);
                    allPermissions.putAll(singleFilePermissions);
                } catch (IOException e) {
                    logger.error("Failed to load permission file: {}. Skipping.", filename, e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan for permission files in 'resources/urls'.", e);
            return Collections.emptyMap();
        }

        logger.info("Successfully loaded and merged permission rules for {} services.", allPermissions.size());
        //service wise api count log here
        allPermissions.forEach((service, rules) ->
                logger.info("Service '{}' has {} API permission rules.", service, rules.size())
        );
        return allPermissions;
    }

    @Bean(name = "publicUrls")
    public PublicUrls publicUrls(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource("urls/public-urls.json").getInputStream()) {
            logger.info("Loading public URLs from public-urls.json");
            return objectMapper.readValue(inputStream, PublicUrls.class);
        } catch (IOException e) {
            logger.error("Failed to load public-urls.json. Public URL checks will not work.", e);
            return new PublicUrls();
        }
    }
}
