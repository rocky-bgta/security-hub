package com.aspire.asat.phishing.util;

import java.util.Locale;

/**
 * Canonicalizes free-text provider names from the frontend or stored credentials
 * so {@code "FISH AUDIO"}, {@code "fish-audio"}, and {@code "FISH_AUDIO"} all match
 * the same enum constant.
 */
public final class ProviderNameNormalizer {

    private ProviderNameNormalizer() {
    }

    /**
     * Uppercases, trims, and turns spaces/hyphens into underscores.
     * Returns {@code null} when the input is blank.
     */
    public static String canonicalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replaceAll("\\s+", "_");
    }

    /**
     * Resolves {@code raw} to an enum constant, matching both underscored names
     * ({@code FISH_AUDIO}) and compacted names ({@code ELEVENLABS} vs {@code ELEVEN LABS}).
     *
     * @return the matching constant, or {@code null} if {@code raw} is blank or unmatched
     */
    public static <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        String canonical = canonicalize(raw);
        if (canonical == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, canonical);
        } catch (IllegalArgumentException ignored) {
            String compact = canonical.replace("_", "");
            for (E value : type.getEnumConstants()) {
                if (value.name().replace("_", "").equals(compact)) {
                    return value;
                }
            }
            return null;
        }
    }
}
