package com.aspire.asat.registration.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommonUtils {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*";
    private static final String ALL = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
    private static final int DEFAULT_TEMP_PASSWORD_LENGTH = 12;
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    private CommonUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateStringId() {
        return UUID.randomUUID().toString();
    }

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }

    /**
     * Generates a temporary password that satisfies policy: length 8–64, at least one
     * uppercase, one lowercase, one digit, and one special character.
     */
    public static String generateTemporaryPassword() {
        return generateTemporaryPassword(DEFAULT_TEMP_PASSWORD_LENGTH);
    }

    /**
     * Generates a temporary password with the given length (clamped to 8–64).
     * Guarantees at least one uppercase, one lowercase, one digit, and one special character.
     */
    public static String generateTemporaryPassword(int length) {
        int len = Math.min(MAX_LENGTH, Math.max(MIN_LENGTH, length));
        Random random = new Random();
        StringBuilder sb = new StringBuilder(len);
        sb.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        sb.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        for (int i = 4; i < len; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }
        List<Character> chars = sb.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(chars);
        return chars.stream().map(String::valueOf).collect(Collectors.joining());
    }
}
