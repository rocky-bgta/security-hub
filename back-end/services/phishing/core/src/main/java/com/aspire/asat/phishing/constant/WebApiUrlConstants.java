package com.aspire.asat.phishing.constant;

/**
 * Web API URL constants for the Phishing module.
 */
public class WebApiUrlConstants {
    
    // Base paths
    public static final String PHISHING_API = "/api/v1/phishing";
    public static final String PHISHING_HEALTH_PATH = "/health";
    
    // Domain Verification endpoints (Task-01)
    public static final String DOMAINS_PATH = PHISHING_API + "/domains";
    public static final String DOMAIN_GENERATE_VERIFICATION = "/generate-verification-email";
    public static final String DOMAIN_VERIFY = "/verify";
    public static final String DOMAIN_LOCK = "/{domainId}/lock";
    public static final String DOMAIN_UNLOCK = "/{domainId}/unlock";
    /** Admin-only: register or re-verify a domain as {@code VERIFIED} without email OTP. */
    public static final String DOMAIN_ADMIN_ADD = "/admin";
    
    // Email Template endpoints (Task-02, Task-03)
    public static final String EMAIL_TEMPLATES_PATH = PHISHING_API + "/email-templates";

    // Config list endpoints (Task: Email/Payload/Difficulty)
    public static final String EMAIL_TYPES_PATH = PHISHING_API + "/email-types";
    public static final String PAYLOAD_TYPES_PATH = PHISHING_API + "/payload-types";
    public static final String TONES_PATH = PHISHING_API + "/tones";
    public static final String ATTACKER_PERSONAS_PATH = PHISHING_API + "/attacker-personas";
    public static final String SOCIAL_ENGINEERING_STRATEGIES_PATH = PHISHING_API + "/social-engineering-strategies";
    public static final String CAMPAIGN_OBJECTIVES_PATH = PHISHING_API + "/campaign-objectives";
    /**
     * Configurable difficulties (Mongo catalog).
     * Distinct from {@link com.aspire.asat.phishing.dto.enums.DifficultyLevel} on email templates.
     */
    public static final String DIFFICULTIES_PATH = PHISHING_API + "/difficulties";
    /** Configurable brand names for phishing / AI content (Mongo catalog). */
    public static final String BRANDS_PATH = PHISHING_API + "/brands";
    /** Configurable call-to-action labels for phishing / AI content (Mongo catalog). */
    public static final String CALL_TO_ACTIONS_PATH = PHISHING_API + "/call-to-actions";
    /** Configurable data capture types for phishing / landing content (Mongo catalog). */
    public static final String DATA_CAPTURE_TYPES_PATH = PHISHING_API + "/data-capture-types";
    /**
     * Configurable personalization levels (Mongo catalog).
     * Source catalog for optional embedded snapshots on sender profiles.
     */
    public static final String PERSONALIZATION_LEVELS_PATH = PHISHING_API + "/personalization-levels";
    /**
     * Configurable urgency levels (Mongo catalog).
     * Distinct from free-text {@code urgencyLevel} on AI generation requests.
     */
    public static final String URGENCY_LEVELS_PATH = PHISHING_API + "/urgency-levels";
    /** Configurable emotional triggers (Mongo catalog). */
    public static final String EMOTIONAL_TRIGGERS_PATH = PHISHING_API + "/emotional-triggers";
    /** Configurable attack techniques (Mongo catalog). */
    public static final String ATTACK_TECHNIQUES_PATH = PHISHING_API + "/attack-techniques";
    /** Configurable expected user actions (Mongo catalog). */
    public static final String EXPECTED_USER_ACTIONS_PATH = PHISHING_API + "/expected-user-actions";
    /** Configurable constraints data entries (Mongo catalog). */
    public static final String CONSTRAINTS_DATA_PATH = PHISHING_API + "/constraints-data";
    /** Configurable trigger events (Mongo catalog). */
    public static final String TRIGGER_EVENTS_PATH = PHISHING_API + "/trigger-events";
    /**
     * Configurable deception levels (Mongo catalog).
     * Source catalog for optional embedded snapshots on sender profiles.
     */
    public static final String DECEPTION_LEVELS_PATH = PHISHING_API + "/deception-levels";
    
    // Landing Page endpoints (Task-04, Task-05)
    public static final String LANDING_PAGES_PATH = PHISHING_API + "/landing-pages";
    /** Configurable landing page categories (Mongo catalog; distinct from {@code LandingPageCategory} enum on entities). */
    public static final String LANDING_PAGE_CATEGORIES_PATH = PHISHING_API + "/landing-page-categories";
    
    // Sender Profile endpoints (Task-06)
    public static final String SENDER_PROFILES_PATH = PHISHING_API + "/sender-profiles";

    // SMS Server Configuration endpoints
    public static final String SMS_SERVER_CONFIGURATIONS_PATH = PHISHING_API + "/sms-server-configurations";

    // Voice Server Configuration endpoints
    public static final String VOICE_SERVER_CONFIGURATIONS_PATH = PHISHING_API + "/voice-server-configurations";

    // Vishing Scenario endpoints
    public static final String VISHING_SCENARIOS_PATH = PHISHING_API + "/vishing-scenarios";

    /** Platform-managed vishing attack template catalog (admin CUD; all clients can read). */
    public static final String VISHING_ATTACK_TEMPLATES_PATH = PHISHING_API + "/vishing-attack-templates";

    /** List of vishing campaign voice-setup clones (DeepfakeVoiceClone with VISHING source). */
    public static final String VISHING_VOICES_PATH = PHISHING_API + "/vishing-voices";
    
    // Campaign endpoints (Task-07)
    public static final String CAMPAIGNS_PATH = PHISHING_API + "/campaigns";
    
    // Breach Detection endpoints (Task-08)
    public static final String BREACHES_PATH = PHISHING_API + "/breaches";
    public static final String RECIPIENT_BREACHES_PATH = PHISHING_API + "/recipient-breaches";
    public static final String BREACH_DETECTION_PATH = PHISHING_API + "/breach-detection";
    
    // Dashboard & Analytics endpoints (Task-09)
    public static final String DASHBOARD_PATH = PHISHING_API + "/dashboard";
    public static final String REPORTS_PATH = PHISHING_API + "/reports";
    public static final String ANALYTICS_PATH = PHISHING_API + "/analytics";

    // Phishing Course Dashboard endpoints (statistics + paginated details)
    public static final String PHISHING_COURSE_PATH = PHISHING_API + "/phishing-course";

    // Global AI provider config (SSM + ai_provider_secret_refs)
    public static final String AI_CONFIG_PATH = PHISHING_API + "/ai-config";

    /** Per-client third-party provider credentials (deepfake voice cloning + video rendering). */
    public static final String PROVIDER_CREDENTIALS_PATH = PHISHING_API + "/provider-credentials";

    /** AI model catalog (per provider). */
    public static final String AI_MODELS_PATH = PHISHING_API + "/ai-models";

    // Training Risk Score endpoint
    public static final String TRAINING_RISK_SCORE_PATH = PHISHING_API + "/training-risk-score";

    // User Risk Profile endpoints
    public static final String USER_RISK_PROFILES_PATH = PHISHING_API + "/user-risk-profiles";

    // Internal service-to-service endpoints (X-Internal-Service-Key)
    public static final String INTERNAL_API_PATH = PHISHING_API + "/internal";
    public static final String INTERNAL_CAMPAIGN_LICENSE_USAGE_PATH = INTERNAL_API_PATH + "/campaign-license-usage";
    public static final String INTERNAL_LICENSED_USER_IDS_PATH = INTERNAL_API_PATH + "/licensed-user-ids";
    public static final String INTERNAL_USER_LICENCE_SNAPSHOT_PATH = INTERNAL_API_PATH + "/user-licence-snapshot";

    // Tracking endpoints (public, no auth required)
    public static final String TRACKING_BASE_PATH = "/t";
    public static final String TRACKING_OPEN = "/open/{trackingId}";
    public static final String TRACKING_CLICK = "/click/{trackingId}";
    public static final String TRACKING_PHISH = "/phish/{trackingId}";
    public static final String TRACKING_SHORT = "/s/{shortCode}";
    public static final String TRACKING_SUBMIT = "/submit/{trackingId}";
    public static final String TRACKING_REPORT = "/report/{trackingId}";
    public static final String TRACKING_REPORT_ADDIN = "/report/{trackingId}/addin";

    // Voice ingestion endpoints (webhook, API key auth)
    public static final String VOICE_BASE_PATH = "/v";
    public static final String VOICE_CALL_STATUS = "/calls/{trackingId}/status";
    public static final String VOICE_CALL_RESULT = "/calls/{trackingId}/result";

    // Twilio voice webhook endpoints (public, secured by X-Twilio-Signature + unguessable trackingId)
    public static final String VOICE_CALL_TWIML = "/calls/{trackingId}/twiml";
    public static final String VOICE_CALL_GATHER = "/calls/{trackingId}/gather";
    public static final String VOICE_CALL_TWILIO_STATUS = "/calls/{trackingId}/twilio-status";
    
    private WebApiUrlConstants() {
        // Private constructor to prevent instantiation
    }
}

