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

  // Client Admin Routes
  clientUser: {
    title: 'Users',
    key: 'users',
    path: '/users',
  },

  // mixed routes
  packageList: {
    title: 'Package List',
    key: 'package-list',
    path: '/package-list',
  },
  courseList: {
    title: 'Course List',
    key: 'course-list',
    path: '/course-list',
  },

  // super admin exclusive routes
  courseChapters: {
    title: 'Course Chapters',
    key: 'chapter-list',
    path: '/course/:slug/chapters',
  },

  // client user exclusive routes
  courseDetails: {
    title: 'Course Details',
    key: 'course-details',
    path: '/course/:packageId/:slug',
  },
  courseComplete: {
    title: 'Course Complete',
    key: 'course-complete',
    path: '/course/:slug/complete',
  },
  contentDetails: {
    title: 'Content Details',
    key: 'content-details',
    path: '/course/:packageId/:slug/:contentId',
  },
  campaigns: {
    title: 'Campaigns',
    key: 'campaigns',
    path: '/campaigns',
  },
  bookmarks: {
    title: 'Bookmarks',
    key: 'bookmarks',
    path: '/bookmarks',
  },
  certificates: {
    title: 'Certifications',
    key: 'certificates',
    path: '/certifications',
  },
  account: {
    title: 'Account',
    key: 'account',
    path: '/account',
  },
  accountProfile: {
    title: 'Account Profile',
    key: 'account-profile',
    path: '/account/profile',
  },
  accountProfileSettings: {
    title: 'Account Profile Settings',
    key: 'account-profile-settings',
    path: '/account/profile-setting',
  },
  accountUserSecurity: {
    title: 'Account Security',
    key: 'account-security',
    path: '/account/security',
  },
  phishingList: {
    title: 'Phishing Report',
    key: 'phishing-report',
    path: '/phishing-list',
  },
  paymentSuccess: {
    title: 'Payment Success',
    key: 'payment-success',
    path: '/payment-success',
  },
  paymentFailed: {
    title: 'Payment Failed',
    key: 'payment-failed',
    path: '/payment-failed',
  },
  clientBillingReport: {
    title: 'Client Billing Report',
    key: 'client-billing-report',
    path: '/client-billing-report',
  },
  mspPaymentReport: {
    title: 'MSP Payment Report',
    key: 'msp-payment-report',
    path: '/msp-payment-report',
  },
  mspPaymentReportDetails: {
    title: 'MSP Payment Report Details',
    key: 'msp-payment-report-details',
    path: '/msp-payment-report/:id',
  },
  creditDetails: {
    title: 'Credit Details',
    key: 'credit-details',
    path: '/credit-details/:id',
  },
  creditUseHistory: {
    title: 'Credit Use History',
    key: 'credit-use-history',
    path: '/credit-use-history/:id',
  },
  topicList: {
    title: 'Topic List',
    key: 'topic-list',
    path: '/topic-list',
  },

  // Account Management
  accountManagement: {
    title: 'Account Management',
    key: 'account-management',
    path: '/account-management/*',
  },

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

  personalInformation: {
    title: 'Personal Information',
    key: 'personal-information',
    path: '/personal-information',
  },
  securitySettings: {
    title: 'Security Settings',
    key: 'security-settings',
    path: '/security-settings',
  },
  notificationSettings: {
    title: 'Notification Settings',
    key: 'notification-settings',
    path: '/notification-settings',
  },
  roleWiseNotificationSettings: {
    title: 'Role-wise Notification Settings',
    key: 'role-wise-notification-settings',
    path: '/role-wise-notification-settings',
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
  accountSecurity: {
    title: 'Account Security',
    key: 'account-security',
    path: '/account-security',
  },
  branding: {
    title: 'Branding Management',
    key: 'branding',
    path: '/branding',
  },

  // Billing Management
  billingManagement: {
    title: 'Billing Management',
    key: 'billing-management',
    path: '/billing-management/*',
  },

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
  paymentReport: {
    title: 'Payment Report',
    key: 'payment-report',
    path: '/payment-report',
  },
  billingAnalytics: {
    title: 'Billing Analytics',
    key: 'billing-analytics',
    path: '/billing-analytics',
  },

  // Branding Management
  brandingManagement: {
    title: 'Branding Management',
    key: 'branding-management',
    path: '/branding-management/*',
  },

  brandingList: {
    title: 'Branding List',
    key: 'branding-list',
    path: '/branding-list',
  },

  // Certificate Management
  certificateManagement: {
    title: 'Certificate Management',
    key: 'certificate-management',
    path: '/certificate-management/*',
  },

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
    path: '/certificate-analytics',
  },

  certificateHistory: {
    title: 'Certificate History',
    key: 'certificate-history',
    path: '/certificate-history',
  },

  // Client Management
  clientManagement: {
    title: 'Client Management',
    key: 'client-management',
    path: '/client-management/*',
  },

  clientOnboarding: {
    title: 'Client Onboarding',
    key: 'client-onboarding',
    path: '/client-onboarding',
  },
  clientEdit: {
    title: 'Client Edit',
    key: 'client-edit',
    path: '/client/:id/edit',
  },
  clientList: {
    title: 'Client List',
    key: 'client-list',
    path: '/client-list',
  },
  clientUserList: {
    title: 'Manage Users',
    key: 'client-user-list',
    path: '/client/:id/users',
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
  clientLicenseHistory: {
    title: 'License History',
    key: 'client-license-history',
    path: '/client-management/client-license-history',
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
  activityLogs: {
    title: 'Activity Logs',
    key: 'activity-logs',
    path: '/activity-logs',
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
  contentManagement: {
    title: 'Content Management',
    key: 'content-management',
    path: '/content-management/*',
  },

  addContent: {
    title: 'Add Content',
    key: 'add-content',
    path: '/add-content',
  },

  // Department Management
  departmentManagement: {
    title: 'Department Management',
    key: 'department-management',
    path: '/department-management/*',
  },

  departmentList: {
    title: 'Departments',
    key: 'departments',
    path: '/departments',
  },
  createDepartment: {
    title: 'Create Department',
    key: 'create-department',
    path: '/create-department',
  },

  // Exam Management
  examManagement: {
    title: 'Exam Management',
    key: 'exam-management',
    path: '/exam-management/*',
  },

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

  exam: {
    title: 'Exam',
    key: 'exam',
    path: '/exam/:slug',
  },
  examResult: {
    title: 'Exam Result',
    key: 'exam-result',
    path: '/exam/:slug/result',
  },

  // Feature Management
  featureManagement: {
    title: 'Feature Management',
    key: 'feature-management',
    path: '/feature-management/*',
  },

  featureList: {
    title: 'Feature List',
    key: 'feature-list',
    path: '/feature-list',
  },

  // Knowledge Hub
  knowledgeHubManagement: {
    title: 'Knowledge Management',
    key: 'knowledge-management',
    path: '/knowledge-management/*',
  },

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

  knowledgeHub: {
    title: 'Knowledge Hub',
    key: 'knowledge-hub',
    path: '/knowledge-hub',
  },

  // Leaderboard Management
  leaderboardManagement: {
    title: 'Leaderboard Management',
    key: 'leaderboard-management',
    path: '/leader-message-management/*',
  },

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

  leaderboard: {
    title: 'Leaderboard',
    key: 'leaderboard',
    path: '/leaderboard',
  },

  // Menu Management
  menuManagement: {
    title: 'Menu Management',
    key: 'menu-management',
    path: '/menu-management/*',
  },

  menuList: {
    title: 'Menu List',
    key: 'menu-list',
    path: '/menu-list',
  },

  // MSP Management
  mspManagement: {
    title: 'MSP Management',
    key: 'msp-management',
    path: '/msp-management/*',
  },

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
  mspEdit: {
    title: 'MSP Edit',
    key: 'msp-edit',
    path: '/msp-edit/:id',
  },
  mspProfile: {
    title: 'MSP Profile',
    key: 'msp-profile',
    path: '/msp-profile',
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
  mspLicenseHistory: {
    title: 'License History',
    key: 'msp-license-history',
    path: '/msp-management/msp-license-history',
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
  newsManagement: {
    title: 'News Management',
    key: 'news-management',
    path: '/news-management/*',
  },

  latestNews: {
    title: 'Latest News',
    key: 'latest-news',
    path: '/latest-news',
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
  notificationManagement: {
    title: 'Notification Management',
    key: 'notification-management',
    path: '/notification-management/*',
  },

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
  editNotificationTemplate: {
    title: 'Edit Notification Template',
    key: 'edit-notification-template',
    path: '/notification-templates/edit/:templateId',
  },
  notificationAnalytics: {
    title: 'Analytics & Reports',
    key: 'notificatioan-analytics',
    path: '/notification-analytics',
  },

  notification: {
    title: 'Notifications',
    key: 'notifications',
    path: '/notifications',
  },

  // Package Management
  packageManagement: {
    title: 'Package Management',
    key: 'package-management',
    path: '/package-management/*',
  },

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

  addPackage: {
    title: 'Add Package',
    key: 'add-package',
    path: '/add-package',
  },

  // License Management
  licenseManagement: {
    title: 'License Management',
    key: 'license-management',
    path: '/license-management/*',
  },
  licenseOverview: {
    title: 'License Overview',
    key: 'license-overview',
    path: '/license-overview',
  },
  licenseAssignment: {
    title: 'License Assignment',
    key: 'license-assignment',
    path: '/license-assignment',
  },

  // Phishing Management
  phishingManagement: {
    title: 'Phishing Management',
    key: 'phishing-management',
    path: '/phishing-management/*',
  },
  phishingDashboard: {
    title: 'Phishing Dashboard',
    key: 'phishing-dashboard',
    path: '/dashboard',
  },
  phishingDomainManagement: {
    title: 'Phishing Domain Management',
    key: 'phishing-domain-management',
    path: '/domain-management',
  },
  phishingCampaings: {
    title: 'Phishing Campaigns',
    key: 'phishing-campaigns',
    path: '/campaigns',
  },
  phishingCampaignCreate: {
    title: 'Create Phishing Campaign',
    key: 'create-phishing-campaign',
    path: '/create-campaign',
  },
  phishingCampaignEdit: {
    title: 'Edit Phishing Campaign',
    key: 'phishing-campaign-edit',
    path: '/edit-campaign/:id',
  },
  phishingCampaignDetails: {
    title: 'Phishing Campaign Details',
    key: 'phishing-campaign-details',
    path: '/campaigns/:id',
  },
  phishingTemplateLibrary: {
    title: 'Phishing Template Library',
    key: 'phishing-template-library',
    path: '/template-library',
  },
  phishingTemplateLibraryCreate: {
    title: 'Create Phishing Template',
    key: 'phishing-template-library-create',
    path: '/create-template',
  },
  phishingTemplateLibraryEdit: {
    title: 'Edit Phishing Template',
    key: 'phishing-template-library-edit',
    path: '/edit-template/:id',
  },
  phishingLandingPages: {
    title: 'Phishing Landing Pages',
    key: 'phishing-landing-pages',
    path: '/landing-pages',
  },
  phishingLandingPageCreate: {
    title: 'Create Phishing Landing Page',
    key: 'phishing-landing-page-create',
    path: '/create-landing-page',
  },
  phishingLandingPageEdit: {
    title: 'Edit Phishing Landing Page',
    key: 'phishing-landing-page-edit',
    path: '/edit-landing-page/:id',
  },
  phishingSenderProfiles: {
    title: 'Phishing Sender Profiles',
    key: 'phishing-sender-profiles',
    path: '/sender-profiles',
  },
  phishingCampaignRiskImpacts: {
    title: 'Phishing Campaign Risk Impacts',
    key: 'phishing-campaign-risk-impacts',
    path: '/campaign-risk-impacts',
  },
  phishingCampaignReports: {
    title: 'Phishing Campaign Reports',
    key: 'phishing-campaign-reports',
    path: '/campaign-reports',
  },
  phishingCampaignDetailReport: {
    title: 'Phishing Campaign Detail Report',
    key: 'phishing-campaign-detail-report',
    path: '/campaign-reports/:id',
  },
  phishingUserRiskReport: {
    title: 'Phishing User Risk Report',
    key: 'phishing-user-risk-report',
    path: '/user-risk-report',
  },
  phishingCourseStatisticsDetails: {
    title: 'Phishing Course Statistics Details',
    key: 'phishing-course-statistics-details',
    path: '/course-statistics-details',
  },
  phishingReports: {
    title: 'Phishing Reports',
    key: 'phishing-reports',
    path: '/reports',
  },
  phishingRecentActivity: {
    title: 'Recent Activity',
    key: 'recent-activity',
    path: '/recent-activity',
  },

  // Smishing Management
  smishingManagement: {
    title: 'Smishing Management',
    key: 'smishing-management',
    path: '/smishing-management/*',
  },
  smishingDashboard: {
    title: 'Smishing Dashboard',
    key: 'smishing-dashboard',
    path: '/dashboard',
  },
  smishingCampaigns: {
    title: 'Smishing Campaigns',
    key: 'smishing-campaigns',
    path: '/campaigns',
  },
  smishingCampaignDetails: {
    title: 'Smishing Campaign Details',
    key: 'smishing-campaign-details',
    path: '/campaigns/:id',
  },
  smishingSimulationCreate: {
    title: 'Create Smishing Simulation',
    key: 'create-smishing-simulation',
    path: '/create-simulation',
  },
  smishingSimulationEdit: {
    title: 'Edit Smishing Simulation',
    key: 'edit-smishing-simulation',
    path: '/edit-simulation/:id',
  },
  smsTemplateLibrary: {
    title: 'SMS Template Library',
    key: 'sms-template-library',
    path: '/sms-template-library',
  },
  smsTemplateLibraryCreate: {
    title: 'Create SMS Template',
    key: 'sms-template-library-create',
    path: '/create-sms-template',
  },
  smsTemplateLibraryEdit: {
    title: 'Edit SMS Template',
    key: 'sms-template-library-edit',
    path: '/edit-sms-template/:id',
  },
  smsServerConfigurations: {
    title: 'SMS Server Configurations',
    key: 'sms-server-configurations',
    path: '/sms-server-configurations',
  },
  smishingLandingPages: {
    title: 'Smishing Landing Pages',
    key: 'smishing-landing-pages',
    path: '/landing-pages',
  },
  smishingLandingPageCreate: {
    title: 'Create Smishing Landing Page',
    key: 'smishing-landing-page-create',
    path: '/create-landing-page',
  },
  smishingLandingPageEdit: {
    title: 'Edit Smishing Landing Page',
    key: 'smishing-landing-page-edit',
    path: '/edit-landing-page/:id',
  },
  smishingReports: {
    title: 'Smishing Reports',
    key: 'smishing-reports',
    path: '/reports',
  },
  smishingCampaignRiskImpacts: {
    title: 'Smishing Campaign Risk Impacts',
    key: 'smishing-campaign-risk-impacts',
    path: '/campaign-risk-impacts',
  },
  smishingCourseStatisticsDetails: {
    title: 'Smishing Course Statistics Details',
    key: 'smishing-course-statistics-details',
    path: '/course-statistics-details',
  },
  smishingCampaignReports: {
    title: 'Smishing Campaign Reports',
    key: 'smishing-campaign-reports',
    path: '/campaign-reports',
  },
  smishingCampaignDetailReport: {
    title: 'Smishing Campaign Detail Report',
    key: 'smishing-campaign-detail-report',
    path: '/campaign-reports/:id',
  },
  smishingUserRiskReport: {
    title: 'Smishing User Risk Report',
    key: 'smishing-user-risk-report',
    path: '/user-risk-report',
  },
  smishingRecentActivity: {
    title: 'Smishing Recent Activity',
    key: 'smishing-recent-activity',
    path: '/recent-activity',
  },

  // Vishing Management
  vishingManagement: {
    title: 'Vishing Management',
    key: 'vishing-management',
    path: '/vishing-management/*',
  },
  vishingDashboard: {
    title: 'Vishing Dashboard',
    key: 'vishing-dashboard',
    path: '/dashboard',
  },
  vishingCampaigns: {
    title: 'Vishing Campaigns',
    key: 'vishing-campaigns',
    path: '/campaigns',
  },
  vishingCampaignRiskImpacts: {
    title: 'Vishing Campaign Risk Impacts',
    key: 'vishing-campaign-risk-impacts',
    path: '/campaign-risk-impacts',
  },
  vishingCampaignDetails: {
    title: 'Vishing Campaign Details',
    key: 'vishing-campaign-details',
    path: '/campaigns/:id',
  },
  vishingSimulationCreate: {
    title: 'Create Vishing Simulation',
    key: 'create-vishing-simulation',
    path: '/create-simulation',
  },
  vishingSimulationEdit: {
    title: 'Edit Vishing Simulation',
    key: 'edit-vishing-simulation',
    path: '/edit-simulation/:id',
  },
  vishingTemplateLibrary: {
    title: 'Vishing Template Library',
    key: 'vishing-template-library',
    path: '/template-library',
  },
  vishingAttackTemplates: {
    title: 'Vishing Attack Templates',
    key: 'vishing-attack-templates',
    path: '/attack-templates',
  },
  vishingReports: {
    title: 'Vishing Reports',
    key: 'vishing-reports',
    path: '/reports',
  },
  voiceServerConfiguration: {
    title: 'Voice Server Configuration',
    key: 'voice-server-configuration',
    path: '/voice-server-configuration',
  },
  vishingProviderConfiguration: {
    title: 'Vishing Provider Configuration',
    key: 'vishing-provider-configuration',
    path: '/provider-configuration',
  },
  vishingCourseStatisticsDetails: {
    title: 'Vishing Course Statistics Details',
    key: 'vishing-course-statistics-details',
    path: '/course-statistics-details',
  },
  vishingCampaignReports: {
    title: 'Vishing Campaign Reports',
    key: 'vishing-campaign-reports',
    path: '/campaign-reports',
  },
  vishingCampaignDetailReport: {
    title: 'Vishing Campaign Detail Report',
    key: 'vishing-campaign-detail-report',
    path: '/campaign-reports/:id',
  },
  vishingUserRiskReport: {
    title: 'Vishing User Risk Report',
    key: 'vishing-user-risk-report',
    path: '/user-risk-report',
  },
  vishingRecentActivity: {
    title: 'Vishing Recent Activity',
    key: 'vishing-recent-activity',
    path: '/recent-activity',
  },

  // Deepfake Management
  deepfakeManagement: {
    title: 'Deepfake Management',
    key: 'deepfake-management',
    path: '/deepfake-management/*',
  },
  deepfakeDashboard: {
    title: 'Deepfake Dashboard',
    key: 'deepfake-dashboard',
    path: '/dashboard',
  },
  deepfakeCreate: {
    title: 'Create DeepFake',
    key: 'create-deepfake',
    path: '/create',
  },
  deepfakeEdit: {
    title: 'Edit DeepFake',
    key: 'edit-deepfake',
    path: '/edit/:id',
  },
  deepFakeContentLibrary: {
    title: 'DeepFake Content Library',
    key: 'deepfake-content-library',
    path: '/content-library',
  },
  deepfakeProviderConfiguration: {
    title: 'Deepfake Provider Configuration',
    key: 'provider-configuration',
    path: '/provider-configuration',
  },

  // Administration
  administrationManagement: {
    title: 'Administration Management',
    key: 'administration-management',
    path: '/administration-management/*',
  },
  aiProviderConfiguration: {
    title: 'AI Provider Configuration',
    key: 'ai-provider-configuration',
    path: '/ai-provider-configuration',
  },
  providerConfiguration: {
    title: 'Provider Configuration',
    key: 'provider-configuration',
    path: '/provider-configuration',
  },
  configurations: {
    title: 'Configurations',
    key: 'configurations',
    path: '/configurations',
  },

  // Breach Detection
  breachDashboard: {
    title: 'Breach Dashboard',
    key: 'breach-dashboard',
    path: '/breaches',
  },
  breachManagement: {
    title: 'Breach Management',
    key: 'breach-management',
    path: '/breaches-management',
  },
  recipientBreaches: {
    title: 'Recipient Breaches',
    key: 'recipient-breaches',
    path: '/breaches-recipients',
  },

  breachMonitor: {
    title: 'Breach Monitor',
    key: 'breach-monitor',
    path: '/breach-monitor/*',
  },
  breachMonitorDashboard: {
    title: 'Breach Monitor',
    key: 'breach-monitor',
    path: '/dashboard',
  },
  breachMonitorEmails: {
    title: 'Breach Monitor Emails',
    key: 'breach-monitor-emails',
    path: '/emails',
  },
  breachMonitorIps: {
    title: 'Breach Monitor IPs',
    key: 'breach-monitor-ips',
    path: '/ips',
  },
  breachMonitorThreatIntel: {
    title: 'Breach Monitor Threat Intelligence',
    key: 'breach-monitor-threat-intelligence',
    path: '/threat-intelligence',
  },
  breachMonitorVulnerabilities: {
    title: 'Breach Monitor Vulnerability',
    key: 'breach-monitor-vulnerability',
    path: '/vulnerability',
  },
  breachMonitorCompanyImpersonation: {
    title: 'Breach Monitor Company Impersonation',
    key: 'breach-monitor-company-impersonation',
    path: '/company-impersonation',
  },

  // Policy Management
  policyManagement: {
    title: 'Policy Management',
    key: 'policy-management',
    path: '/policy-management/*',
  },

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

  assignedPolicies: {
    title: 'Assigned Policies',
    key: 'assigned-policies',
    path: '/assigned-policies',
  },
  requestedPolicies: {
    title: 'Requested Policies',
    key: 'requested-policies',
    path: '/requested-policies',
  },

  // Product Management
  productManagement: {
    title: 'Product Management',
    key: 'product-management',
    path: '/product-management/*',
  },

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
  reportManagement: {
    title: 'Report Management',
    key: 'report-management',
    path: '/report-management/*',
  },

  userReport: {
    title: 'User Report',
    key: 'user-report',
    path: '/user-report',
  },
  supportTicketReport: {
    title: 'Support Ticket Report',
    key: 'support-ticket-report',
    path: '/support-ticket-report',
  },
  billingPaymentReport: {
    title: 'Billing and Payment Report',
    key: 'billing-payment-report',
    path: '/billing-payment-report',
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

  licenseReport: {
    title: 'License Report',
    key: 'license-report',
    path: '/license-report',
  },
  userRiskReport: {
    title: 'User Risk Report',
    key: 'user-risk-report',
    path: '/user-risk-report',
  },
  certificateReport: {
    title: 'Certificate Report',
    key: 'certificate-report',
    path: '/certificate-report',
  },
  userActivityReport: {
    title: 'User Activity Report',
    key: 'user-activity-report',
    path: '/user-activity-report',
  },
  phishingContentReport: {
    title: 'Phishing Content Report',
    key: 'phishing-content-report',
    path: '/phishing-content-report',
  },
  performanceReport: {
    title: 'Performance Report',
    key: 'performance-report',
    path: '/performance-report',
  },

  // Role Management
  roleManagement: {
    title: 'Role Management',
    key: 'role-management',
    path: '/role-management/*',
  },

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
  settings: {
    title: 'Settings',
    key: 'settings',
    path: '/settings/*',
  },

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
  tags: {
    title: 'Product Tags',
    key: 'tags',
    path: '/tags',
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
  billingActions: {
    title: 'Billing Actions',
    key: 'billing-actions',
    path: '/billing-actions',
  },
  billingNextStep: {
    title: 'Billing Next Step',
    key: 'billing-next-step',
    path: '/billing-next-step',
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
  policyType: {
    title: 'Policy Type',
    key: 'policy-type',
    path: '/policy-type',
  },
  latestNewsCategory: {
    title: 'Latest News Category',
    key: 'latest-news-category',
    path: '/latest-news-category',
  },
  supportTicketType: {
    title: 'Support Ticket Type',
    key: 'support-ticket-type',
    path: '/support-ticket-type',
  },
  userRanges: {
    title: 'User Ranges',
    key: 'user-ranges',
    path: '/user-ranges',
  },
  mspType: {
    title: 'MSP Type',
    key: 'msp-type',
    path: '/msp-type',
  },
  creditReason: {
    title: 'Credit Allocation Reason',
    key: 'credit-reason',
    path: '/credit-reason',
  },
  netTerm: {
    title: 'Net Term Configuration',
    key: 'net-term',
    path: '/net-term',
  },
  suspendReason: {
    title: 'Suspend Reasons',
    key: 'suspend-reason',
    path: '/suspend-reason',
  },
  // Support Management
  supportManagement: {
    title: 'Support Management',
    key: 'support-management',
    path: '/support-management/*',
  },

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

  supportTickets: {
    title: 'Support Tickets',
    key: 'support-tickets',
    path: '/support-tickets',
  },

  // Survey Management
  surveyManagement: {
    title: 'Survey Management',
    key: 'survey-management',
    path: '/survey-management/*',
  },

  pollSurvey: {
    title: 'Polls & Surveys List',
    key: 'polls-surveys',
    path: '/polls-surveys',
  },
  createSurvey: {
    title: 'Create Poll/Survey',
    key: 'create-survey',
    path: '/create-survey',
  },

  // User Management
  userManagement: {
    title: 'User Management',
    key: 'users',
    path: '/user-management/*',
  },

  users: {
    title: 'Users',
    key: 'users',
    path: '/users',
  },

  systemUsers: {
    title: 'System Users',
    key: 'system-users',
    path: '/system-users',
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
  syncUser: {
    title: 'Sync Users',
    key: 'sync-users',
    path: '/sync-users',
  },
  userList: {
    title: 'Users List',
    key: 'user-list',
    path: '/user-list',
  },
  userReports: {
    title: 'Users Reports',
    key: 'users-reports',
    path: '/users-reports',
  },
  userAnalytics: {
    title: 'User Analytics',
    key: 'user-analytics',
    path: '/user-analytics',
  },
  userActivityLogs: {
    title: 'Users Activity Logs',
    key: 'users-activity-logs',
    path: '/users-activity-logs',
  },
  suspendUser: {
    title: 'Suspend User',
    key: 'suspend-user',
    path: '/suspend-user',
  },

  userActivities: {
    title: 'User Activities',
    key: 'user-activities',
    path: '/user-activities',
  },
};
