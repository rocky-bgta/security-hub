export const AspireAdminRoutes = {
  profileInformation: {
    title: 'Profile Information',
    key: 'profile-information',
    path: '/account-management/profile-information',
  },
  changePassword: {
    title: 'Change Password',
    key: 'change-password',
    path: '/account-management/change-password',
  },
  notificationPreferences: {
    title: 'Notification Preferences',
    key: 'notification-preferences',
    path: '/account-management/notification-preferences',
  },

  billingHistory: {
    title: 'Billing History',
    key: 'billing-history',
    path: '/billing-management/billing-history',
  },
  paymentHistory: {
    title: 'Payment History',
    key: 'payment-history',
    path: '/billing-management/payment-history',
  },
  invoiceHistory: {
    title: 'Invoice History',
    key: 'invoice-history',
    path: '/billing-management/invoice-history',
  },
  pendingPayment: {
    title: 'Pending Payment',
    key: 'pending-payment',
    path: '/billing-management/pending-payment',
  },
  billingAnalytics: {
    title: 'Billing Analytics',
    key: 'billing-analytics',
    path: '/billing-management/billing-analytics',
  },

  brandingList: {
    title: 'Branding List',
    key: 'branding-list',
    path: '/branding-management/branding-list',
  },

  certificateTemplate: {
    title: 'Certificate Template',
    key: 'certificate-template',
    path: '/certificate-management/certificate-template',
  },
  certificateIssued: {
    title: 'Issued Certificate',
    key: 'certificate-issued',
    path: '/certificate-management/certificate-issued',
  },
  certificateAnalytics: {
    title: 'Certificate Analytics',
    key: 'certificate-analytics',
    path: '/certificate-management/certificate-analytics',
  },

  clientOnboarding: {
    title: 'Client Onboarding',
    key: 'client-onboarding',
    path: '/client-management/client-onboarding',
  },
  clientEdit: {
    title: 'Client Edit',
    key: 'client-edit',
    path: '/client-management/client/:id/edit',
  },
  clientList: {
    title: 'Client List',
    key: 'client-list',
    path: '/client-management/client-list',
  },
  clientUserList: {
    title: 'Manage Users',
    key: 'client-user-list',
    path: '/client-management/client/:id/users',
  },
  clientPaymentHistory: {
    title: 'Payment History',
    key: 'client-payment-history',
    path: '/client-management/payment-history',
  },
  clientPendingPayments: {
    title: 'Pending Payments',
    key: 'client-pending-payments',
    path: '/client-management/pending-payments',
  },
  clientManageLicenses: {
    title: 'Manage Licenses',
    key: 'client-manage-licenses',
    path: '/client-management/client-manage-licenses',
  },
  clientSuspend: {
    title: 'Client Suspend',
    key: 'client-suspend',
    path: '/client-management/client-suspend',
  },
  clientActivityLogs: {
    title: 'Activity Logs',
    key: 'client-activity-logs',
    path: '/client-management/client-activity-logs',
  },
  clientReports: {
    title: 'Client Reports',
    key: 'client-reports',
    path: '/client-management/client-reports',
  },
  clientAnalytics: {
    title: 'Client Analytics',
    key: 'client-analytics',
    path: '/client-management/client-analytics',
  },

  addContent: {
    title: 'Add Content',
    key: 'add-content',
    path: '/content-management/add-content',
  },

  departmentList: {
    title: 'Department List',
    key: 'department-list',
    path: '/department-management/department-list',
  },
  createDepartment: {
    title: 'Create Department',
    key: 'create-department',
    path: '/department-management/create-department',
  },

  examLibrary: {
    title: 'Exam Library',
    key: 'exam-library',
    path: '/exam-management/exam-library',
  },
  examList: {
    title: 'Exam List',
    key: 'exam-list',
    path: '/exam-management/exam-list',
  },
  bulkExamImport: {
    title: 'Bulk Exam Import',
    key: 'bulk-exam-import',
    path: '/exam-management/bulk-exam-import',
  },
  examSetting: {
    title: 'Exam Setting',
    key: 'exam-setting',
    path: '/exam-management/exam-setting',
  },
  examineeReports: {
    title: 'Examinee Data & Reports',
    key: 'examinee-reports',
    path: '/exam-management/examinee-reports',
  },
  examAnalytics: {
    title: 'Exam Analytics',
    key: 'exam-analytics',
    path: '/exam-management/exam-analytics',
  },

  featureList: {
    title: 'Feature List',
    key: 'feature-list',
    path: '/feature-management/feature-list',
  },

  manageResources: {
    title: 'Manage Resources',
    key: 'manage-resources',
    path: '/knowledge-management/manage-resources',
  },
  resourceCategories: {
    title: 'Resource Categories',
    key: 'resource-categories',
    path: '/knowledge-management/resource-categories',
  },
  resourceAnalytics: {
    title: 'Resource Analytics',
    key: 'resource-analytics',
    path: '/knowledge-management/resource-analytics',
  },

  leaderList: {
    title: 'Leader List',
    key: 'leader-message-list',
    path: '/leader-message-management/leader-message-list',
  },
  createLeaderboard: {
    title: 'Create Leaderboard',
    key: 'create-leaderboard',
    path: '/leader-message-management/create-leader-message',
  },

  menuList: {
    title: 'Menu List',
    key: 'menu-list',
    path: '/menu-management/menu-list',
  },

  mspOnboarding: {
    title: 'MSP Onboarding',
    key: 'msp-onboarding',
    path: '/msp-management/msp-onboarding',
  },
  mspList: {
    title: 'MSP List',
    key: 'msp-list',
    path: '/msp-management/msp-list',
  },
  mspPaymentHistory: {
    title: 'Payment History',
    key: 'msp-payment-history',
    path: '/msp-management/msp-payment-history',
  },
  mspPendingPayments: {
    title: 'Pending Payments',
    key: 'msp-pending-payments',
    path: '/msp-management/msp-pending-payments',
  },
  mspManageLicenses: {
    title: 'Manage Licenses',
    key: 'manage-licenses',
    path: '/msp-management/msp-manage-licenses',
  },
  mspSuspend: {
    title: 'Suspend MSP',
    key: 'suspend-msp',
    path: '/msp-management/msp-suspend',
  },
  mspActivityLogs: {
    title: 'Activity Logs',
    key: 'msp-activity-logs',
    path: '/msp-management/msp-activity-logs',
  },
  mspReports: {
    title: 'MSP Reports',
    key: 'msp-reports',
    path: '/msp-management/msp-reports',
  },
  mspAnalytics: {
    title: 'MSP Analytics',
    key: 'msp-analytics',
    path: '/msp-management/msp-analytics',
  },

  manageNews: {
    title: 'Manage News',
    key: 'manage-news',
    path: '/news-management/manage-news',
  },
  createNews: {
    title: 'Create News Post',
    key: 'create-news',
    path: '/news-management/create-news',
  },
  newAnalytics: {
    title: 'News Analytics',
    key: 'news-analytics',
    path: '/news-management/news-analytics',
  },
  archivedNews: {
    title: 'Archived News',
    key: 'archived-news',
    path: '/news-management/archived-news',
  },

  sendNotification: {
    title: 'Send Notification',
    key: 'send-notification',
    path: '/notification-management/send-notification',
  },
  manageNotifications: {
    title: 'Manage Notifications',
    key: 'manage-notifications',
    path: '/notification-management/manage-notifications',
  },
  notificationScheduler: {
    title: 'Notification Scheduler',
    key: 'notification-scheduler',
    path: '/notification-management/notification-scheduler',
  },
  notificationTemplates: {
    title: 'Templates',
    key: 'notification-templates',
    path: '/notification-templates',
  },
  notificationAnalytics: {
    title: 'Notification Analytics & Reports',
    key: 'notification-analytics',
    path: '/notification-management/notification-analytics',
  },

  assignedPackages: {
    title: 'Assigned Packages',
    key: 'assigned-packages',
    path: '/package-management/assigned-packages',
  },
  availablePackages: {
    title: 'Available Packages',
    key: 'available-packages',
    path: '/package-management/available-packages',
  },
  packageLicenseHistory: {
    title: 'License History',
    key: 'license-history',
    path: '/package-management/license-history',
  },
  packageReports: {
    title: 'Performance & Reports',
    key: 'performance-reports',
    path: '/package-management/performance-reports',
  },

  policyList: {
    title: 'Policy List',
    key: 'policy-list',
    path: '/policy-management/policy-list',
  },
  requestPolicy: {
    title: 'Request Policy',
    key: 'request-policy',
    path: '/policy-management/request-policy',
  },

  productList: {
    title: 'Product List',
    key: 'product-list',
    path: '/product-management/product-list',
  },
  productAnalytics: {
    title: 'Product Analytics',
    key: 'product-analytics',
    path: '/product-management/product-analytics',
  },

  userReport: {
    title: 'User Report',
    key: 'user-report',
    path: '/report-management/user-reports',
  },
  supportTicketReport: {
    title: 'Support Ticket Report',
    key: 'support-ticket-report',
    path: '/report-management/support-ticket-reports',
  },
  billingPaymentReport: {
    title: 'Billing and Payment Report',
    key: 'billing-payment-report',
    path: '/report-management/billing-payment-reports',
  },
  accessReport: {
    title: 'Role and Access Report',
    key: 'access-report',
    path: '/report-management/access-report',
  },
  productReport: {
    title: 'Product and Package Report',
    key: 'product-report',
    path: '/report-management/product-report',
  },
  contentReport: {
    title: 'Content Report',
    key: 'content-report',
    path: '/report-management/content-report',
  },
  certificateReport: {
    title: 'Certificate Report',
    key: 'certificate-report',
    path: '/report-management/certificate-report',
  },

  roleList: {
    title: 'Role List',
    key: 'role-list',
    path: '/role-management/role-list',
  },
  permissionList: {
    title: 'Permission Matrix',
    key: 'permission-list',
    path: '/role-management/permission-list',
  },

  manageCoupon: {
    title: 'Manage Coupons',
    key: 'manage-coupon',
    path: '/settings/manage-coupon',
  },
  vatConfigurations: {
    title: 'VAT Configurations',
    key: 'vat-configurations',
    path: '/settings/vat-configurations',
  },
  manageCredits: {
    title: 'Manage Credits',
    key: 'manage-credits',
    path: '/settings/manage-credits',
  },
  tierConfigurations: {
    title: 'Tier Configurations',
    key: 'tier-configurations',
    path: '/settings/tier-configurations',
  },
  manageCompliance: {
    title: 'Manage Compliance',
    key: 'manage-compliance',
    path: '/settings/manage-compliance',
  },
  contentTypes: {
    title: 'Content Types',
    key: 'content-types',
    path: '/settings/content-types',
  },
  tags: {
    title: 'Product Tags',
    key: 'tags',
    path: '/settings/tags',
  },
  country: {
    title: 'Country List',
    key: 'country-list',
    path: '/settings/country-list',
  },
  category: {
    title: 'Category List',
    key: 'category-list',
    path: '/settings/category-list',
  },
  industries: {
    title: 'Industries',
    key: 'industries',
    path: '/settings/industries',
  },
  subIndustries: {
    title: 'Sub-Industries',
    key: 'sub-industries',
    path: '/settings/sub-industries',
  },
  organizationSizes: {
    title: 'Organization Sizes',
    key: 'organization-sizes',
    path: '/settings/organization-sizes',
  },
  organizationTypes: {
    title: 'Organization Types',
    key: 'organization-types',
    path: '/settings/organization-types',
  },
  timeZones: {
    title: 'Time Zones',
    key: 'time-zones',
    path: '/settings/time-zones',
  },
  states: {
    title: 'States',
    key: 'states',
    path: '/settings/states',
  },
  languages: {
    title: 'Languages',
    key: 'languages',
    path: '/settings/languages',
  },
  ticketLibrary: {
    title: 'Ticket Library',
    key: 'ticket-library',
    path: '/support-management/ticket-library',
  },
  pendingTickets: {
    title: 'Pending Tickets',
    key: 'pending-tickets',
    path: '/support-management/pending-tickets',
  },
  resolvedTickets: {
    title: 'Resolved Tickets',
    key: 'resolved-tickets',
    path: '/support-management/resolved-tickets',
  },

  surveyList: {
    title: 'Polls & Surveys List',
    key: 'survey-list',
    path: '/survey-management/survey-list',
  },
  createSurvey: {
    title: 'Create Survey',
    key: 'create-survey',
    path: '/survey-management/create-survey',
  },

  userList: {
    title: 'Client User List',
    key: 'user-list',
    path: '/user-management/user-list',
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
  suspendUser: {
    title: 'Suspend User',
    key: 'suspend-user',
    path: '/user-management/suspend-user',
  },
  userAnalytics: {
    title: 'Client User Analytics',
    key: 'user-analytics',
    path: '/user-management/user-analytics',
  },
  syncUser: {
    title: 'Sync Users',
    key: 'sync-user',
    path: '/user-management/sync-user',
  },
  userActivityLogs: {
    title: 'Users Activity Logs',
    key: 'users-activity-logs',
    path: '/user-management/users-activity-logs',
  },
  userReports: {
    title: 'Client User Reports',
    key: 'user-reports',
    path: '/user-management/user-reports',
  },
};
