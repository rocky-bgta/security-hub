package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.ValidationResult;
import com.aspire.asat.phishing.service.HtmlValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of HtmlValidatorService.
 * Validates HTML syntax, structure, and security.
 * Based on Task-05 Landing Page Creation
 */
@Service
@Slf4j
public class HtmlValidatorServiceImpl implements HtmlValidatorService {
    
    // Patterns for HTML validation
    private static final Pattern FORM_TAG_PATTERN = Pattern.compile("<form[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern INPUT_TAG_PATTERN = Pattern.compile(
            "<input[^>]*name=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_FIELD_PATTERN = Pattern.compile(
            "<input[^>]*type=[\"']password[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
            "\\s+on\\w+\\s*=\\s*[\"'][^\"']*[\"']", Pattern.CASE_INSENSITIVE);
    
    // Login form indicators
    private static final Set<String> LOGIN_FORM_INDICATORS = Set.of(
            "password", "passwd", "pwd", "login", "signin", "sign-in",
            "username", "user", "email", "userid", "user_id"
    );
    
    @Override
    public ValidationResult validate(String htmlContent) {
        return validateWithOptions(htmlContent, false, false);
    }
    
    @Override
    public ValidationResult validateWithOptions(String htmlContent, boolean validateForms, boolean securityCheck) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> formFields = new ArrayList<>();
        
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            errors.add("HTML content is empty");
            return ValidationResult.failure(errors);
        }
        
        // Basic syntax validation
        validateBasicSyntax(htmlContent, errors, warnings);
        
        // Form validation
        if (validateForms) {
            formFields = extractFormFields(htmlContent);
            validateFormStructure(htmlContent, errors, warnings);
        }
        
        // Security check
        if (securityCheck) {
            validateSecurity(htmlContent, errors, warnings);
        }
        
        boolean hasLoginForm = hasLoginForm(htmlContent);
        
        return ValidationResult.builder()
                .isValid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .detectedFormFields(formFields)
                .hasLoginForm(hasLoginForm)
                .build();
    }
    
    @Override
    public List<String> extractFormFields(String htmlContent) {
        Set<String> fields = new HashSet<>();
        
        if (htmlContent == null) {
            return new ArrayList<>();
        }
        
        Matcher matcher = INPUT_TAG_PATTERN.matcher(htmlContent);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (fieldName != null && !fieldName.isEmpty()) {
                fields.add(fieldName);
            }
        }
        
        // Also check for select and textarea
        Pattern selectPattern = Pattern.compile(
                "<select[^>]*name=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher selectMatcher = selectPattern.matcher(htmlContent);
        while (selectMatcher.find()) {
            String fieldName = selectMatcher.group(1);
            if (fieldName != null && !fieldName.isEmpty()) {
                fields.add(fieldName);
            }
        }
        
        Pattern textareaPattern = Pattern.compile(
                "<textarea[^>]*name=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher textareaMatcher = textareaPattern.matcher(htmlContent);
        while (textareaMatcher.find()) {
            String fieldName = textareaMatcher.group(1);
            if (fieldName != null && !fieldName.isEmpty()) {
                fields.add(fieldName);
            }
        }
        
        return new ArrayList<>(fields);
    }
    
    @Override
    public boolean hasLoginForm(String htmlContent) {
        if (htmlContent == null) {
            return false;
        }
        
        String lowerContent = htmlContent.toLowerCase();
        
        // Check for password field
        if (!PASSWORD_FIELD_PATTERN.matcher(htmlContent).find()) {
            return false;
        }
        
        // Check for login form indicators
        for (String indicator : LOGIN_FORM_INDICATORS) {
            if (lowerContent.contains(indicator)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validate basic HTML syntax
     */
    private void validateBasicSyntax(String htmlContent, List<String> errors, List<String> warnings) {
        // Check for unclosed tags (simplified check)
        int openTags = countOccurrences(htmlContent, "<");
        int closeTags = countOccurrences(htmlContent, ">");
        
        if (openTags != closeTags) {
            warnings.add("Possible unclosed HTML tags detected");
        }
        
        // Check for DOCTYPE
        if (!htmlContent.toLowerCase().contains("<!doctype")) {
            warnings.add("Missing DOCTYPE declaration");
        }
        
        // Check for HTML, HEAD, BODY structure
        if (!htmlContent.toLowerCase().contains("<html")) {
            warnings.add("Missing <html> tag");
        }
        
        if (!htmlContent.toLowerCase().contains("<head")) {
            warnings.add("Missing <head> tag");
        }
        
        if (!htmlContent.toLowerCase().contains("<body")) {
            warnings.add("Missing <body> tag");
        }
        
        // Check for title
        if (!htmlContent.toLowerCase().contains("<title")) {
            warnings.add("Missing <title> tag");
        }
    }
    
    /**
     * Validate form structure
     */
    private void validateFormStructure(String htmlContent, List<String> errors, List<String> warnings) {
        Matcher formMatcher = FORM_TAG_PATTERN.matcher(htmlContent);
        
        if (!formMatcher.find()) {
            warnings.add("No form elements detected in HTML");
            return;
        }
        
        // Check for action attribute
        if (!htmlContent.toLowerCase().contains("action=")) {
            warnings.add("Form is missing action attribute");
        }
        
        // Check for method attribute
        if (!htmlContent.toLowerCase().contains("method=")) {
            warnings.add("Form is missing method attribute");
        }
        
        // Check for submit button
        if (!htmlContent.toLowerCase().contains("type=\"submit\"") &&
            !htmlContent.toLowerCase().contains("type='submit'")) {
            warnings.add("Form is missing submit button");
        }
    }
    
    /**
     * Validate security aspects
     */
    private void validateSecurity(String htmlContent, List<String> errors, List<String> warnings) {
        // Check for inline scripts
        if (SCRIPT_TAG_PATTERN.matcher(htmlContent).find()) {
            warnings.add("Inline scripts detected - will be removed during sanitization");
        }
        
        // Check for event handlers
        if (EVENT_HANDLER_PATTERN.matcher(htmlContent).find()) {
            warnings.add("Event handlers detected - will be removed during sanitization");
        }
        
        // Check for external resources
        if (htmlContent.contains("src=\"http://") || htmlContent.contains("src='http://")) {
            warnings.add("Non-HTTPS resources detected - consider using HTTPS");
        }
        
        // Check for iframe
        if (htmlContent.toLowerCase().contains("<iframe")) {
            warnings.add("Iframe elements detected - may cause display issues");
        }
    }
    
    /**
     * Count occurrences of a substring
     */
    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
