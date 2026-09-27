package com.aspire.asat.cms.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalizes {@code specificContent} for API responses: {@code interactiveVideoByLanguage}
 * is stored as a map (language key → video) but clients expect a JSON array.
 * <p>
 * Used by create/update/get content and get chapter-by-id so responses always return
 * {@code interactiveVideoByLanguage} as an array when the field is present.
 */
public final class ContentSpecificResponseNormalizer {

    private ContentSpecificResponseNormalizer() {
    }

    /**
     * Converts persisted {@code specific} (Map, BSON Document, POJO, etc.) to a tree of maps/lists,
     * then ensures every {@code interactiveVideoByLanguage} in the tree is a JSON array.
     */
    public static Object normalizeForJsonResponse(ObjectMapper objectMapper, Object specific) {
        if (specific == null) {
            return null;
        }
        try {
            Map<String, Object> root = objectMapper.convertValue(specific, new TypeReference<Map<String, Object>>() {});
            deepNormalizeInteractiveVideoByLanguage(root);
            return root;
        } catch (IllegalArgumentException e) {
            if (specific instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) specific;
                deepNormalizeInteractiveVideoByLanguage(map);
                return map;
            }
            return specific;
        }
    }

    /**
     * Single-level normalization (legacy / tests). Prefer {@link #normalizeForJsonResponse}.
     */
    @SuppressWarnings("unchecked")
    public static Object normalizeInteractiveVideoByLanguageForResponse(Object specific) {
        if (specific == null) {
            return null;
        }
        if (!(specific instanceof Map)) {
            return specific;
        }
        deepNormalizeInteractiveVideoByLanguage((Map<String, Object>) specific);
        return specific;
    }

    /**
     * Recursively walks maps/lists and converts {@code interactiveVideoByLanguage} from Map to List
     * wherever the key is present.
     */
    @SuppressWarnings("unchecked")
    private static void deepNormalizeInteractiveVideoByLanguage(Map<String, Object> map) {
        if (map.containsKey("interactiveVideoByLanguage")) {
            Object byLang = map.get("interactiveVideoByLanguage");
            if (byLang instanceof Map) {
                List<Map<String, Object>> array = mapToVideoArray((Map<String, Object>) byLang);
                map.put("interactiveVideoByLanguage", array);
            } else if (byLang != null && !(byLang instanceof List)) {
                map.put("interactiveVideoByLanguage", new ArrayList<>());
            }
        }

        for (Map.Entry<String, Object> e : map.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map) {
                deepNormalizeInteractiveVideoByLanguage((Map<String, Object>) v);
            } else if (v instanceof List) {
                for (Object item : (List<?>) v) {
                    if (item instanceof Map) {
                        deepNormalizeInteractiveVideoByLanguage((Map<String, Object>) item);
                    }
                }
            }
        }
    }

    private static List<Map<String, Object>> mapToVideoArray(Map<String, Object> langMap) {
        List<Map<String, Object>> array = new ArrayList<>();
        for (Map.Entry<String, Object> e : langMap.entrySet()) {
            if (e.getValue() instanceof Map) {
                Map<String, Object> videoMap = new LinkedHashMap<>((Map<String, Object>) e.getValue());
                if (!videoMap.containsKey("language") || videoMap.get("language") == null) {
                    videoMap.put("language", e.getKey());
                }
                array.add(videoMap);
            }
        }
        return array;
    }
}
