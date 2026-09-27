package com.aspire.asat.phishing.util;

import java.util.Map;

public final class DeepfakeScriptUtils {

    private DeepfakeScriptUtils() {
    }

    public static String substituteVariables(String script, Map<String, String> variables) {
        if (script == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return script;
        }
        String result = script;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace("[" + entry.getKey() + "]", value);
        }
        return result;
    }
}
