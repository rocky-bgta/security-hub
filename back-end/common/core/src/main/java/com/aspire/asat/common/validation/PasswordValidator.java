package com.aspire.asat.common.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Central password validation enforcing:
 * - Length 8-64
 * - At least 1 uppercase, 1 lowercase, 1 digit, 1 special character
 * - Not in common/weak password list
 * - Passphrase (14+ chars): must still meet complexity
 * - Optional: no personal info (email, name) when context provided
 * - No single dictionary word as password
 */
public final class PasswordValidator {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final int PASSPHRASE_MIN_LENGTH = 14;

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~]");

    private static final Set<String> COMMON_WEAK_PASSWORDS = new HashSet<>(Arrays.asList(
            "123456", "123456789", "12345678", "password", "qwerty123", "qwerty1", "111111", "12345", "secret", "123123",
            "password1", "password123", "admin", "admin123", "letmein", "welcome", "monkey", "dragon", "master", "qwerty",
            "login", "abc123", "111111", "1234567", "sunshine", "princess", "admin1", "welcome1", "shadow", "ashley",
            "football", "iloveyou", "1234", "1234567890", "trustno1", "access", "superman", "qazwsx", "michael",
            "password2", "batman", "654321", "joshua", "123456a", "andrew", "charlie", "donald", "passw0rd", "qwerty1234",
            "zaq1zaq1", "password!", "admin@123", "root", "toor", "pass", "test", "test123", "guest", "temp", "temp123",
            "changeme", "default", "summer", "winter", "spring", "autumn", "hello", "chocolate", "naruto", "chelsea",
            "jessica", "daniel", "anthony", "jennifer", "thomas", "robert", "michelle", "bluesky", "runsfast", "river",
            "flows", "quietly", "night", "company", "office", "user", "manager", "qwertyuiop", "asdfghjkl", "zxcvbnm",
            "password12", "password!", "P@ssw0rd", "P@ssword", "Admin123!", "Welcome1", "Welcome123"
    ));

    private static final Set<String> COMMON_DICTIONARY_WORDS = new HashSet<>(Arrays.asList(
            "password", "welcome", "monkey", "dragon", "master", "sunshine", "princess", "football", "iloveyou",
            "superman", "batman", "shadow", "ashley", "summer", "winter", "spring", "hello", "chocolate", "company"
    ));

    private PasswordValidator() {
    }

    /**
     * Validate password with default rules (no personal context).
     */
    public static PasswordValidationResult validate(String password) {
        return validate(password, PasswordValidationContext.empty());
    }

    /**
     * Validate password with optional context (email, name) to reject personal info.
     */
    public static PasswordValidationResult validate(String password, PasswordValidationContext context) {
        List<String> errors = new ArrayList<>();
        if (password == null) {
            return PasswordValidationResult.failure(List.of("Password is required."));
        }
        String pwd = password.trim();

        // 1. Length (enforce max first so over-long passwords are always rejected)
        if (pwd.length() > MAX_LENGTH) {
            return PasswordValidationResult.failure(
                    List.of("Password must not exceed " + MAX_LENGTH + " characters (current: " + pwd.length() + ")."));
        }
        if (pwd.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long.");
        }

        // 2. Character complexity
        if (!UPPERCASE.matcher(pwd).find()) {
            errors.add("Password must contain at least one uppercase letter (A-Z).");
        }
        if (!LOWERCASE.matcher(pwd).find()) {
            errors.add("Password must contain at least one lowercase letter (a-z).");
        }
        if (!DIGIT.matcher(pwd).find()) {
            errors.add("Password must contain at least one number (0-9).");
        }
        if (!SPECIAL.matcher(pwd).find()) {
            errors.add("Password must contain at least one special character (e.g. ! @ # $ % ^ & *).");
        }

        // 3. Common / weak passwords
        if (COMMON_WEAK_PASSWORDS.contains(pwd.toLowerCase(Locale.ROOT))) {
            errors.add("Password is too common or weak. Choose a stronger, unique password.");
        }

        // 4. Single dictionary word (avoid password composed solely of one common word)
        String lower = pwd.toLowerCase(Locale.ROOT);
        if (COMMON_DICTIONARY_WORDS.contains(lower)) {
            errors.add("Password cannot be a single common word. Use a mix of words, numbers and symbols.");
        }

        // 5. Passphrase: if 14+ chars, still must have complexity (already checked above)
        // No extra rule needed; length and complexity cover it.

        // 6. Personal information (when context provided)
        if (context != null) {
            if (context.getEmail() != null && !context.getEmail().isBlank()) {
                String emailPart = context.getEmail().trim().toLowerCase(Locale.ROOT).split("@")[0];
                if (emailPart.length() >= 3 && lower.contains(emailPart)) {
                    errors.add("Password should not contain your email or username.");
                }
                if (context.getEmail().length() >= 4 && lower.contains(context.getEmail().trim().toLowerCase(Locale.ROOT).replace("@", "").replace(".", ""))) {
                    errors.add("Password should not contain your email address.");
                }
            }
            if (context.getFirstName() != null && context.getFirstName().trim().length() >= 2) {
                String first = context.getFirstName().trim().toLowerCase(Locale.ROOT);
                if (lower.contains(first)) {
                    errors.add("Password should not contain your first name.");
                }
            }
            if (context.getLastName() != null && context.getLastName().trim().length() >= 2) {
                String last = context.getLastName().trim().toLowerCase(Locale.ROOT);
                if (lower.contains(last)) {
                    errors.add("Password should not contain your last name.");
                }
            }
            if (context.getUsername() != null && !context.getUsername().isBlank()) {
                String un = context.getUsername().trim().toLowerCase(Locale.ROOT).split("@")[0];
                if (un.length() >= 3 && lower.contains(un)) {
                    errors.add("Password should not contain your username.");
                }
            }
        }

        if (errors.isEmpty()) {
            return PasswordValidationResult.success();
        }
        return PasswordValidationResult.failure(errors);
    }

    /**
     * Quick check for backward compatibility (e.g. existing code that only needs valid/invalid).
     */
    public static boolean isValid(String password) {
        return validate(password).isValid();
    }

    /**
     * Single message describing policy (for UI or error messages).
     */
    public static String getPolicyMessage() {
        return "Password must be 8–64 characters with at least one uppercase letter, one lowercase letter, one number, and one special character. It must not be a common or weak password, and should not contain your name, email, or username.";
    }
}
