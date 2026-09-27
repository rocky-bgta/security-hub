package com.aspire.asat.phishing.ai.prompt;

import com.aspire.asat.phishing.ai.model.AiEmailRequest;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.AiGenerationOptions;
import com.aspire.asat.phishing.ai.model.AiLandingPageRequest;

import java.util.List;

/**
 * Shared prompt text for all AI provider adapters (OpenAI, Gemini, etc.).
 */
public final class AiProviderPrompts {

    public static final List<String> JSON_OUTPUT_HINTS = List.of(
            "Respond with valid JSON only. Do not wrap JSON in markdown."
    );

    private AiProviderPrompts() {
    }

    /**
     * Phishing email template generation — same instructions for every provider.
     */
    public static String emailGenerationPrompt(AiEmailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiEmailRequest is required");
        }
        AiGenerationOptions opts = request.getOptions();
        if (opts == null) {
            throw new IllegalArgumentException("options are required");
        }

        String tone = nullToEmpty(opts.getTone());
        String language = nullToEmpty(opts.getLanguage());
        String brand = nullToEmpty(opts.getBrand());
        String contentLength = nullToEmpty(opts.getContentLength());
        String callToAction = nullToEmpty(opts.getCallToAction());
        String difficultyFromOpts = nullToEmpty(opts.getDifficulty());
        String targetIndustry = nullToEmpty(opts.getTargetIndustry());
        String department = nullToEmpty(opts.getDepartment());
        if (opts.getDepartment() != null && !opts.getDepartment().isBlank()) {
            targetIndustry = "";
        }
        StringBuilder optionalParams = new StringBuilder();
        appendParameterIfPresent(optionalParams, "constraints", opts.getConstraints());
        appendParameterIfPresent(optionalParams, "targetIndustry", targetIndustry);
        appendParameterIfPresent(optionalParams, "department", department);
        appendParameterIfPresent(optionalParams, "attackTechnique", opts.getAttackTechnique());
        appendParameterIfPresent(optionalParams, "triggerEvent", opts.getTriggerEvent());
        appendParameterIfPresent(optionalParams, "expectedUserAction", opts.getExpectedUserAction());
        appendParameterIfPresent(optionalParams, "urgencyLevel", opts.getUrgencyLevel());
        appendParameterIfPresent(optionalParams, "emotionalTrigger", opts.getEmotionalTrigger());

        String basePrompt = """
                You are generating a phishing email template for security awareness training.

                Create output as VALID JSON only (no markdown), with this schema:
                {
                  "htmlBody": "<FULL_HTML_WITH_PLACEHOLDERS>",
                  "textBody": "<PLAIN_TEXT_WITH_PLACEHOLDERS>",
                  "emailSubject": "<PLAIN_TEXT_WITH_PLACEHOLDERS>",
                  "suggestedDifficulty": "BEGINNER|INTERMEDIATE|ADVANCED|SPEAR_PHISHING"
                }

                Requirements:
                - Use placeholders exactly: {{FIRST_NAME}}, {{LAST_NAME}},{{FULL_NAME}}, {{PHISHING_LINK}}, {{EMAIL_ADDRESS}}, {{DEPARTMENT}}, {{organizationName}}, {{organizationDomain}}
                - The HTML must be safe and should not include scripts.
                - Include a single main CTA link using {{PHISHING_LINK}}.
                - Use a realistic subject line content inside the email (NOT outside JSON).
                - Generate a responsive HTML email layout using inline CSS and table-based structure.
                - Use {{organizationName}} and/or {{organizationDomain}} in footer/signature branding lines.

                Parameters:
                - templateName: %s
                - payloadType: %s
                - tone: %s
                - language: %s
                - inputLanguage: %s
                - difficultyLevel: %s
                - difficulty: %s
                - brand: %s
                - length: %s
                - callToAction: %s
                %s
                - emailSubject: %s
                - description: %s
                - voiceInputContent: %s
                - attackerPersona: %s
                - socialEngineeringStrategy: %s
                - campaignObjective: %s
                - additionalContext: %s
                - tags: %s
                - employeeDataRequired: %s
                """.formatted(
                nullToEmpty(request.getTemplateName()),
                nullToEmpty(request.getPayloadType()),
                tone,
                language,
                nullToEmpty(opts.getInputLanguage()),
                nullToEmpty(opts.getDifficultyLevel()),
                difficultyFromOpts,
                brand,
                contentLength,
                callToAction,
                optionalParams.toString(),
                nullToEmpty(opts.getEmailSubject()),
                nullToEmpty(opts.getDescription()),
                nullToEmpty(opts.getVoiceInputContent()),
                nullToEmpty(opts.getAttackerPersona()),
                nullToEmpty(opts.getSocialEngineeringStrategy()),
                nullToEmpty(opts.getCampaignObjective()),
                nullToEmpty(opts.getAdditionalContext()),
                opts.getTags() == null ? "" : String.join(", ", opts.getTags()),
                opts.getEmployeeDataRequired() == null ? "" : String.join(", ", opts.getEmployeeDataRequired())
        );

        return new StringBuilder(basePrompt)
                .append("\n")
                .append(String.join("\n", JSON_OUTPUT_HINTS))
                .toString();
    }

    /**
     * Phishing landing page generation — same instructions for every provider.
     */
    public static String landingPageGenerationPrompt(AiLandingPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiLandingPageRequest is required");
        }
        AiGenerationOptions opts = request.getOptions();
        if (opts == null) {
            throw new IllegalArgumentException("options are required");
        }

        String tone = nullToEmpty(opts.getTone());
        String language = nullToEmpty(opts.getLanguage());
        String layoutStyle = nullToEmpty(opts.getLayoutStyle());
        String generationMode = nullToEmpty(opts.getGenerationMode());
        String cta = nullToEmpty(opts.getCallToAction());
        StringBuilder optionalParams = new StringBuilder();
        appendParameterIfPresent(optionalParams, "constraints", opts.getConstraints());
        appendParameterIfPresent(optionalParams, "department", opts.getDepartment());
        appendParameterIfPresent(optionalParams, "targetIndustry", opts.getTargetIndustry());
        appendParameterIfPresent(optionalParams, "dataCaptureType", opts.getDataCaptureType());

        return """
                You are generating a phishing landing page for security awareness training.

                Create output as VALID JSON only (no markdown), with this schema:
                {
                  "htmlContent": "<FULL_HTML_PAGE>",
                  "title": "<PAGE_TITLE>",
                  "suggestedDifficulty": "BEGINNER|INTERMEDIATE|ADVANCED|SPEAR_PHISHING"
                }

                Requirements:
                - Return a full HTML document with inline CSS (no external scripts).
                - Use a submit CTA button labeled with callToAction (%s) if provided; otherwise a reasonable default.
                - When dataCaptureType is provided, design the page form fields to match that capture type (e.g. credentials, payment details, personal info).

                Parameters:
                - pageName: %s
                - pageType: %s
                - category: %s
                - difficultyLevel: %s
                - tags: %s
                - targetDepartment: %s
                - urgencyLevel: %s
                - emotionalTrigger: %s
                - layoutStyle: %s
                - generationMode: %s
                - tone: %s
                - language: %s
                - description: %s
                - voiceInput: %s
                - additionalContext: %s
                %s

                """.formatted(
                cta,
                nullToEmpty(request.getPageName()),
                nullToEmpty(request.getPageType()),
                nullToEmpty(request.getCategory()),
                nullToEmpty(opts.getDifficultyLevel()),
                opts.getTags() == null ? "" : String.join(", ", opts.getTags()),
                nullToEmpty(request.getTargetDepartment()),
                nullToEmpty(request.getUrgencyLevel()),
                nullToEmpty(request.getEmotionalTrigger()),
                layoutStyle,
                generationMode,
                tone,
                language,
                nullToEmpty(opts.getDescription()),
                nullToEmpty(opts.getVoiceInput()),
                nullToEmpty(opts.getAdditionalContext()),
                optionalParams.toString()
        ) + "\n" + String.join("\n", JSON_OUTPUT_HINTS);
    }

    /**
     * HTML page fix (element replacement) — same instructions for every provider.
     */
    public static String fixHtmlPagePrompt(AiFixHtmlPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiFixHtmlPageRequest is required");
        }
        if (request.getInput() == null) {
            throw new IllegalArgumentException("input is required");
        }
        var input = request.getInput();

        return """
                You are an expert frontend engineer.
                Fix only the requested part of the HTML page and return the complete updated HTML page.

                CRITICAL OUTPUT FORMAT:
                - Always return a COMPLETE HTML document.
                - The response MUST start with <!DOCTYPE html> and end with </html>.
                - NEVER return partial code.
                - NEVER return only elementHtml.
                - EVEN IF only a small part is changed, return the FULL updated templateCode with the fix applied.

                Unchanged Sections:s
                - Do not modify any part of the HTML that hasn’t been specified for change. If only a section of the page is updated, return the entire page code without altering the rest of the content.
                -  Even if only a small part is modified, return the full updated page code with the fix applied, including elements that were not changed.
                Parameters:
                - prompt: %s
                - elementHtml: %s

                - templateCode: %s
                """.formatted(
                nullToEmpty(input.getPrompt()),
                nullToEmpty(input.getElementHtml()),
                nullToEmpty(input.getTemplateCode())
        );
    }

    /**
     * HTML email template fix (element replacement) — same instructions for every provider.
     */
    public static String fixEmailTemplatePrompt(AiFixHtmlPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiFixHtmlPageRequest is required");
        }
        if (request.getInput() == null) {
            throw new IllegalArgumentException("input is required");
        }
        var input = request.getInput();
        return """
                You are an expert in creating HTML email template.
                Fix only the requested part of the HTML email template and return the complete updated HTML email template.

                Rules:
                - Return HTML only (no markdown, no explanations).
                - Apply changes requested in prompt while preserving existing email template structure and styles.
                - Focus on fixing/replacing the elementHtml section in templateCode.
                - Keep output valid HTML email template markup.
                - Preserve inline CSS and table-based layout patterns when present.
                - Do not add scripts.
                - Add organization name or domain at the bottom of the email template if provided in the prompt and having not blank value.

                Parameters:
                - prompt: %s
                - elementHtml:
                %s

                - templateCode:
                %s
                """.formatted(
                nullToEmpty(input.getPrompt()),
                nullToEmpty(input.getElementHtml()),
                nullToEmpty(input.getTemplateCode())
        );
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private static void appendParameterIfPresent(StringBuilder builder, String key, String value) {
        if (value != null && !value.isBlank()) {
            builder.append("- ").append(key).append(": ").append(value).append("\n");
        }
    }

}
