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
  menuList: {
    title: 'Menu List',
    key: 'menu-list',
    path: '/menu-management/menu-list',
  },

  category: {
    title: 'Category List',
    key: 'category-list',
    path: '/settings/category-list',
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
  policyType: {
    title: 'Policy Type',
    key: 'policy-type',
    path: '/settings/policy-type',
  },
  LatestNewsCategory: {
    title: 'Latest News Category',
    key: 'latest-news-category',
    path: '/settings/latest-news-category',
  },
  languages: {
    title: 'Languages',
    key: 'languages',
    path: '/settings/languages',
  },
  manageCompliance: {
    title: 'Manage Compliance',
    key: 'manage-compliance',
    path: '/settings/manage-compliance',
  },
  manageCoupon: {
    title: 'Manage Coupons',
    key: 'manage-coupon',
    path: '/settings/manage-coupon',
  },
  manageCredits: {
    title: 'Manage Credits',
    key: 'manage-credits',
    path: '/settings/manage-credits',
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
  states: {
    title: 'States',
    key: 'states',
    path: '/settings/states',
  },
  tierConfigurations: {
    title: 'Tier Configurations',
    key: 'tier-configurations',
    path: '/settings/tier-configurations',
  },
  timeZones: {
    title: 'Time Zones',
    key: 'time-zones',
    path: '/settings/time-zones',
  },
  vatConfigurations: {
    title: 'VAT Configurations',
    key: 'vat-configurations',
    path: '/settings/vat-configurations',
  },
  billingActions: {
    title: 'Billing Actions',
    key: 'billing-actions',
    path: '/settings/billing-actions',
  },

  billingNextStep: {
    title: 'Billing Next Step',
    key: 'billing-next-step',
    path: '/settings/billing-next-step',
  },
  departmentList: {
    title: 'Departments',
    key: 'departments',
    path: '/settings/departments',
  },

  userReport: {
    title: 'User Report',
    key: 'user-report',
    path: '/report-management/user-report',
  },
  supportTicketReport: {
    title: 'Support Ticket Report',
    key: 'support-ticket-report',
    path: '/report-management/support-ticket-report',
  },
  billingPaymentReport: {
    title: 'Billing and Payment Report',
    key: 'billing-payment-report',
    path: '/report-management/billing-payment-reports',
  },
  accessReport: {
    title: 'Role & Access Report',
    key: 'access-report',
    path: '/access-report',
  },
  productReport: {
    title: 'Product & Package Report',
    key: 'product-report',
    path: '/report-management/product-report',
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
    path: '/report-management/certificate-report',
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
  assignedPolicies: {
    title: 'Assigned Policies',
    key: 'assigned-policies',
    path: '/policy-management/assigned-policies',
  },
  requestedPolicies: {
    title: 'Requested Policies',
    key: 'requested-policies',
    path: '/policy-management/requested-policies',
  },

  // client admin routes
  supportTickets: {
    title: 'Support Tickets',
    key: 'support-tickets',
    path: '/support-management/support-tickets',
  },
  knowledgeHub: {
    title: 'Knowledge Hub',
    key: 'knowledge-hub',
    path: '/knowledge-management/knowledge-hub',
  },
  manageResources: {
    title: 'Manage Resources',
    key: 'manage-resources',
    path: '/knowledge-management/manage-resources',
  },
  resourceAnalytics: {
    title: 'Resource Analytics',
    key: 'resource-analytics',
    path: '/knowledge-management/resource-analytics',
  },
  resourceCategories: {
    title: 'Resource Categories',
    key: 'resource-categories',
    path: '/knowledge-management/resource-categories',
  },
  leaderboard: {
    title: 'Leaderboard',
    key: 'leaderboard',
    path: '/leader-message-management/leader-message-list',
  },

  // Aspire Admin route
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
  resolveTicket: {
    title: 'Resolved Tickets',
    key: 'resolved-tickets',
    path: '/support-management/resolved-tickets',
  },
  pollSurvey: {
    title: 'Polls & Surveys',
    key: 'polls-surveys',
    path: 'survey-management/polls-surveys',
  },
  latestNews: {
    title: 'Latest News',
    key: 'latest-news',
    path: '/news-management/latest-news',
  },
  supportTicketType: {
    title: 'Support Ticket Type',
    key: 'support-ticket-type',
    path: '/settings/support-ticket-type',
  },
  userRanges: {
    title: 'User Ranges',
    key: 'user-ranges',
    path: '/settings/user-ranges',
  },
  mspType: {
    title: 'MSP Type',
    key: 'msp-type',
    path: '/settings/msp-type',
  },
  creditReason: {
    title: 'Credit Allocation Reason',
    key: 'credit-reason',
    path: '/settings/credit-reason',
  },
  netTerm: {
    title: 'Net Term Configuration',
    key: 'net-term',
    path: '/settings/net-term',
  },
  suspendReason: {
    title: 'Suspend Reasons',
    key: 'suspend-reason',
    path: '/settings/suspend-reason',
  },
};
