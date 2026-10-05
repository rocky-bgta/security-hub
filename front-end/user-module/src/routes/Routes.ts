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
  users: {
    title: 'Users',
    key: 'users',
    path: '/user-management/user-list',
  },
  notFound: {
    title: 'Page Not Found',
    key: 'page-not-found',
    path: '*',
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

  systemUsers: {
    title: 'System Users',
    key: 'system-users',
    path: '/user-management/system-users',
  },

  // Aspire admin routes
  clientUserList: {
    title: 'Users',
    key: 'client-user-list',
    path: '/client-management/client/:id/users',
  },
  userOnboarding: {
    title: 'User Onboarding',
    key: 'user-onboarding',
    path: '/user-management/user-onboarding',
  },
  userBulkImport: {
    title: 'Bulk Import',
    key: 'bulk-import-user',
    path: '/user-management/bulk-import-user',
  },
  syncUser: {
    title: 'Sync Users',
    key: 'sync-users',
    path: '/user-management/sync-users',
  },
  userReport: {
    title: 'User Report',
    key: 'user-report',
    path: '/user-management/user-report',
  },
  userAnalytics: {
    title: 'User Analytics',
    key: 'user-analytics',
    path: '/user-management/user-analytics',
  },
  suspendUser: {
    title: 'Suspend User',
    key: 'suspend-user',
    path: '/user-management/suspend-user',
  },

  mspAnalytics: {
    title: 'MSP Analytics',
    key: 'msp-analytics',
    path: '/msp-management/msp-analytics',
  },
  mspEdit: {
    title: 'MSP Edit',
    key: 'msp-edit',
    path: '/msp-management/msp-edit/:id',
  },
  mspLicense: {
    title: 'MSP License',
    key: 'msp-license',
    path: '/msp-management/msp-license',
  },
  mspLicenseAllocation: {
    title: 'MSP License Allocation',
    key: 'msp-license-allocation',
    path: '/msp-management/msp-license-allocation',
  },
  mspList: {
    title: 'MSP List',
    key: 'msp-list',
    path: '/msp-management/msp-list',
  },
  mspManageLicenses: {
    title: 'Manage Licenses',
    key: 'manage-licenses',
    path: '/msp-management/manage-licenses',
  },
  mspOnboarding: {
    title: 'MSP Onboarding',
    key: 'msp-onboarding',
    path: '/msp-management/msp-onboarding',
  },
  mspProfile: {
    title: 'MSP Profile',
    key: 'msp-profile',
    path: '/msp-management/msp-profile',
  },
  mspReports: {
    title: 'MSP Reports',
    key: 'msp-reports',
    path: '/msp-management/msp-reports',
  },
  mspSuspend: {
    title: 'Suspend MSP ',
    key: 'suspend-msp',
    path: '/msp-management/msp-suspend',
  },

  // msp admin routes
  mspActivityLogs: {
    title: 'MSP Activity Logs',
    key: 'msp-activity-logs',
    path: '/msp-management/msp-activity-logs',
  },

  // client admin routes
  userActivityLogs: {
    title: 'Users Activity Logs',
    key: 'users-activity-logs',
    path: '/user-management/users-activity-logs',
  },

  clientActivityLogs: {
    title: 'Client Activity Logs',
    key: 'client-activity-logs',
    path: '/client-management/client-activity-logs',
  },
  activityLogs: {
    title: 'Activity Logs',
    key: 'activity-logs',
    path: '/activity-logs',
  },
  clientAnalytics: {
    title: 'Client Analytics',
    key: 'client-analytics',
    path: '/client-management/client-analytics',
  },
  clientEdit: {
    title: 'Client Edit',
    key: 'client-edit',
    path: '/client-management/client/:id/edit',
  },
  clientLicense: {
    title: 'Client License',
    key: 'client-license',
    path: '/client-management/client-license',
  },
  clientLicenseAllocation: {
    title: 'Client License Allocation',
    key: 'client-license-allocation',
    path: '/client-management/client-license-allocation',
  },
  clientList: {
    title: 'Client List',
    key: 'client-list',
    path: '/client-management/client-list',
  },
  clientManageLicenses: {
    title: 'Manage Licenses',
    key: 'client-manage-licenses',
    path: '/client-management/client-manage-licenses',
  },
  clientOnboarding: {
    title: 'Client Onboarding',
    key: 'client-onboarding',
    path: '/client-management/client-onboarding',
  },
  clientProfile: {
    title: 'Client Profile',
    key: 'client-profile',
    path: '/client-profile',
  },
  clientReports: {
    title: 'Client Reports',
    key: 'client-reports',
    path: '/client-management/client-reports',
  },
  clientSuspend: {
    title: 'Client Suspend',
    key: 'client-suspend',
    path: '/client-management/client-suspend',
  },
};
