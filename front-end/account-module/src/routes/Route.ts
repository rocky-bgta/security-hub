export const routes = {
  dashboard: {
    title: 'Dashboard',
    key: 'dashboard',
    path: '/',
  },
  login: {
    title: 'Login',
    key: 'login',
    path: '/auth/login',
  },

  twoFactorAuthSetup: {
    title: 'Two Factor Authentication Setup',
    key: 'two-factor-auth-setup',
    path: '/auth/two-factor-authentication/setup',
  },
  twoFactorAuthVerify: {
    title: 'Two Factor Authentication Verify',
    key: 'two-factor-auth-verify',
    path: '/auth/two-factor-authentication/verify',
  },

  personalInformation: {
    title: 'Personal Information',
    key: 'personal-information',
    path: '/account-management/personal-information',
  },

  brandingManagement: {
    title: 'Branding Management',
    key: 'branding-management',
    path: '/account-management/branding',
  },

  accountSecurity: {
    title: 'Account Security',
    key: 'account-security',
    path: '/account-management/account-security',
  },

  securitySettings: {
    title: 'Security Settings',
    key: 'security-settings',
    path: '/security-settings',
  },

  notificationSettings: {
    title: 'Notification Settings',
    key: 'notification-settings',
    path: '/notification-management/notification-settings',
  },

  roleWiseNotificationSettings: {
    title: 'Role-wise Notification Settings',
    key: 'role-wise-notification-settings',
    path: '/notification-management/role-wise-notification-settings',
  },

  notificationTemplates: {
    title: 'Notification Templates',
    key: 'notification-templates',
    path: '/notification-management/notification-templates',
  },

  editNotificationTemplate: {
    title: 'Edit Notification Template',
    key: 'edit-notification-template',
    path: '/notification-management/notification-templates/edit/:templateId',
  },

  userAccountSettings: {
    title: 'User Account Settings',
    key: 'user-account-settings',
    path: '/user-account-settings',
  },

  globalSettings: {
    title: 'Global Settings',
    key: 'global-settings',
    path: '/global-settings',
  },

  notFound: {
    title: 'Page Not Found',
    key: 'page-not-found',
    path: '*',
  },
};
