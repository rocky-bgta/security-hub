export const API_END_POINTS = {
  GET_ROLE_LIST: '/registration/api/v1/role?',
  CREATE_ROLE: '/registration/api/v1/role',
  UPDATE_ROLE: '/registration/api/v1/role/:id',
  DELETE_ROLE: '/registration/api/v1/role/:id',

  GET_LOGGED_IN_USER_PERMISSIONS: '/registration/api/v1/role-permissions',
  UPDATE_ROLE_PERMISSIONS: '/registration/api/v1/role-permissions',
  GET_ROLE_MENU_PERMISSIONS:
    '/registration/api/v1/role-permissions/menu?role=:id',

  GET_MENU_LIST: '/registration/api/v1/menus',
  CREATE_MENU: '/registration/api/v1/menus',
  UPDATE_MENU: '/registration/api/v1/menus/:id',
  DELETE_MENU: '/registration/api/v1/menus/:id',

  GET_CATEGORY_LIST: '/cms/api/v1/categories',
  GET_CATEGORY: '/cms/api/v1/categories/:id',
  CREATE_CATEGORY: '/cms/api/v1/categories',
  UPDATE_CATEGORY: '/cms/api/v1/categories/:id',
  DELETE_CATEGORY: '/cms/api/v1/categories/:id',

  GET_ORGANIZATION_TYPE_LIST: '/registration/api/v1/metadata/organization-type',
  GET_ORGANIZATION_TYPE: '/registration/api/v1/metadata/organization-type/:id',
  CREATE_ORGANIZATION_TYPE: '/registration/api/v1/metadata/organization-type',
  UPDATE_ORGANIZATION_TYPE: '/registration/api/v1/metadata/organization-type',
  DELETE_ORGANIZATION_TYPE:
    '/registration/api/v1/metadata/organization-type/:id',

  GET_ORGANIZATION_SIZE_LIST:
    '/registration/api/v1/dropdown/organization-sizes/active',
  GET_ORGANIZATION_SIZE: '/registration/api/v1/dropdown/organization-sizes/:id',
  CREATE_ORGANIZATION_SIZE: '/registration/api/v1/dropdown/organization-sizes',
  UPDATE_ORGANIZATION_SIZE:
    '/registration/api/v1/dropdown/organization-sizes/:id',
  DELETE_ORGANIZATION_SIZE:
    '/registration/api/v1/dropdown/organization-sizes/:id',

  GET_LANGUAGE_LIST: '/registration/api/v1/dropdown/languages/active',
  GET_LANGUAGE: '/registration/api/v1/dropdown/languages/:id',
  CREATE_LANGUAGE: '/registration/api/v1/dropdown/languages',
  UPDATE_LANGUAGE: '/registration/api/v1/dropdown/languages/:id',
  DELETE_LANGUAGE: '/registration/api/v1/dropdown/languages/:id',

  GET_STATE_LIST: '/registration/api/v1/dropdown/states',
  GET_STATE: '/registration/api/v1/dropdown/states/:id',
  CREATE_STATE: '/registration/api/v1/dropdown/states',
  UPDATE_STATE: '/registration/api/v1/dropdown/states/:id',
  DELETE_STATE: '/registration/api/v1/dropdown/states/:id',
  GET_STATE_LIST_BY_COUNTRY: '/registration/api/v1/dropdown/states/country/:id',

  GET_TIME_ZONE_LIST: '/registration/api/v1/dropdown/timezones',
  GET_TIME_ZONE: '/registration/api/v1/dropdown/timezones/:id',
  CREATE_TIME_ZONE: '/registration/api/v1/dropdown/timezones',
  UPDATE_TIME_ZONE: '/registration/api/v1/dropdown/timezones/:id',
  DELETE_TIME_ZONE: '/registration/api/v1/dropdown/timezones/:id',

  GET_INDUSTRIES_LIST: '/registration/api/v1/dropdown/industries/active',
  GET_INDUSTRIES: '/registration/api/v1/dropdown/industries/:id',
  CREATE_INDUSTRIES: '/registration/api/v1/dropdown/industries',
  UPDATE_INDUSTRIES: '/registration/api/v1/dropdown/industries/:id',
  DELETE_INDUSTRIES: '/registration/api/v1/dropdown/industries/:id',

  GET_SUB_INDUSTRIES_LIST: '/registration/api/v1/dropdown/sub-industries?',
  GET_SUB_INDUSTRIES: '/registration/api/v1/dropdown/sub-industries/:id',
  CREATE_SUB_INDUSTRIES: '/registration/api/v1/dropdown/sub-industries',
  UPDATE_SUB_INDUSTRIES: '/registration/api/v1/dropdown/sub-industries/:id',
  DELETE_SUB_INDUSTRIES: '/registration/api/v1/dropdown/sub-industries/:id',

  GET_COMPLIANCE_LIST: '/cms/api/v1/compliances',
  GET_COMPLIANCE: '/cms/api/v1/compliances/:id',
  CREATE_COMPLIANCE: '/cms/api/v1/compliances',
  UPDATE_COMPLIANCE: '/cms/api/v1/compliances/:id',
  DELETE_COMPLIANCE: '/cms/api/v1/compliances/:id',

  GET_CONTENT_TYPE_LIST: '/cms/api/v1/content-types',
  GET_CONTENT_TYPE: '/cms/api/v1/content-types/:id',
  CREATE_CONTENT_TYPE: '/cms/api/v1/content-types',
  UPDATE_CONTENT_TYPE: '/cms/api/v1/content-types/:id',
  DELETE_CONTENT_TYPE: '/cms/api/v1/content-types/:id',

  GET_TAG_LIST: '/cms/api/v1/tags',
  CREATE_TAG: '/cms/api/v1/tags',
  UPDATE_TAG: '/cms/api/v1/tags/:id',
  UPDATE_TAG_STATUS: '/cms/api/v1/tags/:id/status',
  DELETE_TAG: '/cms/api/v1/tags/:id',

  GET_COUNTRY_LIST: '/registration/api/v1/dropdown/countries',
  GET_COUNTRY: '/registration/api/v1/dropdown/countries/:id',
  CREATE_COUNTRY: '/registration/api/v1/dropdown/countries',
  UPDATE_COUNTRY: '/registration/api/v1/dropdown/countries/:id',
  DELETE_COUNTRY: '/registration/api/v1/dropdown/countries/:id',
  GET_ACTIVE_COUNTRY_LIST: '/registration/api/v1/dropdown/countries/active',

  GET_MSP_TYPE_LIST: '/registration/api/v1/dropdown/msp-types/active',
  GET_MSP_TYPE: '/registration/api/v1/dropdown/msp-types/:id',
  CREATE_MSP_TYPE: '/registration/api/v1/dropdown/msp-types',
  UPDATE_MSP_TYPE: '/registration/api/v1/dropdown/msp-types/:id',
  DELETE_MSP_TYPE: '/registration/api/v1/dropdown/msp-types/:id',

  GET_LEADERBOARD_LIST: '/universal/api/v1/leaderboards?',
  CREATE_LEADERBOARD: '/universal/api/v1/leaderboards',
  UPDATE_LEADERBOARD: '/universal/api/v1/leaderboards/:id',
  DELETE_LEADERBOARD: '/universal/api/v1/leaderboards/:id',

  CREATE_POLL_SURVEY: '/universal/api/polls',
  GET_POLL_SURVEY_LIST: '/universal/api/polls?',
  GET_POLL_SURVEY_DETAILS: '/universal/api/polls/:id',
  UPDATE_POLL_SURVEY: '/universal/api/polls/:id',

  CREATE_LATEST_NEWS: '/universal/api/latest-news',
  GET_LATEST_NEWS_LIST: '/universal/api/latest-news?',
  GET_LATEST_NEWS_DETAILS: '/universal/api/latest-news/:id',
  DELETE_LATEST_NEWS: '/universal/api/latest-news/:id',
  UPDATE_LATEST_NEWS: '/universal/api/latest-news/:id',

  GET_LATEST_NEWS_CATEGORY_LIST: '/universal/api/categories/active',

  GET_POLICY_TYPE_LIST: '/universal/api/policy-types?',
  CREATE_POLICY_TYPE: 'universal/api/policy-types',
  UPDATE_POLICY_TYPE: '/universal/api/policy-types/:id',
  DELETE_POLICY_TYPE: '/universal/api/policy-types/:id',

  GET_NEWS_CATEGORY_LIST: '/universal/api/categories?',
  CREATE_LATEST_NEWS_CATEGORY: '/universal/api/categories',
  UPDATE_LATEST_NEWS_CATEGORY: '/universal/api/categories/:id',
  DELETE_LATEST_NEWS_CATEGORY: '/universal/api/categories/:id',

  CREATE_POLICY: '/universal/api/policies',
  GET_POLICY_LIST: '/universal/api/policies?',
  GET_POLICY_DETAILS: '/universal/api/policies/:id',
  UPDATE_POLICY: '/universal/api/policies/:id',
  DELETE_POLICY: '/universal/api/policies/:id',
  GET_CLIENT_AND_USER_POLICY_LIST: '/universal/api/policies/list?',

  GET_KNOWLEDGE_HUB_LIST: '/universal/api/v1/knowledge-hub?',
  GET_KNOWLEDGE_HUB: '/universal/api/v1/knowledge-hub/:id',
  CREATE_KNOWLEDGE_HUB: '/universal/api/v1/knowledge-hub',
  GET_KNOWLEDGE_HUB_DETAILS: '/universal/api/v1/knowledge-hub/:id',
  UPDATE_KNOWLEDGE_HUB: '/universal/api/v1/knowledge-hub/:id',
  DELETE_KNOWLEDGE_HUB: '/universal/api/v1/knowledge-hub/:id',
  GET_KNOWLEDGE_HUB_ACTIVE_LIST: '/universal/api/v1/knowledge-hub/active?',

  GET_SUPPORT_TICKETS: '/universal/api/v1/support-tickets?',
  CREATE_SUPPORT_TICKET: '/universal/api/v1/support-tickets',
  GET_SUPPORT_TICKET_DETAILS: '/universal/api/v1/support-tickets/:id',
  SOLVE_SUPPORT_TICKET: '/universal/api/v1/support-tickets/:id/status',
  GET_COMMENTS: '/universal/api/v1/support-tickets/:id/comments',
  CREATE_COMMENT: '/universal/api/v1/support-tickets/:id/comments',
  DELETE_COMMENT: '/universal/api/v1/support-tickets/comments/:id',

  GET_USER_SUB_PACKAGES: '/cms/api/v1/client/subpackages/list',
  GET_PRODUCT_LIST: '/cms/api/v1/client-product/:id',
  GET_PACKAGE_LIST: '/cms/api/v1/client-product/packages?productId=:id',
  GET_MSP_PRODUCT_LIST: '/registration/api/v1/msp-product/:id',
  GET_MSP_PACKAGE_LIST:
    '/registration/api/v1/msp-product/packages?productId=:id',
  GET_CMS_PRODUCT_LIST: '/cms/api/v1/products?',

  GET_CLIENTS: '/registration/api/v1/client/admin/list',

  GET_BILLING_ACTION_LIST: '/billing/api/v1/invoice/action',
  GET_BILLING_ACTION: '/billing/api/v1/invoice/action/:id',
  CREATE_BILLING_ACTION: '/billing/api/v1/invoice/action',
  UPDATE_BILLING_ACTION: '/billing/api/v1/invoice/action/:id',
  DELETE_BILLING_ACTION: '/billing/api/v1/invoice/action/:id',

  GET_BILLING_NEXT_STEP_LIST: '/billing/api/v1/invoice/next-step',
  GET_BILLING_NEXT_STEP: '/billing/api/v1/invoice/next-step/:id',
  CREATE_BILLING_NEXT_STEP: '/billing/api/v1/invoice/next-step',
  UPDATE_BILLING_NEXT_STEP: '/billing/api/v1/invoice/next-step/:id',
  DELETE_BILLING_NEXT_STEP: '/billing/api/v1/invoice/next-step/:id',

  GET_DEPARTMENT_LIST: '/registration/api/v1/departments?',
  GET_DEPARTMENT: '/registration/api/v1/departments/:id',
  CREATE_DEPARTMENT: '/registration/api/v1/departments',
  UPDATE_DEPARTMENT: '/registration/api/v1/departments',
  DELETE_DEPARTMENT: '/registration/api/v1/departments/:id',

  GET_SUPPORT_TICKET_TYPE_LIST: '/universal/api/v1/support-ticket-types?',
  GET_SUPPORT_TICKET_TYPE: '/universal/api/v1/support-ticket-types/:id',
  CREATE_SUPPORT_TICKET_TYPE: '/universal/api/v1/support-ticket-types',
  UPDATE_SUPPORT_TICKET_TYPE: '/universal/api/v1/support-ticket-types/:id',
  DELETE_SUPPORT_TICKET_TYPE: '/universal/api/v1/support-ticket-types/:id',

  GET_SUPPORT_ACTIVE_TYPE_LIST: '/universal/api/v1/support-ticket-types/all',

  GET_USER_RANGE_LIST: '/cms/api/v1/user-ranges?',
  GET_USER_RANGE: '/cms/api/v1/user-ranges/:id',
  CREATE_USER_RANGE: '/cms/api/v1/user-ranges',
  UPDATE_USER_RANGE: '/cms/api/v1/user-ranges/:id',
  DELETE_USER_RANGE: '/cms/api/v1/user-ranges/:id',

  GET_CREDIT_REASON_LIST: '/registration/api/credit-allocation-reason?',
  GET_CREDIT_REASON: '/registration/api/credit-allocation-reason/:id',
  CREATE_CREDIT_REASON: '/registration/api/credit-allocation-reason',
  UPDATE_CREDIT_REASON: '/registration/api/credit-allocation-reason/:id',
  DELETE_CREDIT_REASON: '/registration/api/credit-allocation-reason/:id',

  GET_NET_TERM_LIST: '/registration/api/net-term-configuration?',
  GET_NET_TERM: '/registration/api/net-term-configuration/:id',
  CREATE_NET_TERM: '/registration/api/net-term-configuration',
  UPDATE_NET_TERM: '/registration/api/net-term-configuration/:id',
  DELETE_NET_TERM: '/registration/api/net-term-configuration/:id',

  GET_SUSPEND_REASON_LIST: '/registration/api/v1/dropdown/suspend-reasons',
  GET_SUSPEND_REASON: '/registration/api/v1/dropdown/suspend-reasons/:id',
  CREATE_SUSPEND_REASON: '/registration/api/v1/dropdown/suspend-reasons',
  UPDATE_SUSPEND_REASON: '/registration/api/v1/dropdown/suspend-reasons/:id',
  DELETE_SUSPEND_REASON: '/registration/api/v1/dropdown/suspend-reasons/:id',

  GET_USER_SUMMARY_REPORT: '/registration/api/v1/reports/user-summary?',
  GET_PRODUCT_LICENSE_ONBOARDING_REPORT:
    '/registration/api/v1/reports/product-license-onboarding',
  GET_SUPPORT_TICKET_REPORT_SUMMARY:
    '/universal/api/v1/support-ticket-reports/summary',
  GET_SUPPORT_TICKET_STATUS_DISTRIBUTION:
    '/universal/api/v1/support-ticket-reports/status-distribution',
  GET_SUPPORT_TICKET_RECENT_TICKETS:
    '/universal/api/v1/support-ticket-reports/recent-tickets?',
  GET_SUPPORT_RESOLUTION_TIME: '/universal/api/v1/support-resolution-time?',

  GET_PACKAGE_ASSIGNMENT_REPORT:
    '/cms/api/v1/client/package-assignment-report?',
  GET_SUBSCRIPTION_SUMMARY_REPORT:
    '/billing/api/v1/reports/subscription-summary?',
  GET_PAYMENT_REPORT: '/billing/api/v1/reports/payment',
  GET_DUE_PAYMENT_REPORT: '/billing/api/v1/reports/due-payment',
  GET_INVOICE_SUMMARY_REPORT: '/billing/api/v1/reports/invoice-summary',
  GET_CLIENT_ADMIN_PRODUCT_USAGE_REPORT:
    '/cms/api/v1/client-admin/product-usage-report',
  GET_CLIENT_ADMIN_PRODUCT_USAGE_REPORT_EXPORT:
    '/cms/api/v1/client-admin/product-usage-report/export',
  GET_TOPIC_ASSIGNMENT_REPORT: '/cms/api/v1/reports/topic-assignment',
  GET_SUB_PACKAGE_REPORT: '/cms/api/v1/reports/sub-package',
  GET_ISSUED_CERTIFICATES_REPORT:
    '/cms/api/v1/client/issued-certificate-report?',
  GET_ISSUED_CERTIFICATES_REPORT_SUMMARY:
    '/cms/api/v1/client/issued-certificate-report/summary?',
  GET_EXPIRED_CERTIFICATES_REPORT:
    '/cms/api/v1/client/expired-certificate-report?',
  GET_EXPIRED_CERTIFICATES_REPORT_SUMMARY:
    '/cms/api/v1/client/expired-certificate-report/summary?',
};
