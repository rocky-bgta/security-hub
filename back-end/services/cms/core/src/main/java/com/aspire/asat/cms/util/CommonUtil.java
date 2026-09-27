package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.interactive.InteractiveVideo;
import com.aspire.asat.cms.dto.interactive.SpecificContent;
import com.aspire.asat.common.service.files.FileService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class CommonUtil {

    /**
     * Converts page number and page size to offset.
     */
    public static int getOffset(int page, int pageSize) {
        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Page must be >= 0 and pageSize must be > 0");
        }
        return page * pageSize;
    }

    /**
     * Converts ISO datetime string to formatted date (e.g., "dd-MM-yyyy HH:mm").
     */
    public static String formatDate(String isoDateStr, String outputFormat) {
        try {
            Instant instant = Instant.parse(isoDateStr);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            return DateTimeFormatter.ofPattern(outputFormat).format(zonedDateTime);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get current timestamp as ISO string.
     */
    public static String getCurrentIsoTimestamp() {
        return Instant.now().toString();
    }

    /**
     * Converts epoch milliseconds to readable date.
     */
    public static String epochToDate(long epochMillis, String format) {
        try {
            return Instant.ofEpochMilli(epochMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generates a UUID string.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }


    public static String getLoggedInUser() {
        // Placeholder for actual user retrieval logic
        return "system";
    }

    /**
     * Checks if a string is null or blank.
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Safely parses string to integer with fallback.
     */
    public static int safeParseInt(String str, int fallback) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Safely parses string to boolean.
     */
    public static boolean safeParseBoolean(String str) {
        return "true".equalsIgnoreCase(str);
    }

    @SuppressWarnings("unchecked")
    public static Object enrichUrls(Object specificContent, FileService fileService) {
        if (specificContent == null) return null;

        // Case 1: already a Map
        if (specificContent instanceof Map) {
            Map<String, Object> specificMap = (Map<String, Object>) specificContent;
            if (specificMap.containsKey("interactiveVideo")) {
                Map<String, Object> interactive = (Map<String, Object>) specificMap.get("interactiveVideo");

                Object rawProcessVideoUrl = interactive.get("processVideoUrl");
                if (rawProcessVideoUrl != null) {
                    String processVideoUrl = fileService.getPath(rawProcessVideoUrl.toString());
                    if (processVideoUrl != null && !processVideoUrl.isBlank()) {
                        interactive.put("processVideoUrl", processVideoUrl);
                    }
                }
            }
            return specificMap;
        }

        // Case 2: a POJO
        if (specificContent instanceof SpecificContent) {
            SpecificContent sc = (SpecificContent) specificContent;
            if (sc.getInteractiveVideo() != null) {
                InteractiveVideo video = sc.getInteractiveVideo();

                if (video.getProcessVideoUrl() != null) {
                    String processed = fileService.getPath(video.getProcessVideoUrl());
                    if (processed != null && !processed.isBlank()) {
                        video.setProcessVideoUrl(processed);
                    }
                }
//                if (video.getVideoUrl() != null) {
//                    String videoUrl = fileService.getPath(video.getVideoUrl());
//                    if (videoUrl != null && !videoUrl.isBlank()) {
//                        video.setVideoUrl(videoUrl);
//                    }
//                }
            }
            return sc;
        }

        // Case 3: unknown type → just return unchanged
        return specificContent;
    }

}
