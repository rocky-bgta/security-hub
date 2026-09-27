package com.aspire.asat.phishing.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VishingScriptPlaceholderUtils {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*\\}\\}");

    private VishingScriptPlaceholderUtils() {
    }

    public static List<String> detectVariables(String scriptBody) {
        if (scriptBody == null || scriptBody.isBlank()) {
            return List.of();
        }
        Set<String> vars = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(scriptBody);
        while (matcher.find()) {
            vars.add("{{" + matcher.group(1) + "}}");
        }
        return new ArrayList<>(vars);
    }

    public static String render(String scriptBody, java.util.Map<String, String> values) {
        if (scriptBody == null) {
            return null;
        }
        if (values == null || values.isEmpty()) {
            return scriptBody;
        }
        String result = scriptBody;
        for (var entry : values.entrySet()) {
            String key = entry.getKey();
            String placeholder = key.startsWith("{{") ? key : "{{" + key + "}}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}
