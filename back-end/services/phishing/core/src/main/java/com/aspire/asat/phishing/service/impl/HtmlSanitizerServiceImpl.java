package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.SanitizeHtmlResult;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of HtmlSanitizerService.
 * Removes dangerous HTML elements and attributes for safe email template storage.
 * Based on BR-04, BR-10 from Task-03 requirements.
 */
@Service
@Slf4j
public class HtmlSanitizerServiceImpl implements HtmlSanitizerService {
    
    /**
     * Elements to completely remove (with content)
     */
//    private static final List<String> DANGEROUS_ELEMENTS = List.of(
//            "script", "iframe", "object", "embed", "form", "input",
//            "meta", "link", "base", "applet", "frame", "frameset"
//    );

// 20260411
//    private static final List<String> DANGEROUS_ELEMENTS = List.of(
//            "script", "iframe", "embed", "base", "applet", "frame", "frameset"
//    );

    private static final List<String> DANGEROUS_ELEMENTS = List.of();
    
    /**
     * Attributes to remove (event handlers and dangerous protocols)
     */
    private static final List<String> DANGEROUS_ATTRIBUTES = List.of(
            "onclick", "ondblclick", "onmousedown", "onmouseup", "onmouseover",
            "onmousemove", "onmouseout", "onmouseenter", "onmouseleave",
            "onkeydown", "onkeyup", "onkeypress",
            "onfocus", "onblur", "onchange", "onsubmit", "onreset",
            "onload", "onunload", "onerror", "onabort",
            "oncontextmenu", "ondrag", "ondrop",
            "formaction", "xlink:href", "data-bind"
    );
    
    /**
     * Pattern for dangerous protocols in href/src
     */
    private static final Pattern DANGEROUS_PROTOCOL_PATTERN = 
            Pattern.compile("(href|src|action)\\s*=\\s*['\"]?\\s*(javascript|data|vbscript):", 
                    Pattern.CASE_INSENSITIVE);
    
    @Override
    public String sanitize(String htmlContent) {
        if (!StringUtils.hasText(htmlContent)) {
            return "";
        }
        
        String sanitized = htmlContent;
        
        // Remove dangerous elements with their content
        for (String element : DANGEROUS_ELEMENTS) {
            Pattern pattern = Pattern.compile(
                    "<" + element + "[^>]*>.*?</" + element + ">|<" + element + "[^>]*/?>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }
        
        // Remove dangerous attributes
        for (String attr : DANGEROUS_ATTRIBUTES) {
            Pattern pattern = Pattern.compile(
                    "\\s+" + attr + "\\s*=\\s*(['\"])[^'\"]*\\1|\\s+" + attr + "\\s*=\\s*[^\\s>]+",
                    Pattern.CASE_INSENSITIVE);
            sanitized = pattern.matcher(sanitized).replaceAll("");
        }
        
        // Remove dangerous protocols
        sanitized = DANGEROUS_PROTOCOL_PATTERN.matcher(sanitized).replaceAll("$1=\"#\"");
        
        log.debug("Sanitized HTML content, length: {} -> {}", 
                htmlContent.length(), sanitized.length());
        
        return sanitized.trim();
    }
    
    @Override
    public SanitizeHtmlResult sanitizeWithDetails(String htmlContent) {
        if (!StringUtils.hasText(htmlContent)) {
            return SanitizeHtmlResult.builder()
                    .sanitizedHtml("")
                    .wasModified(false)
                    .removedElements(List.of())
                    .removedAttributes(List.of())
                    .isSafe(true)
                    .build();
        }
        
        List<String> removedElements = new ArrayList<>();
        List<String> removedAttributes = new ArrayList<>();
        String sanitized = htmlContent;
        
        // Find and remove dangerous elements
        for (String element : DANGEROUS_ELEMENTS) {
            Pattern pattern = Pattern.compile(
                    "<" + element + "[^>]*>.*?</" + element + ">|<" + element + "[^>]*/?>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(sanitized);
            if (matcher.find()) {
                removedElements.add(element);
                sanitized = matcher.replaceAll("");
            }
        }
        
        // Find and remove dangerous attributes
        for (String attr : DANGEROUS_ATTRIBUTES) {
            Pattern pattern = Pattern.compile(
                    "\\s+" + attr + "\\s*=\\s*(['\"])[^'\"]*\\1|\\s+" + attr + "\\s*=\\s*[^\\s>]+",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sanitized);
            if (matcher.find()) {
                removedAttributes.add(attr);
                sanitized = matcher.replaceAll("");
            }
        }
        
        // Check for dangerous protocols
        Matcher protocolMatcher = DANGEROUS_PROTOCOL_PATTERN.matcher(sanitized);
        if (protocolMatcher.find()) {
            removedAttributes.add("javascript: protocol");
            sanitized = protocolMatcher.replaceAll("$1=\"#\"");
        }
        
        boolean wasModified = !sanitized.equals(htmlContent);
        String warningMessage = wasModified 
                ? "HTML content contained potentially unsafe elements that were removed" 
                : null;
        
        return SanitizeHtmlResult.builder()
                .sanitizedHtml(sanitized.trim())
                .wasModified(wasModified)
                .removedElements(removedElements)
                .removedAttributes(removedAttributes)
                .isSafe(true)
                .warningMessage(warningMessage)
                .build();
    }
    
    @Override
    public boolean isValidHtml(String htmlContent) {
        if (!StringUtils.hasText(htmlContent)) {
            return true;
        }
        
        // Basic HTML validation
        // Check for balanced tags (simplified)
        try {
            // Count opening and closing tags for common elements
            int openingTags = countPattern(htmlContent, "<[a-z][a-z0-9]*[^>]*>");
            int closingTags = countPattern(htmlContent, "</[a-z][a-z0-9]*>");
            int selfClosingTags = countPattern(htmlContent, "<[a-z][a-z0-9]*[^>]*/>");
            
            // Not a perfect validation, but catches major issues
            // Allow self-closing tags and common void elements
            return Math.abs(openingTags - closingTags - selfClosingTags) <= 5;
        } catch (Exception e) {
            log.warn("HTML validation error: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean containsDangerousElements(String htmlContent) {
        if (!StringUtils.hasText(htmlContent)) {
            return false;
        }
        
        String lowerContent = htmlContent.toLowerCase();
        
        // Check for dangerous elements
        for (String element : DANGEROUS_ELEMENTS) {
            if (lowerContent.contains("<" + element)) {
                return true;
            }
        }
        
        // Check for dangerous attributes
        for (String attr : DANGEROUS_ATTRIBUTES) {
            if (lowerContent.contains(attr + "=")) {
                return true;
            }
        }
        
        // Check for dangerous protocols
        return DANGEROUS_PROTOCOL_PATTERN.matcher(htmlContent).find();
    }
    
    private int countPattern(String content, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
