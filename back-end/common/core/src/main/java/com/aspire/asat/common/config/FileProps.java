package com.aspire.asat.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class FileProps {

    private Files files;
    private Aws aws;            // app.aws (bucket/region)
    private Azures azures;
    private Gcp gcp;
    private Storage storage;

    @Data
    public static class Files {
        private int presignTtlSeconds;
        private Long maxSizeBytes;
        private List<String> allowedExtensions;
        private String defaultContentType = "application/octet-stream";
        private Map<String, String> contentTypeMap;
    }

    @Data
    public static class Aws {
        private String bucket;
        private String region;
        private String accessKeyId;
        private String secretAccessKey;
        private CloudFront cloudFront;


    }

    @Data
    public static class CloudFront {
        private String distributionDomain;
        private String keyPairId;
        private String privateKeyPem;
        private String distributionMainDomain;
    }

    @Data
    public static class Azures {
        private StorageProps storage;

        @Data
        public static class StorageProps {
            private String connectionString; // binds connection-string
            private String containerName;    // binds container-name
        }
    }

    @Data
    public static class Gcp {
        private String projectId;
        private String bucket;
    }

    @Data
    public static class Storage {
        /** s3 | azure | gcp */
        private String active = "s3";
    }
}
