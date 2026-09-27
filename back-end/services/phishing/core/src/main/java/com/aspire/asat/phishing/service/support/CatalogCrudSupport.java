package com.aspire.asat.phishing.service.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared helpers for Mongo-backed name/description catalog CRUD (payload types, tones, etc.).
 */
public final class CatalogCrudSupport {

    public static final Set<String> CONFIG_CATALOG_SORT_FIELDS = Set.of("displayOrder", "name", "createdAt");

    private CatalogCrudSupport() {
    }

    /**
     * Same semantics as email-templates list: {@code offset} is the page index (0-based), not a skip in items.
     */
    public static Pageable pageableForOffsetAsPageIndex(int offset, int pageSize, String sortBy, String sortOrder) {
        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        return PageRequest.of(pageNumber, effectivePageSize, sort(sortBy, sortOrder, CONFIG_CATALOG_SORT_FIELDS));
    }

    public static Sort sort(String sortBy, String sortOrder, Set<String> allowedFields) {
        String field = (sortBy != null && allowedFields.contains(sortBy)) ? sortBy : "createdAt";
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortOrder != null ? sortOrder : "desc");
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, field);
    }

    /** Case-insensitive substring match; regex metacharacters in user input are literal. */
    public static String nameContainsPattern(String trimmedSearch) {
        return ".*" + Pattern.quote(trimmedSearch) + ".*";
    }

    /** Case-insensitive exact name match; regex metacharacters in user input are literal. */
    public static String exactNamePattern(String trimmedName) {
        return "^" + Pattern.quote(trimmedName) + "$";
    }

    public static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
