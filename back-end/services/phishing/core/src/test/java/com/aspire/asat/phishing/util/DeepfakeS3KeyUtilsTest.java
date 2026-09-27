package com.aspire.asat.phishing.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepfakeS3KeyUtilsTest {

    @Test
    void normalizeKey_plainKey_unchanged() {
        assertEquals(
                "deepfake/background/d58cbf9e-d7b7-4a44-88dc-82fc9773c088-backkground.jpg",
                DeepfakeS3KeyUtils.normalizeKey(
                        "deepfake/background/d58cbf9e-d7b7-4a44-88dc-82fc9773c088-backkground.jpg"));
    }

    @Test
    void normalizeKey_presignedVirtualHostedUrl_extractsObjectKey() {
        String url = "https://asatv2-media-bucket.s3.amazonaws.com/deepfake/background/"
                + "d58cbf9e-d7b7-4a44-88dc-82fc9773c088-backkground.jpg"
                + "?X-Amz-Algorithm=AWS4-HMAC-SHA256"
                + "&X-Amz-Date=20260728T162246Z"
                + "&X-Amz-SignedHeaders=host"
                + "&X-Amz-Credential=AKIAEXAMPLE%2F20260728%2Fus-east-1%2Fs3%2Faws4_request"
                + "&X-Amz-Expires=3600"
                + "&X-Amz-Signature=14a7fc59e0e41a39a707e2f4c6b9b258971e382ee9551bd29d97953a6d2fe8e0";

        assertEquals(
                "deepfake/background/d58cbf9e-d7b7-4a44-88dc-82fc9773c088-backkground.jpg",
                DeepfakeS3KeyUtils.normalizeKey(url));
    }

    @Test
    void normalizeKey_doubleWrappedPresignUrl_extractsObjectKey() {
        String inner = "https://asatv2-media-bucket.s3.amazonaws.com/deepfake/background/"
                + "d58cbf9e-d7b7-4a44-88dc-82fc9773c088-backkground.jpg"
                + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=inner";
        String outer = "https://asatv2-media-bucket.s3.amazonaws.com/"
                + java.net.URLEncoder.encode(inner, java.nio.charset.StandardCharsets.UTF_8)
                + "?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=outer";

        assertEquals(
                "deepfake/background/d58cbf9e-d7b7-4a44-88dc-82fc9773c088-backkground.jpg",
                DeepfakeS3KeyUtils.normalizeKey(outer));
    }

    @Test
    void normalizeKey_pathStyleUrl_stripsBucket() {
        String url = "https://s3.us-east-1.amazonaws.com/asatv2-media-bucket/deepfake/background/bg.jpg";

        assertEquals(
                "deepfake/background/bg.jpg",
                DeepfakeS3KeyUtils.normalizeKey(url, "asatv2-media-bucket"));
    }

    @Test
    void looksLikeHttpUrl_detectsPlainAndEncoded() {
        assertTrue(DeepfakeS3KeyUtils.looksLikeHttpUrl("https://example.com/a"));
        assertTrue(DeepfakeS3KeyUtils.looksLikeHttpUrl("https%3A//example.com/a"));
        assertFalse(DeepfakeS3KeyUtils.looksLikeHttpUrl("deepfake/background/a.jpg"));
    }
}
