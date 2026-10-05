export const API_END_POINTS = {
  SYSTEM_USER_LIST: '/registration/api/v1/system/user?',
  ADD_SYSTEM_USER: '/registration/api/v1/system/user',
  UPDATE_SYSTEM_USER: '/registration/api/v1/system/user/:id',
  GET_SYSTEM_USER: '/registration/api/v1/system/user/:id',

  // This API is used to check if any type of user exists by email
  CHECK_USER_EXISTS: '/registration/api/v1/end-user/exists-by-email?email=',

  FETCH_END_USER_LIST: '/registration/api/v1/end-user/all?',
  SUSPEND_END_USER: '/registration/api/v1/end-user/:id/status',

  // client admin
  END_USER_LIST: '/registration/api/v1/end-user?',
  ADD_END_USER: '/registration/api/v1/end-user',
  VIEW_END_USER: '/registration/api/v1/end-user/',
  UPDATE_END_USER: '/registration/api/v1/end-user/',
  IMPORT_END_USER: '/registration/api/v1/end-user/import?clientAdminId=',
  BULK_IMPORT_VALIDATE: '/registration/api/v1/end-user/bulk-import/validate',
  BULK_IMPORT_USERS:
    '/registration/api/v1/end-user/bulk-import/:sessionId/users',
  BULK_IMPORT_ONBOARD:
    '/registration/api/v1/end-user/bulk-import/:sessionId/onboard',

  VALIDATE_EMAIL: '/registration/api/v1/client/admin/username/exists?email=',
  VALIDATE_EMAIL_TEMPLATE:
    '/registration/api/v1/client/admin/validate-template',
  ADMIN_RESET_PASSWORD: '/auth/api/v1/auth/password/admin-reset',

  // MSP API
  CREATE_MSP_ADMIN: '/registration/api/v1/msp/onboarding',
  MSP_LIST: '/registration/api/v1/msp/onboarded?',
  MSP_VIEW: '/registration/api/v1/msp/:id/view',
  EDIT_MSP_ADMIN: '/registration/api/v1/msp/:id',
  SUSPEND_MSP: '/registration/api/v1/msp/user/:id/suspend',
  CHANGE_STATUS_MSP_ADMIN: '/registration/api/v1/msp/user/:id/status',
  MSP_AVAILABLE_PRODUCTS: '/registration/api/v1/msp/:mspId/products/assigned',

  // Client Admin API
  CREATE_CLIENT_ADMIN: '/registration/api/v1/client/admin/onboard',
  GET_CLIENT_ADMIN_LIST: '/registration/api/v1/client/admin/list?',
  CLIENT_ADMIN_DETAILS: '/registration/api/v1/client/admin/:id',
  EDIT_CLIENT_ADMIN: '/registration/api/v1/client/admin/:id',
  ONBOARD_CLIENT_ADMIN: '/registration/api/v1/client/admin/buy-now',
  SUSPEND_CLIENT_ADMIN: '/registration/api/v1/client/admin/user/:id/status',
  CHANGE_STATUS_CLIENT_ADMIN: '/registration/api/v1/client/admin/:id/status',

  CREATE_INSECURE_WEB_CONFIG: '/breach/api/v1/insecure-web/config',
  CREATE_SHODAN_CONFIG: '/breach/api/v1/shodan/monitors',

  // Product API
  PRODUCT_LIST: '/cms/api/v1/products?',

  // Global API
  COUNTRY_LIST: '/registration/api/v1/dropdown/countries/active',
  GET_ROLE_LIST: '/registration/api/v1/role?',
  GET_SUSPENSION_REASONS: '/registration/api/v1/dropdown/suspend-reasons',
  MSP_LIST_BY_COUNTRY: '/registration/api/v1/client/admin/by-country?',
  CLIENT_LIST_BY_MSP: '/registration/api/v1/client/admin/get-client-by-msp?',

  GET_ORGANIZATION_TYPE_LIST: '/registration/api/v1/metadata/organization-type',
  GET_ACTIVE_COUNTRY_LIST: '/registration/api/v1/dropdown/countries/active',
  GET_ACTIVE_STATE_LIST: '/registration/api/v1/dropdown/states/active',
  GET_ACTIVE_TIME_ZONE_LIST: '/registration/api/v1/dropdown/timezones/active',
  GET_ACTIVE_LANGUAGE_LIST: '/registration/api/v1/dropdown/languages/active',
  GET_ACTIVE_INDUSTRIES_LIST: '/registration/api/v1/dropdown/industries/active',
  GET_SUB_INDUSTRIES_LIST:
    '/registration/api/v1/dropdown/sub-industries/active',
  GET_ACTIVE_ORGANIZATION_SIZE_LIST:
    '/registration/api/v1/dropdown/organization-sizes/active',
  GET_ACTIVE_MSP_TYPE_LIST:
    '/registration/api/v1/dropdown/msp-types/active',
  GET_TIER_LIST: '/registration/api/tier',
  GET_CREDIT_ALLOCATION_REASON_LIST:
    '/registration/api/credit-allocation-reason',
  GET_NET_DAYS_LIST: '/registration/api/net-term-configuration',
  GET_COMPLIANCE_LIST: '/cms/api/v1/compliances',

  USER_ACTIVITY_LOGS: '/registration/api/v1/reports/user-activity/history?',
  ACTIVITY_LOG_LIST: '/registration/api/v1/activity-log/list',

  BILLING_ACTION_LIST: '/billing/api/v1/invoice/action',
  BILLING_NEXT_STEP_LIST: '/billing/api/v1/invoice/next-step',

  CREATE_PAYMENT_COMMENT: '/billing/api/v1/invoice/comment-log',

  GET_DEPARTMENT_LIST: '/registration/api/v1/departments?',
  GET_VAT_DATA: '/billing/api/v1/vat/country/:countryCode',
  INITIATE_ONLINE_PAYMENT: '/billing/api/v1/payment/online-entry',
  BILLING_VAT_RATE_BY_COUNTRY: '/billing/api/v1/vat/country/:countryId',
  INVOICE_LIST: '/billing/api/v1/invoice/list?',

  PACKAGE_PRICE_BY_USER_RANGE:
    '/cms/api/v1/package-range-pricing/package/:packageId',
  GET_COUPON_BY_CODE: '/billing/api/v1/coupon/code/:code',
  GET_COUPON_BY_ID: '/billing/api/v1/coupon/id/:id',

  // User sync API
  SYNC_USERS: '/registration/api/v1/microsoft/status',
  SYNC_USERS_AUTH_URL: '/registration/api/v1/microsoft/auth-url',
  SYNC_USERS_GROUPS: '/registration/api/v1/microsoft/groups',
  IMPORT_USERS: '/registration/api/v1/microsoft/import-users',
};
