export const routes = {
  dashboard: {
    title: 'Dashboard',
    key: 'dashboard',
    path: '/',
  },
  phishingDashboard: {
    title: 'Phishing Dashboard',
    key: 'phishing-dashboard',
    path: '/phishing-management/dashboard',
  },
  selfOnboard: {
    title: 'Self Onboarding',
    key: 'self-onboarding',
    path: '/self-onboarding',
  },
  buyProduct: {
    title: 'Buy Product',
    key: 'buy-product',
    path: '/buy-product',
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
  requestResetPassword: {
    title: 'Request Reset Password',
    key: 'request-reset-password',
    path: '/auth/request-reset',
  },
  resetPassword: {
    title: 'Reset Password',
    key: 'reset-password',
    path: '/auth/reset-password',
  },
  setPassword: {
    title: 'Set Password',
    key: 'set-password',
    path: '/auth/set-password',
  },
  settings: {
    title: 'Settings',
    key: 'settings',
    path: 'settings',
  },
  profile: {
    title: 'View Profile',
    key: 'profile',
    path: '/profile',
  },
  notFound: {
    title: 'Page Not Found',
    key: 'page-not-found',
    path: '*',
  },

  // Super admin side bar routes
  userManagement: {
    title: 'User Management',
    key: 'users',
    path: '/user-management',
  },
  contentManagement: {
    title: 'Content Management',
    key: 'content-management',
    path: '/content-management',
  },
  taskManagement: {
    title: 'Task Management',
    key: 'task-management',
    path: '/task-management',
  },
  userActivityLogs: {
    title: 'Activity Logs',
    key: '  activity-logs',
    path: '/activity-logs',
  },

  licenseHistory: {
    title: 'License History',
    key: 'license-history',
    path: '/package-management/license-history',
  },
  certificateStatistics: {
    title: 'Certificate History',
    key: 'certificate-history',
    path: '/certificate-management/certificate-history',
  },
  userActivities: {
    title: 'Users Activity Logs',
    key: 'users-activity-logs',
    path: '/user-management/users-activity-logs',
  },
  clientOnboarding: {
    title: 'Client Onboarding',
    key: 'client-onboarding',
    path: '/client-management/client-onboarding',
  },
  mspOnboarding: {
    title: 'MSP Onboarding',
    key: 'msp-onboarding',
    path: '/msp-management/msp-onboarding',
  },
};
