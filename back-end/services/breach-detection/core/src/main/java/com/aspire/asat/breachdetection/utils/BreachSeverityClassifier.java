package com.aspire.asat.breachdetection.utils;

import com.aspire.asat.breachdetection.dto.enums.BreachSeverity;
import com.aspire.asat.breachdetection.model.InsecureWebBreachFinding;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Derives a {@link BreachSeverity} for an email-related InsecureWeb finding using
 * the data we already persist. Used by the dashboard "Breach Activity" chart.
 *
 * Rules (highest match wins):
 *   CRITICAL - cleartext password leaked, OR financial / SSN / credit-card data exposed.
 *   HIGH     - hashed password leaked.
 *   MEDIUM   - personally identifiable info exposed (phone, address, DOB, etc).
 *   LOW      - email-only / username exposure.
 */
@Component
public class BreachSeverityClassifier {
    private static final Pattern CAMEL_CASE_SPLIT = Pattern.compile("([a-z])([A-Z])");

    private static final Set<String> PLAINTEXT_PASSWORD_TOKENS = Set.of(
            "plaintext password", "plain password", "cleartext password", "password");
    private static final Set<String> HASHED_PASSWORD_TOKENS = Set.of(
            "hashed password", "hash", "salt");
    private static final Set<String> CRITICAL_DATA_TOKENS = Set.of(
            "ssn", "social security", "credit card", "card", "cvv", "bank", "bank account",
            "account number", "iban", "swift", "routing number");
    private static final Set<String> MEDIUM_PII_TOKENS = Set.of(
            "phone", "phone number", "address", "physical address", "full name", "name",
            "date of birth", "dob", "ip", "ip address");
    private static final Set<String> LOW_DATA_TOKENS = Set.of(
            "email", "email address", "email addresses", "username", "usernames", "user name");

    public BreachSeverity classify(InsecureWebBreachFinding finding) {
        if (finding == null) {
            return BreachSeverity.LOW;
        }
        String normalizedCompromised = normalize(finding.getCompromisedData());
        Set<String> tokens = tokenize(normalizedCompromised);

        boolean hasPlaintextPassword = StringUtils.hasText(finding.getPassword())
                || hasAnyPhrase(normalizedCompromised, tokens, PLAINTEXT_PASSWORD_TOKENS);
        boolean hasHashedPassword = StringUtils.hasText(finding.getHashedPassword())
                || hasAnyPhrase(normalizedCompromised, tokens, HASHED_PASSWORD_TOKENS);
        boolean hasCriticalData = hasAnyPhrase(normalizedCompromised, tokens, CRITICAL_DATA_TOKENS);
        boolean hasMediumPii = StringUtils.hasText(finding.getPhone())
                || hasAnyPhrase(normalizedCompromised, tokens, MEDIUM_PII_TOKENS);
        boolean hasLowOnlyData = hasAnyPhrase(normalizedCompromised, tokens, LOW_DATA_TOKENS);

        // Highest-match wins.
        if (hasCriticalData || (hasPlaintextPassword && hasMediumPii)) {
            return BreachSeverity.CRITICAL;
        }
        if (hasPlaintextPassword || hasHashedPassword) {
            return BreachSeverity.HIGH;
        }
        if (hasMediumPii) {
            return BreachSeverity.MEDIUM;
        }
        if (hasLowOnlyData) {
            return BreachSeverity.LOW;
        }
        return BreachSeverity.LOW;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = CAMEL_CASE_SPLIT.matcher(value).replaceAll("$1 $2")
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("[^a-z0-9, ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        // basic plural normalization for common terms
        normalized = normalized.replace("email addresses", "email address")
                .replace("passwords", "password")
                .replace("usernames", "username")
                .replace("phone numbers", "phone number")
                .replace("full names", "full name")
                .replace("physical addresses", "physical address");
        return normalized;
    }

    private Set<String> tokenize(String normalized) {
        if (!StringUtils.hasText(normalized)) {
            return Set.of();
        }
        return Arrays.stream(normalized.split("[, ]+"))
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean hasAnyPhrase(String normalized, Set<String> tokens, Set<String> candidates) {
        if (!StringUtils.hasText(normalized)) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate.contains(" ")) {
                if (normalized.contains(candidate)) {
                    return true;
                }
                continue;
            }
            if (tokens.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
