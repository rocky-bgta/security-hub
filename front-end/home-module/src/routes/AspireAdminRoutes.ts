export const AspireAdminRoutes = {
  dashboard: {
    title: 'Dashboard',
    key: 'dashboard',
    path: '/',
  },

  // Account Management
  profileInformation: {
    title: 'Profile Information',
    key: 'profile-information',
    path: '/profile-information',
  },
  changePassword: {
    title: 'Change Password',
    key: 'change-password',
    path: '/change-password',
  },
  notificationPreferences: {
    title: 'Notification Preferences',
    key: 'notification-preferences',
    path: '/notification-preferences',
  },

  // Billing Management
  billingHistory: {
    title: 'Billing History',
    key: 'billing-history',
    path: '/billing-history',
  },
  paymentHistory: {
    title: 'Payment History',
    key: 'payment-history',
    path: '/payment-history',
  },
  invoiceHistory: {
    title: 'Invoice History',
    key: 'invoice-history',
    path: '/invoice-history',
  },
  pendingPayment: {
    title: 'Pending Payment',
    key: 'pending-payment',
    path: '/pending-payment',
  },
  billingAnalytics: {
    title: 'Billing Analytics',
    key: 'billing-analytics',
    path: '/billing-analytics',
  },

  // Branding Management
  brandingList: {
    title: 'Branding List',
    key: 'branding-list',
    path: '/branding-list',
  },

  // Certificate Management
  certificateTemplate: {
    title: 'Certificate Template',
    key: 'certificate-template',
    path: '/certificate-template',
  },
  certificateIssued: {
    title: 'Issued Certificate',
    key: 'certificate-issued',
    path: '/certificate-issued',
  },
  certificateAnalytics: {
    title: 'Certificate Analytics & Logs',
    key: 'certificate-analytics',
    path: '/certificate-management/certificate-analytics',
  },

  // Client Management
  clientOnboarding: {
    title: 'Client Onboarding',
    key: 'client-onboarding',
    path: '/client-onboarding',
  },
  clientList: {
    title: 'Client List',
    key: 'client-list',
    path: '/client-list',
  },
  clientPaymentHistory: {
    title: 'Client Payment History',
    key: 'client-payment-history',
    path: '/client-payment-history',
  },
  clientPendingPayments: {
    title: 'Client Pending List',
    key: 'client-pending-list',
    path: '/client-pending-list',
  },
  clientManageLicenses: {
    title: 'Manage Licenses',
    key: 'client-manage-licenses',
    path: '/client-manage-licenses',
  },
  clientSuspend: {
    title: 'Suspend Client',
    key: 'client-suspend',
    path: '/client-suspend',
  },
  clientActivityLogs: {
    title: 'Client Activity Logs',
    key: 'client-activity-logs',
    path: '/client-activity-logs',
  },
  clientReports: {
    title: 'Client Reports',
    key: 'client-reports',
    path: '/client-reports',
  },
  clientAnalytics: {
    title: 'Client Analytics',
    key: 'client-analytics',
    path: '/client-analytics',
  },

  // Content Management
  addContent: {
    title: 'Add Content',
    key: 'add-content',
    path: '/add-content',
  },

  // Department Management
  departmentList: {
    title: 'Department List',
    key: 'department-list',
    path: '/department-list',
  },
  createDepartment: {
    title: 'Create Department',
    key: 'create-department',
    path: '/create-department',
  },

  // Exam Management
  examLibrary: {
    title: 'Exam Library',
    key: 'exam-library',
    path: '/exam-library',
  },
  examList: {
    title: 'Exam List',
    key: 'exam-list',
    path: '/exam-list',
  },
  bulkExamImport: {
    title: 'Bulk Exam Import',
    key: 'bulk-exam-import',
    path: '/bulk-exam-import',
  },
  examSetting: {
    title: 'Exam Setting',
    key: 'exam-setting',
    path: '/exam-setting',
  },
  examineeReports: {
    title: 'Examinee Data & Reports',
    key: 'examinee-reports',
    path: '/examinee-reports',
  },
  examAnalytics: {
    title: 'Exam Analytics',
    key: 'exam-analytics',
    path: '/exam-analytics',
  },

  // Feature Management
  featureList: {
    title: 'Feature List',
    key: 'feature-list',
    path: '/feature-list',
  },

  // Knowledge Hub
  manageResources: {
    title: 'Manage Resources',
    key: 'manage-resources',
    path: '/manage-resources',
  },
  resourceCategories: {
    title: 'Categories & Tags',
    key: 'resource-categories',
    path: '/resource-categories',
  },
  resourceAnalytics: {
    title: 'Resource Analytics',
    key: 'resource-analytics',
    path: '/resource-analytics',
  },

  // Leaderboard Management
  leaderList: {
    title: 'Leader List',
    key: 'leader-message-list',
    path: '/leader-message-list',
  },
  createLeaderboard: {
    title: 'Create Leaderboard',
    key: 'create-leaderboard',
    path: '/create-leader-message',
  },

  // Menu Management
  menuList: {
    title: 'Menu List',
    key: 'menu-list',
    path: '/menu-list',
  },

  // MSP Management
  mspOnboarding: {
    title: 'MSP Onboarding',
    key: 'msp-onboarding',
    path: '/msp-onboarding',
  },
  mspList: {
    title: 'MSP List',
    key: 'msp-list',
    path: '/msp-list',
  },
  mspPaymentHistory: {
    title: 'Payment History',
    key: 'msp-payment-history',
    path: '/msp-payment-history',
  },
  mspPendingPayments: {
    title: 'Pending Payments',
    key: 'msp-pending-payments',
    path: '/msp-pending-payments',
  },
  mspManageLicenses: {
    title: 'Manage Licenses',
    key: 'msp-manage-licenses',
    path: '/msp-manage-licenses',
  },
  mspSuspend: {
    title: 'Suspend MSP',
    key: 'msp-suspend',
    path: '/msp-suspend',
  },
  mspActivityLogs: {
    title: 'Activity Logs',
    key: 'msp-activity-logs',
    path: '/msp-activity-logs',
  },
  mspReports: {
    title: 'MSP Reports',
    key: 'msp-reports',
    path: '/msp-reports',
  },
  mspAnalytics: {
    title: 'MSP Analytics',
    key: 'msp-analytics',
    path: '/msp-analytics',
  },

  // News Management
  manageNews: {
    title: 'Manage News',
    key: 'manage-news',
    path: '/manage-news',
  },
  createNews: {
    title: 'Create News',
    key: 'create-news',
    path: '/create-news',
  },
  newsAnalytics: {
    title: 'News Analytics',
    key: 'news-analytics',
    path: '/news-analytics',
  },
  archivedNews: {
    title: 'Archived News',
    key: 'archived-news',
    path: '/archived-news',
  },

  // Notification Settings
  sendNotification: {
    title: 'Send Notification',
    key: 'send-notification',
    path: '/send-notification',
  },
  manageNotifications: {
    title: 'Manage Notifications',
    key: 'manage-notifications',
    path: '/manage-notifications',
  },
  notificationScheduler: {
    title: 'Notification Scheduler',
    key: 'notification-scheduler',
    path: '/notification-scheduler',
  },
  notificationTemplates: {
    title: 'Templates',
    key: 'notification-templates',
    path: '/notification-templates',
  },
  notificationAnalytics: {
    title: 'Analytics & Reports',
    key: 'notificatioan-analytics',
    path: '/notification-analytics',
  },

  // Package Management
  assignedPackages: {
    title: 'Assigned Packages',
    key: 'assigned-packages',
    path: '/assigned-packages',
  },
  availablePackages: {
    title: 'Available Packages',
    key: 'available-packages',
    path: '/available-packages',
  },
  subPackages: {
    title: 'Sub Packages',
    key: 'sub-packages',
    path: '/sub-packages',
  },
  packageLicenseHistory: {
    title: 'License History',
    key: 'license-history',
    path: '/license-history',
  },
  packageReports: {
    title: 'Performance & Reports',
    key: 'performance-reports',
    path: '/performance-reports',
  },

  // Policy Management
  policyList: {
    title: 'Policy List',
    key: 'policy-list',
    path: '/policy-list',
  },
  requestPolicy: {
    title: 'Request Policy',
    key: 'request-policy',
    path: '/request-policy',
  },

  // Product Management
  productList: {
    title: 'Product Library',
    key: 'product-list',
    path: '/product-list',
  },
  productAnalytics: {
    title: 'Product Analytics',
    key: 'product-analytics',
    path: '/product-analytics',
  },

  // Report Management
  userReport: {
    title: 'User Report',
    key: 'user-report',
    path: '/user-report',
  },
  accessReport: {
    title: 'Role & Access Report',
    key: 'access-report',
    path: '/access-report',
  },
  productReport: {
    title: 'Product & Package Report',
    key: 'product-report',
    path: '/product-report',
  },
  contentReport: {
    title: 'Content Report',
    key: 'content-report',
    path: '/content-report',
  },

  // Role Management
  roleList: {
    title: 'Role List',
    key: 'role-list',
    path: '/role-list',
  },
  permissionList: {
    title: 'Permission Matrix',
    key: 'permission-list',
    path: '/permission-list',
  },

  // Settings
  manageCoupon: {
    title: 'Manage Coupons',
    key: 'manage-coupon',
    path: '/manage-coupon',
  },
  vatConfigurations: {
    title: 'VAT Configurations',
    key: 'vat-configurations',
    path: '/vat-configurations',
  },
  manageCredits: {
    title: 'Manage Credits',
    key: 'manage-credits',
    path: '/manage-credits',
  },
  tierConfigurations: {
    title: 'Tier Configurations',
    key: 'tier-configurations',
    path: '/tier-configurations',
  },
  manageCompliance: {
    title: 'Manage Compliance',
    key: 'manage-compliance',
    path: '/manage-compliance',
  },
  contentTypes: {
    title: 'Content Types',
    key: 'content-types',
    path: '/content-types',
  },
  country: {
    title: 'Country List',
    key: 'country-list',
    path: '/country-list',
  },
  category: {
    title: 'Category List',
    key: 'category-list',
    path: '/category-list',
  },
  industries: {
    title: 'Industries',
    key: 'industries',
    path: '/industries',
  },
  subIndustries: {
    title: 'Sub-Industries',
    key: 'sub-industries',
    path: '/sub-industries',
  },
  organizationSizes: {
    title: 'Organization Sizes',
    key: 'organization-sizes',
    path: '/organization-sizes',
  },
  organizationTypes: {
    title: 'Organization Types',
    key: 'organization-types',
    path: '/organization-types',
  },
  languages: {
    title: 'Languages',
    key: 'languages',
    path: '/languages',
  },
  states: {
    title: 'States',
    key: 'states',
    path: '/states',
  },
  timeZones: {
    title: 'Time Zones',
    key: 'time-zones',
    path: '/time-zones',
  },
  // Support Management
  ticketLibrary: {
    title: 'Ticket Library',
    key: 'ticket-library',
    path: '/ticket-library',
  },
  pendingTickets: {
    title: 'Pending Tickets',
    key: 'pending-tickets',
    path: '/pending-tickets',
  },
  resolvedTickets: {
    title: 'Resolved Tickets',
    key: 'resolved-tickets',
    path: '/resolved-tickets',
  },

  // Survey Management
  surveyList: {
    title: 'Polls & Surveys List',
    key: 'survey-list',
    path: '/survey-list',
  },
  createSurvey: {
    title: 'Create Poll/Survey',
    key: 'create-survey',
    path: '/create-survey',
  },

  // User Management
  userList: {
    title: 'Users List',
    key: 'user-list',
    path: '/user-list',
  },
  userOnboarding: {
    title: 'User Onboarding',
    key: 'user-onboarding',
    path: '/user-onboarding',
  },
  userBulkImport: {
    title: 'Bulk Import',
    key: 'bulk-import-user',
    path: '/bulk-import-user',
  },
  suspendUser: {
    title: 'Suspend User',
    key: 'suspend-user',
    path: '/suspend-user',
  },
  userAnalytics: {
    title: 'User Analytics',
    key: 'user-analytics',
    path: '/user-analytics',
  },
  syncUser: {
    title: 'Sync Users',
    key: 'sync-users',
    path: '/sync-users',
  },
  userActivityLogs: {
    title: 'Users Activity Logs',
    key: 'users-activity-logs',
    path: '/users-activity-logs',
  },
  userReports: {
    title: 'Users Reports',
    key: 'users-reports',
    path: '/users-reports',
  },

  // paymentSuccess: {
  //   title: 'Payment Success',
  //   key: 'payment-success',
  //   path: '/payment-success',
  // },
  // paymentFailed: {
  //   title: 'Payment Failed',
  //   key: 'payment-failed',
  //   path: '/payment-failed',
  // },

  // clientBillingReport: {
  //   title: 'Client Billing Report',
  //   key: 'client-billing-report',
  //   path: '/client-billing-report',
  // },

  // mspPaymentReport: {
  //   title: 'MSP Payment Report',
  //   key: 'msp-payment-report',
  //   path: '/msp-payment-report',
  // },
  // mspPaymentReportDetails: {
  //   title: 'MSP Payment Report Details',
  //   key: 'msp-payment-report-details',
  //   path: '/msp-payment-report/:id',
  // },

  // creditDetails: {
  //   title: 'Credit Details',
  //   key: 'credit-details',
  //   path: '/credit-details/:id',
  // },
  // creditUseHistory: {
  //   title: 'Credit Use History',
  //   key: 'credit-use-history',
  //   path: '/credit-use-history/:id',
  // },
};
