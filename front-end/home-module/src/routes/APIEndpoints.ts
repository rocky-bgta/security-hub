export const API_END_POINTS = {
  PRODUCT_LIST: '/cms/api/v1/client-product/:id',
  // Client Admin API Dashboard API Endpoints
  CLIENT_ADMIN_USER_ACTIVITIES:
    '/registration/api/v1/reports/user-activity/login-statistics?',
  CLIENT_ADMIN_CERTIFICATES_STATS: '/cms/api/v1/client/certificate-summary?',
  CLIENT_ADMIN_DASHBOARD_SUMMARY: '/cms/api/v1/client-dashboard/client-admin',
  CLIENT_ADMIN_LICENSE_STATISTICS:
    '/registration/api/v1/client/admin/license-statistics?',
  CLIENT_ADMIN_USER_RISK_BY_CONTENT:
    '/cms/api/v1/client-dashboard/risk-analysis?',
  CLIENT_ADMIN_PHISHING_ASSET_COUNTS:
    '/phishing/api/v1/phishing/dashboard/asset-counts',

  BOARDED_CLIENT: '/registration/api/v1/client',
  BOARDED_MSP: '/registration/api/v1/msp',
  LICENSE_COUNT: '/registration/api/v1/license/count',
  CONTENT_COUNT: '/registration/api/v1/content/count',

  USER_DASHBOARD_SUMMARY: '/registration/api/v1/client/dashboard/summary',

  LOGIN: '/auth/api/v1/auth/login',
  REFRESH: '/auth/api/v1/auth/refresh',
  LOGOUT: '/auth/api/v1/auth/logout',
  FETCH_MFA_METHODS: '/auth/api/v1/auth/mfa/methods',
  FETCH_USER_MFA_METHODS: '/auth/api/v1/auth/mfa/user-methods',
  SET_DEFAULT_MFA_METHOD: '/auth/api/v1/auth/mfa/default-method',
  REMOVE_MFA_METHOD: '/auth/api/v1/auth/mfa/user-methods/:method',
  GENERATE_OTP: '/auth/api/v1/auth/mfa/generate-otp',
  SETUP_AUTHENTICATOR: '/auth/api/v1/auth/mfa/authenticator/setup',
  VERIFY_AUTHENTICATOR: '/auth/api/v1/auth/mfa/authenticator/verify-setup',

  RESEND_VERIFY_CODE: '/auth/api/v1/auth/mfa/resend-otp',
  VERIFY_OTP: '/auth/api/v1/auth/mfa/verify-otp',
  FETCH_USER_INFO: '/auth/api/v1/auth/user-details',
  SET_COOKIES: '/cms/api/files/get-signed-cookies',
  FORGET_PASSWORD: '/auth/api/v1/auth/password/request-reset',
  RESET_PASSWORD: '/auth/api/v1/auth/password/reset',

  SIDEMENU: '/registration/api/v1/role-permissions/',

  UPLOAD_URL: '/cms/api/files/generate-presign',

  GET_USER_NOTIFICATIONS: '/notification/api/v1/management/in-app?',
  GET_USER_NOTIFICATION_COUNT: '/notification/api/v1/management/in-app/count',
  MARK_NOTIFICATION_AS_READ: '/notification/api/v1/management/in-app/:id/read',
  MARK_ALL_NOTIFICATIONS_AS_READ:
    '/notification/api/v1/management/in-app/read-all',

  // Aspire Admin API Dashboard API Endpoints
  SALES_PROGRESS:
    '/cms/api/v1/organization-dashboard/sales-progress?timeFrame=:timeFrame',
  DASHBOARD_SUMMARY: '/cms/api/v1/organization-dashboard',
  CONTENT_DISTRIBUTION: '/cms/api/v1/topics/distribution',
  LICENSE_DISTRIBUTION:
    '/registration/api/v1/client/admin/organization-license-statistics?clientAdminId=:clientAdminId',
  USER_ACTIVITIES: '/registration/api/v1/dashboard/user-status-counts',

  COUNTRY_LIST: '/registration/api/v1/dropdown/countries',
  CLIENT_LIST_BY_COUNTRY: '/registration/api/v1/client/admin/by-country?',
  CLIENT_LIST: '/registration/api/v1/client/admin/list?',
  CLIENT_DETAILS: '/registration/api/v1/client/admin/:id',
  MSP_LIST: '/registration/api/v1/msp?',
  MSP_DETAILS: '/registration/api/v1/msp/:id',

  // Branding API Endpoints
  GET_BRANDING: '/registration/api/v1/branding',

  REQUIRED_INFO: '/registration/api/v1/required-info',
  UPDATE_BRANDING: '/registration/api/v1/branding',
  ADD_END_USER: '/registration/api/v1/end-user',
  IMPORT_END_USER: '/registration/api/v1/end-user/import?clientAdminId=',
  ASSIGN_SUB_PACKAGE: '/registration/api/v1/end-user/assign-sub-package',
  UNASSIGNED_USER_LIST: '/registration/api/v1/end-user/unassigned?',
  GET_DEPARTMENT_LIST: '/registration/api/v1/departments?',

  SUB_PACKAGE_LIST: '/cms/api/v1/sub-packages?',
  CLIENT_ADMIN_ASSIGN_PRODUCT_LIST:
    '/registration/api/v1/client/admin/products/assigned/:clientAdminId?',
  SUB_PACKAGE_DETAILS: '/cms/api/v1/sub-packages/:id',

  CLIENT_ADMIN_CERTIFICATE_TEMPLATE:
    '/cms/api/v1/certificate-templates/client-admin-template?',
  ASSIGN_CLIENT_CERTIFICATE_TEMPLATE:
    '/cms/api/v1/client-certificate-templates',
  GET_CERTIFICATE_TEMPLATE_DETAILS: '/cms/api/v1/certificate-templates/:id',

  AI_MODELS: '/phishing/api/v1/phishing/ai-models',
};
