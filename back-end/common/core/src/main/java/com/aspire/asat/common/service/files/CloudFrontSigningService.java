package com.aspire.asat.common.service.files;


import com.aspire.asat.common.config.FileProps;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class CloudFrontSigningService {

    private final FileProps fileProps;
    private final Environment environment;
    private CloudFrontUtilities cfUtilities;

    public CloudFrontSigningService(FileProps fileProps, Environment environment) {
        this.fileProps = fileProps;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        cfUtilities = CloudFrontUtilities.create();
    }

    public Map<String, String> generateSignedCookies(Duration validFor) throws Exception {
        String distributionDomain = fileProps.getAws().getCloudFront().getDistributionDomain();
        String keyPairId = fileProps.getAws().getCloudFront().getKeyPairId();
        String configuredPrivateKey = fileProps.getAws().getCloudFront().getPrivateKeyPem();
        boolean isLocalProfile = isLocalProfileSelected();
        String privateKeyValue;

        if (isLocalProfile) {
            // Local profile should always load from bundled key file.
            privateKeyValue = "classpath:private_key.pem";
        } else {
            // Non-local profiles should use PEM content loaded from Parameter Store.
            if (!StringUtils.hasText(configuredPrivateKey)) {
                throw new IllegalStateException("CloudFront private key is required from Parameter Store for non-local profiles");
            }
            if (configuredPrivateKey.startsWith("classpath:")) {
                throw new IllegalStateException("Classpath private key is not allowed for non-local profiles. Use Parameter Store value");
            }
            privateKeyValue = configuredPrivateKey;
        }

        Path pemPath;
        if (privateKeyValue.startsWith("classpath:")) {
            String resourcePath = privateKeyValue.substring("classpath:".length());
            ClassPathResource resource = new ClassPathResource(resourcePath);
            pemPath = Files.createTempFile("cloudfront-key", ".pem");
            Files.copy(resource.getInputStream(), pemPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } else if (privateKeyValue.startsWith("-----BEGIN")) {
            // PEM content directly from property/SSM
            String pemContent = privateKeyValue.replace("\\n", System.lineSeparator());
            pemPath = Files.createTempFile("cloudfront-key", ".pem");
            Files.writeString(pemPath, pemContent);
        } else {
            // fallback: assume filesystem path
            pemPath = Path.of(privateKeyValue);
        }

        Instant expiresAt = Instant.now().plus(validFor);

        CustomSignerRequest customRequest = CustomSignerRequest.builder()
                .resourceUrl("https://" + distributionDomain + "/*")
                .privateKey(pemPath)
                .keyPairId(keyPairId)
                .expirationDate(expiresAt)
                .build();

        CookiesForCustomPolicy cookies = cfUtilities.getCookiesForCustomPolicy(customRequest);

        // Strip "CloudFront-Policy=" etc. from the values
        return Map.of(
                "CloudFront-Policy", stripPrefix(cookies.policyHeaderValue()),
                "CloudFront-Signature", stripPrefix(cookies.signatureHeaderValue()),
                "CloudFront-Key-Pair-Id", stripPrefix(cookies.keyPairIdHeaderValue())
        );
    }

    private String stripPrefix(String headerValue) {
        int idx = headerValue.indexOf('=');
        return (idx >= 0) ? headerValue.substring(idx + 1) : headerValue;
    }

    private boolean isLocalProfileSelected() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles == null || activeProfiles.length == 0) {
            return true;
        }
        for (String profile : activeProfiles) {
            if ("local".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

}




