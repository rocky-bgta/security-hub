export const API_END_POINTS = {
  PRODUCT_LIST: '/cms/api/v1/products?',
  PRODUCT_DETAILS: '/cms/api/v1/products/',
  PRODUCT_CREATE: '/cms/api/v1/products',
  PRODUCT_UPDATE: '/cms/api/v1/products/',
  PRODUCT_DELETE: '/cms/api/v1/products/',
  PRODUCT_ENABLED: '/cms/api/v1/products/enabled?',
  PRODUCT_BULK_UPDATE: '/cms/api/v1/products/bulk-update',
  PRODUCT_BULK_DELETE: '/cms/api/v1/products/bulk-delete',
  PRODUCT_BULK_EXPORT: '/cms/api/v1/products/bulk-export',
  PRODUCT_EXAM_CSV_UPLOAD: '/cms/api/v1/new-questions/import-csv?',

  COURSE_LIST: '/cms/api/v1/courses?',
  COURSE_DETAILS: '/cms/api/v1/courses/',
  COURSE_CREATE: '/cms/api/v1/courses',
  COURSE_UPDATE: '/cms/api/v1/courses/',
  COURSE_DELETE: '/cms/api/v1/courses/',
  COURSE_ENABLED: '/cms/api/v1/courses/enabled?',
  COURSE_BULK_UPDATE: '/cms/api/v1/courses/bulk-update',
  COURSE_BULK_DELETE: '/cms/api/v1/courses/bulk-delete',
  COURSE_BULK_EXPORT: '/cms/api/v1/courses/bulk-export',

  TOPIC_LIST: '/cms/api/v1/topics?',
  TOPIC_DETAILS: '/cms/api/v1/topics/',
  TOPIC_CREATE: '/cms/api/v1/topics',
  TOPIC_UPDATE: '/cms/api/v1/topics/',
  TOPIC_DELETE: '/cms/api/v1/topics/',
  TOPIC_ENABLED: '/cms/api/v1/topics/enabled?',
  TOPIC_BULK_UPDATE: '/cms/api/v1/topics/bulk-update',
  TOPIC_BULK_DELETE: '/cms/api/v1/topics/bulk-delete',
  TOPIC_BULK_EXPORT: '/cms/api/v1/topics/bulk-export',
  FILTER_TOPIC_LIST: '/cms/api/v1/topics/filter/by-package/',
  TOPIC_FILTER: '/cms/api/v1/topics/filter',
  TOPIC_FILTER_BY_PRODUCT: '/cms/api/v1/topics/filter/by-product/',
  USER_TOPIC_DETAILS: '/cms/api/v1/topics/client/topics/:topicId',
  TOPIC_BY_PACKAGE: '/cms/api/v1/topics/product/package/topics?',

  QUESTION_CREATE: '/cms/api/v1/new-questions',
  QUESTION_LIST: '/cms/api/v1/new-questions?',
  QUESTION_DELETE: '/cms/api/v1/new-questions/',
  QUESTION_UPDATE: '/cms/api/v1/new-questions/',
  QUESTION_DETAILS: '/cms/api/v1/new-questions/',

  PACKAGE_LIST: '/cms/api/v1/packages?',
  PACKAGE_DETAILS: '/cms/api/v1/packages/',
  CLIENT_ASSIGNED_PACKAGE_DETAILS:
    '/registration/api/v1/client/admin/product/package/detail/:id',
  PACKAGE_CREATE: '/cms/api/v1/packages',
  PACKAGE_UPDATE: '/cms/api/v1/packages/',
  PACKAGE_DELETE: '/cms/api/v1/packages/',
  PACKAGE_ENABLED: '/cms/api/v1/packages/enabled?',
  PACKAGE_BULK_UPDATE: '/cms/api/v1/packages/bulk-update',
  PACKAGE_BULK_DELETE: '/cms/api/v1/packages/bulk-delete',
  PACKAGE_BULK_EXPORT: '/cms/api/v1/packages/bulk-export',
  PACKAGE_EXISTS: '/cms/api/v1/packages/exists?name=',
  PACKAGE_ASSIGN: '/cms/api/v1/client/packages/assign?',

  // Chapters API Endpoints
  CHAPTER_LIST: '/cms/api/v1/chapters?',
  CHAPTER_DETAILS: '/cms/api/v1/chapters/',
  CHAPTER_CREATE: '/cms/api/v1/chapters',
  CHAPTER_UPDATE: '/cms/api/v1/chapters/',
  CHAPTER_DELETE: '/cms/api/v1/chapters/',

  // Features API Endpoints
  FEATURE_LIST: '/cms/api/v1/features?',
  FEATURE_CREATE: '/cms/api/v1/features',
  FEATURE_UPDATE: '/cms/api/v1/features/',
  FEATURE_ENABLED: '/cms/api/v1/features/enabled?',
  FEATURE_DETAILS: (id: string) => `/cms/api/v1/features/${id}`,
  FEATURE_DELETE: (id: string) => `/cms/api/v1/features/${id}`,
  FEATURE_BULK_UPDATE: '/cms/api/v1/features/bulk-update',
  FEATURE_BULK_DELETE: '/cms/api/v1/features/bulk-delete',
  FEATURE_BULK_EXPORT: '/cms/api/v1/features/bulk-export',

  // Content API Endpoints
  CONTENT_LIST: '/cms/api/v1/contents?',
  CONTENT_CREATE: '/cms/api/v1/contents',
  CONTENT_UPDATE: '/cms/api/v1/contents/',
  CONTENT_DETAILS: '/cms/api/v1/contents/',
  CONTENT_DELETE: '/cms/api/v1/contents/',

  // UPLOAD_URL: '/storages/generate-upload-url?fileName=',
  // FILE_PATH: '/storages/check-upload-status?fileName=',

  UPLOAD_URL: '/cms/api/files/generate-presign',
  FILE_PATH: '/cms/api/files/get-presigned-url?key=',

  // USER API ENDPOINTS
  GET_USER_DETAILS: '/auth/api/v1/auth/user-details',
  GET_USER_INFO: '/registration/api/v1/end-user/:userId',
  UPDATE_USER_INFO: '/registration/api/v1/end-user',
  GET_USER_SUB_PACKAGES: '/cms/api/v1/sub-package-topics?userId=',
  GET_USER_SUB_PACKAGE_TOPICS: '/cms/api/v1/sub-package-topics/:subPackageId',
  USER_TRANSCRIPT_LIST: '/cms/api/v1/client/topics/completed?',
  USER_TOPICS_PROGRESS: '/cms/api/v1/topics/client/user/topics-progress?',
  USER_DASHBOARD_SUMMARY: '/cms/api/v1/client/subpackages/statistics-count',

  USER_ACTIVITY_LOGS: '/registration/api/v1/reports/user-activity/history?',
  ACTIVITY_LOG_LIST: '/registration/api/v1/activity-log/list',
  USER_PACKAGE_LIST: '/cms/api/v1/client/packages?',
  USER_PACKAGE_DETAILS: '/cms/api/v1/client/packages/details?',
  USER_COURSE_LIST: '/cms/api/v1/client/courses?',
  USER_COURSE_DETAILS: '/cms/api/v1/client/courses/',
  USER_BOOKMARK: '/cms/api/v1/sub-package-topics/bookmark',
  USER_BOOKMARK_LIST: '/cms/api/v1/sub-package-topics/bookmarked?',
  GET_CONTENT: '/cms/api/v1/contents/',
  USER_CONTENT_COMPLETE: '/cms/api/v1/contents/client/contents/complete?',
  USER_CERTIFICATE_LIST: '/cms/api/v1/client/certificates?',
  UPDATE_PASSWORD: '/auth/api/v1/auth/password/change',
  USER_LEADERBOARD: '/universal/api/v1/leaderboards/active-web',
  USER_SURVEY: '/universal/api/polls/latest-active',
  USER_POLL_SURVEY_REACT: '/universal/api/polls/:id/votes',
  USER_POLL_SURVEY_SUMMARY: '/universal/api/polls/:id/summary',
  USER_NEWS: '/universal/api/latest-news/active',
  USER_NEWS_REACTION: '/universal/api/latest-news/:id/like',
  USER_CERTIFICATES_STATS: '/cms/api/v1/client/certificate-summary',
  USER_CREATE_EXAM: '/cms/api/v1/client/exam/create',
  USER_EXAM_QUESTIONS:
    '/cms/api/v1/client/exam/:examId/question/:questionNumber',
  USER_QUESTION_SUBMIT: '/cms/api/v1/client/exam/question/submit',
  USER_VALIDATE_ANSWER: '/cms/api/v1/client/exam/validate-answer',
  USER_FINIAL_EXAM_SUBMIT: '/cms/api/v1/client/exam/submit-final',
  USER_PACKAGE_RESET: '/cms/api/v1/user-subpackages/reset?',
  USER_DASHBOARD_CARD_COURSE_LIST:
    '/cms/api/v1/client/user-packages/status-grouped?',
  USER_TOPICS_STATISTICS: '/cms/api/v1/topics/client/user/topics/statistics?',
  CLIENT_ADMIN_USER_ACTIVITIES:
    '/registration/api/v1/reports/user-activity/login-statistics?',

  // Client Admin API
  CLIENT_ADMIN_ASSIGN_PRODUCT_LIST:
    '/registration/api/v1/client/admin/products/assigned/:clientAdminId?',
  CLIENT_ADMIN_LICENSE_OVERVIEW_SUMMARY:
    '/registration/api/v1/client/admin/license-overview-summary',
  CLIENT_LICENSE_ASSIGNMENTS: '/cms/api/v1/client/license-assignments?',
  ASSIGN_PRODUCT: '/registration/api/v1/client/admin/product/reassign',
  END_USER_LIST: '/cms/api/v1/end-user/unassigned?',

  // Certificate API
  CERTIFICATE_HISTORY_LIST: '/cms/api/v1/client/certificate-details?',
  EXAM_CERTIFICATES_LIST: '/cms/api/v1/client/exam-certificates?',
  CERTIFICATE_TEMPLATE_CREATE: '/cms/api/v1/certificate-templates',
  CERTIFICATE_TEMPLATE_LIST: '/cms/api/v1/certificate-templates?',
  CLIENT_ADMIN_CERTIFICATE_TEMPLATE:
    '/cms/api/v1/certificate-templates/client-admin-template?',
  CERTIFICATE_TEMPLATE_DETAILS: (id: string) =>
    `/cms/api/v1/certificate-templates/${id}`,
  CERTIFICATE_TEMPLATE_UPDATE: (id: string) =>
    `/cms/api/v1/certificate-templates/${id}`,
  CERTIFICATE_TEMPLATE_DELETE: (id: string) =>
    `/cms/api/v1/certificate-templates/${id}`,
  GET_CERTIFICATE_TEMPLATE_DETAILS: '/cms/api/v1/certificate-templates/:id',
  ASSIGN_CLIENT_CERTIFICATE_TEMPLATE:
    '/cms/api/v1/client-certificate-templates',
  EXPIRING_CERTIFICATES: '/cms/api/v1/client/expiring-certificates?',
  EXPIRING_CERTIFICATES_EXPORT_REPORT:
    '/cms/api/v1/client/export-certificates?',
  SEND_EMAIL_EXPIRING_CERTIFICATES:
    '/cms/api/v1/client/email-expiring-certificates',
  CERTIFICATE_SUMMARY_STATS: '/cms/api/v1/client/certificate-summary-stats?',

  // Client Admin Product Api
  CLIENT_ADMIN_PRODUCT_ANALYTICS_LIST:
    '/cms/api/v1/packages/client/product-analytics?',
  CLIENT_ADMIN_PRODUCT_ANALYTICS_EXPORT:
    '/cms/api/v1/packages/client/product-analytics/download?',
  ASSIGN_MULTIPLE_PACKAGE: '/cms/api/v1/client/packages/assign-multiple',

  // Client Admin package API
  ASSIGNED_PACKAGE_LIST: '/cms/api/v1/packages/client/assigned-packages?',
  AVAILABLE_PACKAGE_LIST: '/cms/api/v1/packages/client/packages/available?',
  LICENSE_HISTORY_LIST: '/cms/api/v1/packages/client/license-history?',
  CLIENT_LICENSE_HISTORY_LIST:
    '/registration/api/v1/super-admin/license-history?',
  MSP_LICENSE_HISTORY_LIST:
    '/registration/api/v1/super-admin/msp-license-history?',
  PACKAGE_PERFORMANCE_REPORT:
    '/cms/api/v1/packages/client/package-performance?',

  EXAM_LIST: '/cms/api/v1/package-exams/search?',
  EDIT_EXAM: (id: string) => `/cms/api/v1/package-exams/${id}`,

  // Country API
  COUNTRY_LIST: '/registration/api/v1/dropdown/countries',
  PROVINCE_LIST:
    '/registration/api/v1/dropdown/states/country/:countryId/active',

  // Compliance API
  COMPLIANCE_LIST: '/cms/api/v1/compliances',

  // content type API
  CONTENT_TYPE_LIST: '/cms/api/v1/content-types',

  //Category API
  CATEGORY_LIST: '/cms/api/v1/categories',

  // Product tags API
  TAG_LIST: '/cms/api/v1/tags',

  // Sub Package API
  SUB_PACKAGE_CREATE: '/cms/api/v1/sub-packages',
  SUB_PACKAGE_UPDATE: '/cms/api/v1/sub-packages/',
  SUB_PACKAGE_LIST: '/cms/api/v1/sub-packages?',
  SUB_PACKAGE_EXISTS: '/cms/api/v1/sub-packages/exists?subPackageName=',
  UNASSIGNED_USER_LIST: '/registration/api/v1/end-user/unassigned?',
  ASSIGN_SUB_PACKAGE: '/registration/api/v1/end-user/assign-sub-package',
  USER_SUB_PACKAGE_DETAILS: '/cms/api/v1/sub-packages/client/user-subpackage?',
  SUB_PACKAGE_DETAILS: '/cms/api/v1/sub-packages/:id',
  SUB_PACKAGE_ASSIGNED_USERS:
    '/cms/api/v1/sub-packages/:subPackageId/assigned-users?',

  // Client Admin API
  ORGANIZATION_LIST: '/registration/api/v1/client/admin/list?',

  // Notification API Endpoints
  GET_USER_NOTIFICATIONS: '/notification/api/v1/management/in-app?',
  GET_USER_NOTIFICATION_COUNT: '/notification/api/v1/management/in-app/count',
  MARK_NOTIFICATION_AS_READ: '/notification/api/v1/management/in-app/:id/read',
  MARK_ALL_NOTIFICATIONS_AS_READ:
    '/notification/api/v1/management/in-app/read-all',

  // MSP API
  MSP_LIST: '/registration/api/v1/msp?',
  MSP_ALL_PRODUCT_LIST: '/registration/api/v1/msp/:mspId/products/catalog',
  MSP_CONTACT_SALES: '/registration/api/v1/msp/:mspId/contact-sales',
  MSP_PRODUCT_TOPICS: '/registration/api/v1/msp/:mspId/msp-product-topics',
  MSP_PRODUCT_LIST: '/registration/api/v1/msp-product/:mspId',
  MSP_CLIENT_DROPDOWN: '/registration/api/v1/msp/:mspId/clients/dropdown',
  MSP_PACKAGE_LIST:
    '/registration/api/v1/msp-product/packages?productId=:productId',
  GET_ACTIVE_ORGANIZATION_SIZE_LIST:
    '/registration/api/v1/dropdown/organization-sizes/active',
  MSP_EXPIRED_CERTIFICATE_REPORT_SUMMARY:
    '/registration/api/v1/client/expired-certificate-report/summary?',
  MSP_EXPIRED_CERTIFICATE_REPORT_EXPORT:
    '/registration/api/v1/client/expired-certificate-report/export?',

  // Client API
  CLIENT_LIST: '/registration/api/v1/client/admin/list',
  CLIENT_DETAILS: '/registration/api/v1/client/admin/:id',

  // Billing API
  BILLING_ACTION_LIST: '/billing/api/v1/invoice/action',
  BILLING_NEXT_STEP_LIST: '/billing/api/v1/invoice/next-step',
  BILLING_VAT_RATE_BY_COUNTRY: '/billing/api/v1/vat/country/:countryId',

  // Department API
  GET_DEPARTMENT_LIST: '/registration/api/v1/departments?',

  GET_USER_RANGES: '/cms/api/v1/user-ranges?isActive=true',
  PACKAGE_PRICE_BY_USER_RANGE:
    '/cms/api/v1/package-range-pricing/package/:packageId',

  // Coupon API
  GET_COUPON_BY_CODE: '/billing/api/v1/coupon/code/:code',

  GET_ACTIVE_LANGUAGE_LIST: '/registration/api/v1/dropdown/languages/active',

  // Configuration dropdown endpoints
  CONFIGURATION_PAYLOAD_TYPES: '/phishing/api/v1/phishing/payload-types',
  CONFIGURATION_DIFFICULTY: '/phishing/api/v1/phishing/difficulties',
  CONFIGURATION_TONES: '/phishing/api/v1/phishing/tones',
  CONFIGURATION_ATTACKER_PERSONAS:
    '/phishing/api/v1/phishing/attacker-personas',
  CONFIGURATION_SOCIAL_ENGINEERING_STRATEGIES:
    '/phishing/api/v1/phishing/social-engineering-strategies',
  CONFIGURATION_CAMPAIGN_OBJECTIVES:
    '/phishing/api/v1/phishing/campaign-objectives',
  CONFIGURATION_TRIGGER_EVENTS: '/phishing/api/v1/phishing/trigger-events',
  CONFIGURATION_ATTACK_TECHNIQUES:
    '/phishing/api/v1/phishing/attack-techniques',
  CONFIGURATION_EMOTIONAL_TRIGGERS:
    '/phishing/api/v1/phishing/emotional-triggers',
  CONFIGURATION_URGENCY_LEVELS: '/phishing/api/v1/phishing/urgency-levels',
  CONFIGURATION_BRANDS: '/phishing/api/v1/phishing/brands',
  CONFIGURATION_CALL_TO_ACTIONS: '/phishing/api/v1/phishing/call-to-actions',
  PHISHING_CAMPAIGN_STATISTICS: (channel: string) =>
    `/phishing/api/v1/phishing/campaigns/me/statistics?channel=${channel}`,
  CONFIGURATION_INDUSTRIES: '/registration/api/v1/dropdown/industries/active',
  CONFIGURATION_SUB_INDUSTRIES:
    '/registration/api/v1/dropdown/sub-industries/active',

  // Phishing API
  HAS_RECEIVED_CAMPAIGN:
    '/phishing/api/v1/phishing/campaigns/me/has-received-campaign',
};
