package com.aspire.asat.auth.serializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NameUtils {
    public static Map<String, String> splitFullName(String fullName) {
        Map<String, String> nameParts = new HashMap<>();
        if (fullName == null || fullName.trim().isEmpty()) {
            nameParts.put("firstName", "");
            nameParts.put("middleName", "");
            nameParts.put("lastName", "");
            return nameParts;
        }

        // Split by spaces and hyphens
        String[] parts = fullName.trim().split("[\\s-]+");
        nameParts.put("firstName", parts[0]);
        if (parts.length > 2) {
            nameParts.put("lastName", parts[parts.length - 1]);
            nameParts.put("middleName", String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 1)));
        } else if (parts.length == 2) {
            nameParts.put("lastName", parts[1]);
            nameParts.put("middleName", "");
        } else {
            nameParts.put("lastName", "");
            nameParts.put("middleName", "");
        }
        return nameParts;
    }
}
