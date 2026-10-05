export const API_END_POINTS = {
  VALIDATE_EMAIL: '/username/exists?email=',
  VALIDATE_EMAIL_TEMPLATE: '/validate-template',
  CREATE_CLIENT_ADMIN: '/onboard',

  UPDATE_PASSWORD: '/auth/api/v1/auth/password/change',
  CLIENT_ADMIN_DETAILS: '/registration/api/v1/client/admin/:clientAdminId',
  UPDATE_CLIENT_ADMIN_INFO: '/registration/api/v1/client/admin/:clientAdminId',

  GET_ORGANIZATION_TYPE_LIST: '/registration/api/v1/metadata/organization-type',
  GET_ACTIVE_COUNTRY_LIST: '/registration/api/v1/dropdown/countries/active',
  GET_ACTIVE_STATE_LIST: '/registration/api/v1/dropdown/states/active',
  GET_ACTIVE_TIME_ZONE_LIST: '/registration/api/v1/dropdown/timezones/active',
  GET_ACTIVE_LANGUAGE_LIST: '/registration/api/v1/dropdown/languages/active',
  GET_ACTIVE_INDUSTRIES_LIST: '/registration/api/v1/dropdown/industries/active',
  GET_ACTIVE_ORGANIZATION_SIZE_LIST:
    '/registration/api/v1/dropdown/organization-sizes/active',

  GET_NOTIFICATION_SETTINGS:
    '/notification/api/v1/admin/notification-settings/dynamic',
  UPDATE_NOTIFICATION_SETTINGS:
    '/notification/api/v1/admin/notification-settings/dynamic/action',

  GET_ROLE_NOTIFICATION_SETTINGS:
    '/notification/api/v1/admin/notification-settings/roles',
  UPDATE_ROLE_NOTIFICATION_SETTINGS:
    '/notification/api/v1/admin/notification-settings/roles/action',

  GET_NOTIFICATION_TEMPLATES:
    '/notification/api/v1/admin/notification-templates',
  UPDATE_NOTIFICATION_TEMPLATES:
    '/notification/api/v1/admin/notification-templates/action',

  GET_USED_NOTIFICATION_TYPES:
    '/notification/api/v1/management/types/used',

  AI_MODELS: '/phishing/api/v1/phishing/ai-models',

  // Client Admin API Endpoints
  GET_CLIENT_ADMIN_NOTIFICATION_SETTINGS:
    '/notification/api/v1/client/notification-settings',
  UPDATE_CLIENT_ADMIN_NOTIFICATION_SETTINGS:
    '/notification/api/v1/client/notification-settings/action',

  // Branding API Endpoints
  GET_BRANDING: '/registration/api/v1/branding',
  UPDATE_BRANDING: '/registration/api/v1/branding',
};
