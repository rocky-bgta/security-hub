package com.aspire.asat.phishing.service.impl.importer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class CookieJar {
    private final Map<String, String> cookies = new LinkedHashMap<>();

    public void capture(List<String> setCookies) {
        if (setCookies == null) {
            return;
        }
        for (String raw : setCookies) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String cookiePart = raw.split(";", 2)[0].trim();
            int idx = cookiePart.indexOf('=');
            if (idx > 0) {
                String name = cookiePart.substring(0, idx).trim();
                String value = cookiePart.substring(idx + 1).trim();
                cookies.put(name, value);
            }
        }
    }

    public String asHeader() {
        StringJoiner joiner = new StringJoiner("; ");
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            joiner.add(entry.getKey() + "=" + entry.getValue());
        }
        return joiner.toString();
    }

    public boolean isEmpty() {
        return cookies.isEmpty();
    }

    public int size() {
        return cookies.size();
    }
}

