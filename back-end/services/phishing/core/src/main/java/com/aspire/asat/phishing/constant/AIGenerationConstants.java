package com.aspire.asat.phishing.constant;

import java.util.List;

/**
 * Constants for AI-powered email template generation.
 * Based on BRD Use Case 2.1.3.2: AI Template Generation Parameters
 */
public final class AIGenerationConstants {
    
    private AIGenerationConstants() {
        // Prevent instantiation
    }
    
    /**
     * Supported target industries for phishing simulations
     */
    public static final List<String> TARGET_INDUSTRIES = List.of(
            "Banking",
            "Healthcare",
            "Technology",
            "Retail",
            "Email Service Providers",
            "Government",
            "Education",
            "Insurance",
            "Telecommunications",
            "Manufacturing",
            "Legal",
            "Real Estate"
    );
    
    /**
     * Attacker personas (15+ from BRD)
     * Used to simulate different types of phishing attackers
     */
    public static final List<String> ATTACKER_PERSONAS = List.of(
            "Bank Representative",
            "Tech Support",
            "Government Agency",
            "HR Department",
            "IT Security Team",
            "CEO/Executive",
            "Delivery Service",
            "Tax Authority",
            "Insurance Company",
            "Cloud Service Provider",
            "Lottery/Prize",
            "Family Emergency",
            "Vendor/Supplier",
            "Social Media Platform",
            "Healthcare Provider",
            "Telecom Provider"
    );
    
    /**
     * Social engineering strategies (15+ from BRD)
     * Psychological tactics used in phishing attacks
     */
    public static final List<String> SOCIAL_ENGINEERING_STRATEGIES = List.of(
            "Urgency",
            "Authority",
            "Intimidation",
            "Helpfulness",
            "Curiosity",
            "Fear",
            "Greed",
            "Scarcity",
            "Social Proof",
            "Reciprocity",
            "Commitment",
            "Liking",
            "Trust",
            "Familiarity",
            "Compliance"
    );
    
    /**
     * Campaign objectives for phishing simulations
     */
    public static final List<String> CAMPAIGN_OBJECTIVES = List.of(
            "Credential Harvesting",
            "Malware Distribution",
            "Financial Fraud",
            "Data Exfiltration",
            "Reconnaissance",
            "Account Takeover",
            "Business Email Compromise",
            "Ransomware Delivery"
    );
    
    /**
     * AI generation prompt templates
     */
    public static final String AI_SYSTEM_PROMPT = """
            You are an expert in creating phishing email templates for security awareness training.
            Your task is to generate realistic but safe phishing email content that helps organizations
            train their employees to recognize and avoid phishing attacks.
            
            The generated content should:
            1. Be realistic enough to serve as effective training material
            2. Not contain actual malicious links or payloads
            3. Include common phishing indicators for training purposes
            4. Use the specified attacker persona and social engineering strategy
            5. Target the specified industry appropriately
            """;
    
    public static final String AI_GENERATION_TEMPLATE = """
            Generate a phishing email template with the following parameters:
            
            Target Industry: %s
            Attacker Persona: %s
            Social Engineering Strategy: %s
            Campaign Objective: %s
            Subject Line: %s
            
            Additional context from user:
            %s
            
            Please generate:
            1. HTML email body with professional styling
            2. Plain text version of the email
            3. Suggested difficulty level (BEGINNER, INTERMEDIATE, ADVANCED, SPEAR_PHISHING) based on sophistication
            
            Format your response as JSON with fields: htmlBody, textBody, suggestedDifficulty
            """;
}
