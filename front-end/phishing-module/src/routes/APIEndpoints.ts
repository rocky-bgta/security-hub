/**
 * API endpoint constants for the Phishing module
 */
export const API_END_POINTS = {
  // End User Management (from Registration module)
  END_USER_LIST: '/registration/api/v1/end-user?',
  END_USER_DETAILS: (id: string) => `/registration/api/v1/end-user/${id}`,
  DEPARTMENT_LIST: '/registration/api/v1/departments?',
  DEPARTMENT_USER_COUNTS: '/registration/api/v1/departments/user-counts?',
  RISK_GROUP_USER_COUNTS:
    '/registration/api/v1/departments/risk-group-user-counts?',

  // Domain Management (Task-01)
  DOMAIN_LIST: '/phishing/api/v1/phishing/domains?',
  DOMAIN_DETAILS: (id: string) => `/phishing/api/v1/phishing/domains/${id}`,
  DOMAIN_GENERATE_CODE:
    '/phishing/api/v1/phishing/domains/generate-verification-email',
  DOMAIN_ADD: '/phishing/api/v1/phishing/domains/admin',
  DOMAIN_VERIFY: '/phishing/api/v1/phishing/domains/verify',
  DOMAIN_LOCK: (id: string) => `/phishing/api/v1/phishing/domains/${id}/lock`,
  DOMAIN_UNLOCK: (id: string) =>
    `/phishing/api/v1/phishing/domains/${id}/unlock`,
  DOMAIN_DELETE: (id: string) => `/phishing/api/v1/phishing/domains/${id}`,
  DOMAIN_RESEND_CODE:
    '/phishing/api/v1/phishing/domains/resend-verification-email',

  // Email Template Management (Task-02, Task-03)
  EMAIL_TEMPLATE_LIST: '/phishing/api/v1/phishing/email-templates?',
  EMAIL_TEMPLATE_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/email-templates/${id}`,
  EMAIL_TEMPLATE_PREVIEW: (id: string) =>
    `/phishing/api/v1/phishing/email-templates/${id}/preview`,
  EMAIL_TEMPLATE_CREATE: '/phishing/api/v1/phishing/email-templates',
  EMAIL_TEMPLATE_UPDATE: (id: string) =>
    `/phishing/api/v1/phishing/email-templates/${id}`,
  EMAIL_TEMPLATE_DELETE: (id: string) =>
    `/phishing/api/v1/phishing/email-templates/${id}`,
  EMAIL_TEMPLATE_DUPLICATE: (id: string) =>
    `/phishing/api/v1/phishing/email-templates/${id}/duplicate`,
  EMAIL_TEMPLATE_TAGS: '/phishing/api/v1/phishing/email-templates/tags',
  EMAIL_TEMPLATE_FILTERS: '/phishing/api/v1/phishing/email-templates/filters',
  EMAIL_TEMPLATE_AI_GENERATE:
    '/phishing/api/v1/phishing/email-templates/ai-generate',
  STT_TRANSCRIBE: '/phishing/api/v1/stt/transcribe',
  STT_TRANSCRIBE_STATUS: (audioId: string) => `/phishing/api/v1/stt/${audioId}`,
  EMAIL_TEMPLATE_SANITIZE_HTML:
    '/phishing/api/v1/phishing/email-templates/sanitize-html',
  EMAIL_TEMPLATE_AI_OPTIONS:
    '/phishing/api/v1/phishing/email-templates/ai-options',
  EMAIL_TEMPLATE_FIX_HTML:
    '/phishing/api/v1/phishing/email-templates/fix/email-template',
  EMAIL_TEMPLATE_LANGUAGES:
    '/phishing/api/v1/phishing/email-templates/languages',
  EMAIL_TEMPLATE_UPLOAD_ATTACHMENT: (id: string) =>
    `/phishing/api/v1/phishing/email-templates/${id}/attachments`,
  EMAIL_TEMPLATE_DELETE_ATTACHMENT: (
    templateId: string,
    attachmentId: string,
  ) =>
    `/phishing/api/v1/phishing/email-templates/${templateId}/attachments/${attachmentId}`,

  /** AI API provider credentials (Gemini, OpenAI, etc.) */
  AI_PROVIDER_CONFIG: '/phishing/api/v1/phishing/ai-config',
  AI_MODELS: '/phishing/api/v1/phishing/ai-models',
  AI_MODEL_DETAILS: (id: string) => `/phishing/api/v1/phishing/ai-models/${id}`,

  /** Deepfake third-party provider credentials (ElevenLabs, Fish Audio, HeyGen) */
  PROVIDER_CREDENTIALS_LIST: '/phishing/api/v1/phishing/provider-credentials?',
  PROVIDER_CREDENTIALS: '/phishing/api/v1/phishing/provider-credentials',
  PROVIDER_CREDENTIAL_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/provider-credentials/${id}`,
  PROVIDER_CREDENTIAL_STATUS: (id: string) =>
    `/phishing/api/v1/phishing/provider-credentials/${id}/status`,
  PROVIDER_CREDENTIAL_DEFAULT: (id: string) =>
    `/phishing/api/v1/phishing/provider-credentials/${id}/default`,

  // Landing Page Management (Task-04, Task-05)
  LANDING_PAGE_LIST: '/phishing/api/v1/phishing/landing-pages?',
  LANDING_PAGE_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/landing-pages/${id}`,
  LANDING_PAGE_PREVIEW: (id: string) =>
    `/phishing/api/v1/phishing/landing-pages/${id}/preview`,
  LANDING_PAGE_CREATE: '/phishing/api/v1/phishing/landing-pages',
  LANDING_PAGE_UPDATE: (id: string) =>
    `/phishing/api/v1/phishing/landing-pages/${id}`,
  LANDING_PAGE_DELETE: (id: string) =>
    `/phishing/api/v1/phishing/landing-pages/${id}`,
  LANDING_PAGE_DUPLICATE: (id: string) =>
    `/phishing/api/v1/phishing/landing-pages/${id}/duplicate`,
  LANDING_PAGE_CATEGORIES: '/phishing/api/v1/phishing/landing-pages/categories',
  LANDING_PAGE_IMPORT: '/phishing/api/v1/phishing/landing-pages/import',
  LANDING_PAGE_AI_GENERATE:
    '/phishing/api/v1/phishing/landing-pages/ai-generate',
  LANDING_PAGE_VALIDATE_HTML:
    '/phishing/api/v1/phishing/landing-pages/validate-html',
  LANDING_PAGE_FIX_HTML:
    '/phishing/api/v1/phishing/landing-pages/fix/html-page',
  LANDING_PAGE_UPLOAD_THUMBNAIL: (id: string) =>
    `/phishing/api/v1/phishing/landing-pages/${id}/thumbnail`,
  LANDING_PAGES_BY_TEMPLATE: (templateId: string) =>
    `/phishing/api/v1/phishing/landing-pages/${templateId}/landing-pages`,

  // Sender Profile Management (Task-06)
  SENDER_PROFILE_LIST: '/phishing/api/v1/phishing/sender-profiles?',
  SENDER_PROFILE_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/sender-profiles/${id}`,
  SENDER_PROFILE_CREATE: '/phishing/api/v1/phishing/sender-profiles',
  SENDER_PROFILE_UPDATE: (id: string) =>
    `/phishing/api/v1/phishing/sender-profiles/${id}`,
  SENDER_PROFILE_DELETE: (id: string) =>
    `/phishing/api/v1/phishing/sender-profiles/${id}`,
  SENDER_PROFILE_DUPLICATE: (id: string) =>
    `/phishing/api/v1/phishing/sender-profiles/${id}/duplicate`,
  SENDER_PROFILE_TEST: (id: string) =>
    `/phishing/api/v1/phishing/sender-profiles/${id}/test`,
  SENDER_PROFILE_TEST_NEW: '/phishing/api/v1/phishing/sender-profiles/test',
  SENDER_PROFILE_VERIFIED: '/phishing/api/v1/phishing/sender-profiles/verified',
  SENDER_PROFILE_CHECK_DOMAIN:
    '/phishing/api/v1/phishing/sender-profiles/check-domain',
  SENDER_PROFILE_IMPORT: '/phishing/api/v1/phishing/sender-profiles/import',

  // SMS Server Configuration Management
  SMS_SERVER_CONFIGURATION_LIST:
    '/phishing/api/v1/phishing/sms-server-configurations?',
  SMS_SERVER_CONFIGURATION_CREATE:
    '/phishing/api/v1/phishing/sms-server-configurations',
  SMS_SERVER_CONFIGURATION_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/sms-server-configurations/${id}`,
  SMS_SERVER_CONFIGURATION_UPDATE: (id: string) =>
    `/phishing/api/v1/phishing/sms-server-configurations/${id}`,
  SMS_SERVER_CONFIGURATION_DELETE: (id: string) =>
    `/phishing/api/v1/phishing/sms-server-configurations/${id}`,

  GET_ACTIVE_INDUSTRIES_LIST: '/registration/api/v1/dropdown/industries/active',
  GET_ACTIVE_COUNTRY_LIST: '/registration/api/v1/dropdown/countries/active',

  // Campaign Management (Task-07)
  CAMPAIGN_LIST: '/phishing/api/v1/phishing/campaigns?',
  CAMPAIGN_DETAILS: (id: string) => `/phishing/api/v1/phishing/campaigns/${id}`,
  CAMPAIGN_CREATE: '/phishing/api/v1/phishing/campaigns',
  CAMPAIGN_UPDATE: (id: string) => `/phishing/api/v1/phishing/campaigns/${id}`,
  CAMPAIGN_UPDATE_STEP: (id: string, step: number) =>
    `/phishing/api/v1/phishing/campaigns/${id}/step/${step}`,
  CAMPAIGN_UPDATE_STEP_SMS_SERVER: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/step/4/sms-server`,
  CAMPAIGN_DELETE: (id: string) => `/phishing/api/v1/phishing/campaigns/${id}`,
  CAMPAIGN_LAUNCH: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/launch`,
  CAMPAIGN_PAUSE: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/pause`,
  CAMPAIGN_RESUME: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/resume`,
  CAMPAIGN_CANCEL: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/cancel`,
  CAMPAIGN_RECIPIENTS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/recipients`,
  CAMPAIGN_STATS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/stats`,
  CAMPAIGN_STATS_REFRESH: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/stats/refresh`,
  CAMPAIGN_PREVIEW_EMAIL: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/preview-email`,
  CAMPAIGN_RECOMMENDED_TOPICS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/recommended-topics?`,
  CAMPAIGN_IS_FIRST: '/phishing/api/v1/phishing/campaigns/is-first?',
  CAMPAIGN_ALLOCATE_LICENCE: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/allocate-licence`,
  CAMPAIGN_LICENSED_USERS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/licensed-users?`,
  CAMPAIGN_LICENSED_USER_GROUP_COUNTS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/licensed-users/group-counts`,
  CAMPAIGN_LICENSED_USER_DEPARTMENT_COUNTS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/licensed-users/department-counts`,

  // Vishing Simulation
  VISHING_CAMPAIGN_STEP_VOICE_SETUP: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/step/2/voice-setup`,
  VISHING_CAMPAIGN_STEP_SCENARIO: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/step/3/scenario`,
  VISHING_CAMPAIGN_STEP_TELEPHONY: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/step/4/telephony`,
  VISHING_DATA_CAPTURE: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/data-capture`,
  VISHING_SUCCESS_KEYWORDS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/success-keywords`,
  VISHING_REMEDIATION: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/remediation`,
  VISHING_TEACHABLE_MOMENT: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/teachable-moment`,
  VISHING_REPORT: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/report`,
  VISHING_REPORT_EXPORT: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/report/export`,
  VISHING_LIVE_METRICS: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/live-metrics`,
  VISHING_TEST_CALL: (id: string) =>
    `/phishing/api/v1/phishing/campaigns/${id}/vishing/test-call`,
  VISHING_SCENARIO_LIST: '/phishing/api/v1/phishing/vishing-scenarios?',
  VISHING_SCENARIO_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/vishing-scenarios/${id}`,
  VISHING_SCENARIO_CREATE: '/phishing/api/v1/phishing/vishing-scenarios',
  VISHING_SCENARIO_PUBLISH: (id: string) =>
    `/phishing/api/v1/phishing/vishing-scenarios/${id}/publish`,
  VISHING_ATTACK_TEMPLATE_LIST:
    '/phishing/api/v1/phishing/vishing-attack-templates?',
  VISHING_ATTACK_TEMPLATE_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/vishing-attack-templates/${id}`,
  VISHING_ATTACK_TEMPLATE_CREATE:
    '/phishing/api/v1/phishing/vishing-attack-templates',
  VISHING_VOICES_LIST: '/phishing/api/v1/phishing/vishing-voices?',
  VOICE_SERVER_LIST: '/phishing/api/v1/phishing/voice-server-configurations?',
  VOICE_SERVER_DEFAULT:
    '/phishing/api/v1/phishing/voice-server-configurations/default',
  VOICE_SERVER_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/voice-server-configurations/${id}`,
  VOICE_SERVER_CREATE: '/phishing/api/v1/phishing/voice-server-configurations',
  VOICE_SERVER_SET_DEFAULT: (id: string) =>
    `/phishing/api/v1/phishing/voice-server-configurations/${id}/set-default`,
  VOICE_SERVER_TEST: (id: string) =>
    `/phishing/api/v1/phishing/voice-server-configurations/${id}/test`,
  USER_RISK_PROFILE_LIST: '/phishing/api/v1/phishing/user-risk-profiles?',
  USER_RISK_PROFILE_EXPORT:
    '/phishing/api/v1/phishing/user-risk-profiles/export',
  USER_RISK_PROFILE_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/user-risk-profiles/${id}`,
  TRAINING_RISK_SCORE: (userId: string) =>
    `/phishing/api/v1/phishing/training-risk-score/${userId}`,

  // Breach Detection (Task-08)
  BREACH_LIST: '/phishing/api/v1/phishing/breaches?',
  BREACH_DETAILS: (id: string) => `/phishing/api/v1/phishing/breaches/${id}`,
  BREACH_UPDATE_STATUS: (id: string) =>
    `/phishing/api/v1/phishing/breaches/${id}/status`,
  BREACH_DELETE: (id: string) => `/phishing/api/v1/phishing/breaches/${id}`,
  BREACH_EXPORT: '/phishing/api/v1/phishing/breaches/export',
  RECIPIENT_BREACH_LIST: '/phishing/api/v1/phishing/recipient-breaches?',
  RECIPIENT_BREACH_DETAILS: (id: string) =>
    `/phishing/api/v1/phishing/recipient-breaches/${id}`,
  RECIPIENT_BREACH_NOTIFY: (id: string) =>
    `/phishing/api/v1/phishing/recipient-breaches/${id}/notify`,
  RECIPIENT_BREACH_RESET: (id: string) =>
    `/phishing/api/v1/phishing/recipient-breaches/${id}/reset-password`,
  RECIPIENT_BREACH_RESOLVE: (id: string) =>
    `/phishing/api/v1/phishing/recipient-breaches/${id}/resolve`,
  BREACH_CONFIG_GET: '/phishing/api/v1/phishing/breach-detection/config',
  BREACH_CONFIG_UPDATE: '/phishing/api/v1/phishing/breach-detection/config',
  BREACH_SYNC: '/phishing/api/v1/phishing/breach-detection/sync',

  // Dashboard & Analytics (Task-09)
  DASHBOARD_PHISH_PRONE_DATA:
    '/phishing/api/v1/phishing/dashboard/phish-prone-data?',
  DASHBOARD_OVERVIEW: '/phishing/api/v1/phishing/dashboard/overview',
  DASHBOARD_RISK_IMPACT: '/phishing/api/v1/phishing/campaigns/risk-impact',
  DASHBOARD_KPI: '/phishing/api/v1/phishing/dashboard/kpi-metrics?',
  DASHBOARD_CAMPAIGNS: '/phishing/api/v1/phishing/dashboard/campaigns?',
  DASHBOARD_EMAIL_STATS: '/phishing/api/v1/phishing/dashboard/email-stats',
  DASHBOARD_USER_RISK: '/phishing/api/v1/phishing/dashboard/user-risk',
  DASHBOARD_TRENDS: '/phishing/api/v1/phishing/dashboard/trends?',
  DASHBOARD_ALL_TRENDS: '/phishing/api/v1/phishing/dashboard/trends/all?',
  DASHBOARD_BREACH_SUMMARY:
    '/phishing/api/v1/phishing/dashboard/breach-summary',
  DASHBOARD_REFRESH: '/phishing/api/v1/phishing/dashboard/refresh',

  INSECURE_WEB_FINDINGS_SUMMARY: '/breach/api/v1/insecure-web/findings/summary',

  // Reports (Task-09)
  REPORTS_CAMPAIGNS: '/phishing/api/v1/phishing/reports/campaigns?',
  REPORTS_CAMPAIGN_DETAIL: (id: string) =>
    `/phishing/api/v1/phishing/reports/campaigns/${id}`,
  REPORTS_CAMPAIGN_EXPORT: (id: string, format: string) =>
    `/phishing/api/v1/phishing/reports/campaigns/${id}/export?format=${format}`,
  REPORTS_EMAIL_ACTIVITY: '/phishing/api/v1/phishing/reports/email-activity?',
  REPORTS_USER_RISK: '/phishing/api/v1/phishing/reports/user-risk?',
  REPORTS_USER_RISK_EXPORT:
    '/phishing/api/v1/phishing/reports/user-risk/export?',
  REPORTS_BREACH_SUMMARY: '/phishing/api/v1/phishing/reports/breach-summary',

  // Analytics (Task-09)
  ANALYTICS_PHISH_PRONE:
    '/phishing/api/v1/phishing/analytics/phish-prone-percentage',
  ANALYTICS_PHISH_PRONE_CAMPAIGN: (id: string) =>
    `/phishing/api/v1/phishing/analytics/phish-prone-percentage/${id}`,
  ANALYTICS_REPEAT_OFFENDERS:
    '/phishing/api/v1/phishing/analytics/repeat-offenders?',
  ANALYTICS_DELIVERY_RATE: '/phishing/api/v1/phishing/analytics/delivery-rate',
  ANALYTICS_COMPROMISE_RATE:
    '/phishing/api/v1/phishing/analytics/compromise-rate',
  ANALYTICS_REPORT_RATE: '/phishing/api/v1/phishing/analytics/report-rate',
  ANALYTICS_USER_RISK: (userId: string) =>
    `/phishing/api/v1/phishing/analytics/user-risk/${userId}`,
  ANALYTICS_RECALCULATE_RISK:
    '/phishing/api/v1/phishing/analytics/recalculate-risk',

  /** Dynamic dropdowns */
  DYNAMIC_DROPDOWN: '/phishing/api/v1/phishing/',

  GET_ACTIVE_TIME_ZONE_LIST: '/registration/api/v1/dropdown/timezones/active',
  GET_ACTIVE_LANGUAGE_LIST: '/registration/api/v1/dropdown/languages/active',
  CONFIGURATION_PAYLOAD_TYPES: '/phishing/api/v1/phishing/payload-types',
  CONFIGURATION_LANDING_PAGE_CATEGORIES:
    '/phishing/api/v1/phishing/landing-page-categories',
  CONFIGURATION_DATA_CAPTURE_TYPES:
    '/phishing/api/v1/phishing/data-capture-types',
  CONFIGURATION_CAMPAIGN_OBJECTIVES:
    '/phishing/api/v1/phishing/campaign-objectives',
  CONFIGURATION_PERSONALIZATION_LEVELS:
    '/phishing/api/v1/phishing/personalization-levels',
  CONFIGURATION_BRANDS: '/phishing/api/v1/phishing/brands',
  CONFIGURATION_DECEPTION_LEVELS: '/phishing/api/v1/phishing/deception-levels',
  CONFIGURATION_TONES: '/phishing/api/v1/phishing/tones',
  CONFIGURATION_ATTACKER_PERSONAS:
    '/phishing/api/v1/phishing/attacker-personas',
  CONFIGURATION_CALL_TO_ACTIONS: '/phishing/api/v1/phishing/call-to-actions',
  CONFIGURATION_SOCIAL_ENGINEERING_STRATEGIES:
    '/phishing/api/v1/phishing/social-engineering-strategies',
  CONFIGURATION_EMOTIONAL_TRIGGERS:
    '/phishing/api/v1/phishing/emotional-triggers',
  CONFIGURATION_ATTACK_TECHNIQUES:
    '/phishing/api/v1/phishing/attack-techniques',
  CONFIGURATION_TRIGGER_EVENTS: '/phishing/api/v1/phishing/trigger-events',
  CONFIGURATION_EXPECTED_USER_ACTIONS:
    '/phishing/api/v1/phishing/expected-user-actions',
  CONFIGURATION_CONSTRAINTS_DATA: '/phishing/api/v1/phishing/constraints-data',
  CONFIGURATION_DIFFICULTY: '/phishing/api/v1/phishing/difficulties',
  CONFIGURATION_URGENCY_LEVELS: '/phishing/api/v1/phishing/urgency-levels',
  CONFIGURATION_DIFFICULTIES: '/phishing/api/v1/phishing/difficulties',

  /** Client admin assigned products (same as content-module) */
  CLIENT_ADMIN_ASSIGN_PRODUCT_LIST:
    '/registration/api/v1/client/admin/products/assigned/:clientAdminId?',

  /** CMS — training module filters & topics */
  CMS_TOPIC_FILTER_BY_PACKAGE: '/cms/api/v1/topics/filter/by-package/',
  CMS_PRIVATE_TOPICS: '/cms/api/v1/topics/private?',
  CMS_CATEGORY_LIST: '/cms/api/v1/categories',
  CMS_CONTENT_TYPE_LIST: '/cms/api/v1/content-types',
  CMS_COMPLIANCE_LIST: '/cms/api/v1/compliances',
  CMS_TAG_LIST: '/cms/api/v1/tags',
  REGISTRATION_COUNTRY_LIST: '/registration/api/v1/dropdown/countries',

  GET_BREACH_MONITOR_EMAIL_SUMMARY:
    '/breach/api/v1/insecure-web/findings/summary',
  GET_BREACH_MONITOR_BREACHED_EMAIL_LIST:
    '/breach/api/v1/insecure-web/findings?',
  GET_BREACH_MONITOR_BREACHED_EMAIL_ACTIVITY:
    '/breach/api/v1/insecure-web/findings/activity?',
  GET_BREACH_MONITOR_BREACHED_EMAIL_SOURCES:
    '/breach/api/v1/insecure-web/findings/sources?',

  GET_PHISHING_COURSE_STATISTICS:
    '/phishing/api/v1/phishing/phishing-course/statistics',
  GET_PHISHING_COURSE_DETAILS:
    '/phishing/api/v1/phishing/phishing-course/details?',

  // Deepfake Video API
  DEEPFAKE_VIDEO_LIST: '/phishing/api/v1/deepfake/videos?',
  DEEPFAKE_VIDEO_CREATE: '/phishing/api/v1/deepfake/videos',
  DEEPFAKE_VIDEO_DETAILS: (videoId: string) =>
    `/phishing/api/v1/deepfake/videos/${videoId}`,
  DEEPFAKE_VIDEO_DELETE: (videoId: string) =>
    `/phishing/api/v1/deepfake/videos/${videoId}`,
  DEEPFAKE_VIDEO_STATUS: (videoId: string) =>
    `/phishing/api/v1/deepfake/videos/${videoId}/status`,
  DEEPFAKE_VIDEO_START_STEP_1: '/phishing/api/v1/deepfake/videos/step/1',
  DEEPFAKE_VIDEO_UPDATE_STEP: (videoId: string, step: number) =>
    `/phishing/api/v1/deepfake/videos/${videoId}/step/${step}`,
  DEEPFAKE_UPLOAD_BACKGROUND: '/phishing/api/v1/deepfake/background',
  DEEPFAKE_IMAGE_LIST: '/phishing/api/v1/deepfake/backgrounds?',
  DEEPFAKE_VOICE_LIST: '/phishing/api/v1/deepfake/voices?',
  DEEPFAKE_VOICE_CLONE: (videoId: string) =>
    `/phishing/api/v1/deepfake/videos/${videoId}/step/4`,
  DEEPFAKE_VOICE_DELETE: (voiceCloneId: string) =>
    `/phishing/api/v1/deepfake/voices/${voiceCloneId}`,
  DEEPFAKE_MICRO_CONTENT: '/phishing/api/v1/deepfake/micro-content',
  DEEPFAKE_MICRO_CONTENT_DELETE: (videoId: string) =>
    `/phishing/api/v1/deepfake/micro-content/${videoId}`,
  DEEPFAKE_VIDEO_RENDER_PROVIDERS:
    '/phishing/api/v1/deepfake/video-render-providers',
};
