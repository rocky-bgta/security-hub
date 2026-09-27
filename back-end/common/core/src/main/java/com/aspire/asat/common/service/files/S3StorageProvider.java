package com.aspire.asat.common.service.files;

import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageProvider implements StorageProvider {

    private final FileProps props;
    private S3Client s3Client;
    private S3Presigner presigner;
    private final  CloudFrontSigningService cloudFrontSigningService;


    @PostConstruct
    public void init() {
        var accessKeyId = props.getAws() != null ? props.getAws().getAccessKeyId() : null;
        var secretAccessKey = props.getAws() != null ? props.getAws().getSecretAccessKey() : null;
        var region = Region.of(props.getAws().getRegion());

        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
            var creds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            this.s3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();

            this.presigner = S3Presigner.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .build();
            log.info("Initialized S3 client with explicit app.aws credentials");
            return;
        }

        // Fall back to AWS default credentials chain (env vars, profile, IAM role, etc.).
        var defaultProvider = DefaultCredentialsProvider.create();
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(defaultProvider)
                .build();

        this.presigner = S3Presigner.builder()
                .region(region)
                .credentialsProvider(defaultProvider)
                .build();
        log.info("Initialized S3 client with AWS default credentials provider chain");
    }

    @PreDestroy
    public void close() {
        if (s3Client != null) s3Client.close();
        if (presigner != null) presigner.close();
    }

    @Override
    public String providerName() {
        return "s3";
    }

    @Override
    public PresignedUrlGenerationResponse generatePresignedUrl(
            PresignedUrlGenerationRequest req, String fileId, String key, String contentType) {

        var put = PutObjectRequest.builder()
                .bucket(props.getAws().getBucket())
                .key(key)
                .contentType(contentType)
                .serverSideEncryption("AES256")
                .build();

        var presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(put)
                .signatureDuration(Duration.ofSeconds(props.getFiles().getPresignTtlSeconds()))
                .build();

        var presigned = presigner.presignPutObject(presignReq);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("x-amz-server-side-encryption", "AES256");

        return PresignedUrlGenerationResponse.builder()
                .provider(providerName())
                .fileId(fileId)
                .bucketOrContainer(props.getAws().getBucket())
                .key(key)
                .method("PUT")
                .url(presigned.url().toString())
                .objectUrl(getPath(key))
                .requiredHeaders(headers)
                .expiresAt(Instant.now().plusSeconds(props.getFiles().getPresignTtlSeconds()))
                .build();
    }

    @Override
    public FileUploadResponse uploadFile(String localFilePath, String key) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(props.getAws().getBucket())
                    .key(key)
                    .build();

            s3Client.putObject(putRequest, Paths.get(localFilePath));

            return FileUploadResponse.builder()
                    .provider(providerName())
                    .bucketOrContainer(props.getAws().getBucket())
                    .path(key)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("S3 upload failed for " + localFilePath, e);
        }
    }

    @Override
    public InputStream readFile(String containerOrBucket, String key) {
        try {
            return s3Client.getObject(builder -> builder.bucket(containerOrBucket).key(key));
        } catch (Exception e) {
            throw new RuntimeException("S3 read failed for bucket: " + containerOrBucket + ", key: " + key, e);
        }
    }

    public InputStream readFile(String key) {
        return readFile(props.getAws().getBucket(), key);
    }

    @Override
    public String buildUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(props.getAws().getBucket())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofHours(1))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public Map<String, String> generateSignedCookies(Duration validFor) {
        try {
            return cloudFrontSigningService.generateSignedCookies(validFor);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CloudFront signed cookies", e);
        }
    }

    @Override
    public String getFilePath(String key) {
        String bucket = props.getAws().getBucket();
        String region = props.getAws().getRegion();
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    @Override
    public String getPath(String key) {
        return "https://" + props.getAws().getCloudFront().getDistributionDomain() + "/"+ key;
    }
}
