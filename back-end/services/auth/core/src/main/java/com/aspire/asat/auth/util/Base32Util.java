package com.aspire.asat.auth.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Base32 encoding/decoding utility for TOTP secrets
 * Implements RFC 4648 Base32 encoding
 */
@UtilityClass
public class Base32Util {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final Map<Character, Integer> BASE32_DECODE_MAP = new HashMap<>();

    static {
        for (int i = 0; i < BASE32_CHARS.length(); i++) {
            BASE32_DECODE_MAP.put(BASE32_CHARS.charAt(i), i);
        }
    }

    /**
     * Decode Base32 string to byte array
     *
     * @param base32 Base32 encoded string
     * @return Decoded byte array
     */
    public static byte[] decode(String base32) {
        base32 = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        
        if (base32.isEmpty()) {
            return new byte[0];
        }

        int paddingCount = 0;
        for (int i = base32.length() - 1; i >= 0 && base32.charAt(i) == '='; i--) {
            paddingCount++;
        }

        int binaryLength = (base32.length() - paddingCount) * 5 / 8;
        byte[] result = new byte[binaryLength];

        int buffer = 0;
        int bitsRemaining = 0;
        int resultIndex = 0;

        for (char c : base32.toCharArray()) {
            if (c == '=') {
                break;
            }

            Integer value = BASE32_DECODE_MAP.get(c);
            if (value == null) {
                throw new IllegalArgumentException("Invalid Base32 character: " + c);
            }

            buffer = (buffer << 5) | value;
            bitsRemaining += 5;

            if (bitsRemaining >= 8) {
                result[resultIndex++] = (byte) (buffer >> (bitsRemaining - 8));
                bitsRemaining -= 8;
            }
        }

        return result;
    }

    /**
     * Encode byte array to Base32 string
     *
     * @param data Byte array to encode
     * @return Base32 encoded string
     */
    public static String encode(byte[] data) {
        if (data.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int bitsRemaining = 0;

        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsRemaining += 8;

            while (bitsRemaining >= 5) {
                int index = (buffer >> (bitsRemaining - 5)) & 0x1F;
                result.append(BASE32_CHARS.charAt(index));
                bitsRemaining -= 5;
            }
        }

        if (bitsRemaining > 0) {
            int index = (buffer << (5 - bitsRemaining)) & 0x1F;
            result.append(BASE32_CHARS.charAt(index));
        }

        // Add padding
        while (result.length() % 8 != 0) {
            result.append('=');
        }

        return result.toString();
    }
}

