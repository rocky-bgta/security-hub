package com.aspire.asat.universal.utils;

import java.util.Random;
import java.util.UUID;

public class CommonUtils {
    private CommonUtils() {
        throw new IllegalStateException("Utility class");
    }


    public static String generateStringId() {
        return UUID.randomUUID().toString();
    }

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }

    public static String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
