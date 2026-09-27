package com.aspire.asat.phishing.constant;

import com.aspire.asat.phishing.dto.cms.CmsProductPackageMapping;

import java.util.List;

/**
 * Static defaults for the CMS Topic/Chapter/Content payloads used when generating micro content.
 *
 * <p>These are placeholder values (taken from the sample payloads) that will be replaced with real,
 * per-environment data later. Keeping them in one place makes that update trivial.</p>
 */
public final class MicroContentCmsDefaults {

    private MicroContentCmsDefaults() {
    }

    /**
     * Prefix for per-video CMS topic names:
     * {@code Micro Content of {adminFirstName}'s {videoTitle}}.
     */
    public static final String TOPIC_NAME_PREFIX = "Micro Content";
    public static final String TOPIC_DESCRIPTION = "Micro content";
    public static final String TOPIC_THUMBNAIL_URL = "";

    // ---- Topic required reference data (replace with real ids later) ----
    public static final List<String> CATEGORY_IDS = List.of("32f30a8e-5f22-4ac4-bd18-0e4e6772cd28");
    public static final List<String> COUNTRY_IDS = List.of("6b125351-cd90-4447-8889-720a26acf3b2");
    public static final List<String> COMPLIANCE_IDS = List.of("b65f43a0-5bec-4bd6-8b73-f26fb1a848a2");
    public static final String CONTENT_TYPE_ID = "bffd6018-b3d8-4178-bdec-73f6be9530e1";
    public static final int DURATION_MINUTES = 5;

    // ---- Product package mapping (replace with real ids later) ----
    public static final String PRODUCT_ID = "31133f89-8f8a-4997-a8cd-319218a0b159";
    public static final String PRODUCT_NAME = "Phishing Simulation";
    public static final List<String> PACKAGE_IDS = List.of("1776588445659");

    // ---- Statuses ----
    public static final String TOPIC_STATUS = "ENABLED";
    public static final String CHAPTER_STATUS = "ENABLED";
    public static final String CONTENT_STATUS = "DRAFT";
    public static final String CONTENT_TYPE = "VIDEO";

    // ---- Chapter defaults ----
    public static final String CHAPTER_DESCRIPTION = "";

    // ---- Content background formatting ----
    public static final String BG_TEXT_COLOR = "#000000";
    public static final String BG_BACKGROUND_COLOR = "#ffffff00";
    public static final String BG_BACKGROUND_IMAGE = "";
    public static final String BG_BACKGROUND_OPACITY = "100";
    public static final String BG_TONE = "LIGHT";

    // ---- Content interactive video / metadata ----
    public static final String CAPTION_URL = "";
    /** Deepfake videos are already final — skip CMS VPS queue. */
    public static final boolean INTERACTIVE_VIDEO_PROCESSING = false;
    public static final String INTERACTIVE_VIDEO_PROCESSING_STATUS = "PROCESSED";
    public static final String METADATA_VIDEO_TEXT = "";

    /**
     * Builds the CMS topic name: {@code Micro Content of {firstName}'s {videoTitle}}.
     */
    public static String topicName(String firstName, String videoTitle) {
        String name = firstName == null ? "" : firstName.trim();
        String title = videoTitle == null ? "" : videoTitle.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("firstName is required for microContent topic naming");
        }
        if (title.isEmpty()) {
            throw new IllegalArgumentException("videoTitle is required for microContent topic naming");
        }
        return TOPIC_NAME_PREFIX + " of " + name + "'s " + title;
    }

    /**
     * Uniqueness fallback when {@link #topicName(String, String)} collides in CMS:
     * {@code Micro Content of {firstName}'s {videoTitle} ({deepfakeVideoId})}.
     */
    public static String topicNameWithVideoId(String firstName, String videoTitle, String deepfakeVideoId) {
        return withVideoIdSuffix(topicName(firstName, videoTitle), deepfakeVideoId);
    }

    /**
     * Appends {@code ({deepfakeVideoId})} to any topic name for CMS uniqueness retries.
     */
    public static String withVideoIdSuffix(String topicName, String deepfakeVideoId) {
        String base = topicName == null ? "" : topicName.trim();
        String id = deepfakeVideoId == null ? "" : deepfakeVideoId.trim();
        if (base.isEmpty()) {
            return id.isEmpty() ? "" : "(" + id + ")";
        }
        if (id.isEmpty()) {
            return base;
        }
        return base + " (" + id + ")";
    }

    /**
     * Resolves the CMS topic name: non-blank {@code customTopicName} wins; otherwise the
     * formatted default {@link #topicName(String, String)}.
     */
    public static String resolveTopicName(String customTopicName, String firstName, String videoTitle) {
        if (customTopicName != null && !customTopicName.trim().isEmpty()) {
            return customTopicName.trim();
        }
        return topicName(firstName, videoTitle);
    }

    /**
     * First whitespace token of a display name (client admin full name / full name / id).
     */
    public static String firstNameFrom(String displayName) {
        String trimmed = displayName == null ? "" : displayName.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }

    /**
     * @return a fresh list of the default product-package mappings for the topic payload.
     */
    public static List<CmsProductPackageMapping> productPackages() {
        return List.of(CmsProductPackageMapping.builder()
                .productId(PRODUCT_ID)
                .productName(PRODUCT_NAME)
                .packageIds(PACKAGE_IDS)
                .build());
    }
}
