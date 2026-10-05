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
  userManagement: {
    title: 'User Management',
    key: 'users',
    path: '/user-management',
  },
  contentManagement: {
    title: 'Content Management',
    key: 'content-management',
    path: '/',
  },
  buyProduct: {
    title: 'Buy Product',
    key: 'buy-product',
    path: '/buy-product',
  },

  // mixed routes
  packageList: {
    title: 'Package List',
    key: 'package-list',
    path: '/package-list',
  },
  subPackages: {
    title: 'Sub Packages',
    key: 'sub-packages',
    path: '/package-management/sub-packages',
  },
  courseList: {
    title: 'Course List',
    key: 'course-list',
    path: '/course-list',
  },

  // super admin exclusive routes
  productList: {
    title: 'Product List',
    key: 'product-list',
    path: '/product-management/product-list',
  },
  contentLibrary: {
    title: 'Content Library',
    key: 'content-library',
    path: '/content-library',
  },
  addContent: {
    title: 'Add Content',
    key: 'add-content',
    path: '/content-management/add-content',
  },
  featureList: {
    title: 'Feature List',
    key: 'feature-list',
    path: '/feature-management/feature-list',
  },
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
  accountSecurity: {
    title: 'Account Security',
    key: 'account-security',
    path: '/account/security',
  },
  aspireAdmin: {
    title: 'Account Billing',
    key: 'account-billing',
    path: '/aspire-admin',
  },
  clientAdmin: {
    title: 'Account Billing',
    key: 'account-billing',
    path: '/client-admin',
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
  phishingList: {
    title: 'Phishing Report',
    key: 'phishing-report',
    path: '/phishing-list',
  },
  notification: {
    title: 'Notification',
    key: 'notification',
    path: '/notification',
  },
  userActivityLogs: {
    title: 'Activity Logs',
    key: 'activity-logs',
    path: '/activity-logs',
  },

  // client admin exclusive routes
  productAnalytics: {
    title: 'Product Analytics',
    key: 'product-analytics',
    path: '/product-management/product-analytics',
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
  licenseOverview: {
    title: 'License Overview',
    key: 'license-overview',
    path: '/license-management/license-overview',
  },
  licenseAssignment: {
    title: 'License Assignment',
    key: 'license-assignment',
    path: '/license-management/license-assignment',
  },
  certificateHistory: {
    title: 'Certificate History',
    key: 'certificate-history',
    path: '/certificate-management/certificate-history',
  },
  certificateTemplate: {
    title: 'Certificate Template',
    key: 'certificate-template',
    path: '/certificate-management/certificate-template',
  },

  // Exam management Routes
  examList: {
    title: 'Exam Management',
    key: 'exam-management',
    path: '/exams',
  },
  examLibrary: {
    title: 'Exam Library',
    key: 'exam-library',
    path: '/exam-management/exam-library',
  },
  createExam: {
    title: 'Create Exam',
    key: 'create-exam',
    path: '/create-exam',
  },
  examSettings: {
    title: 'Exam Setting',
    key: 'exam-setting',
    path: '/exam-setting',
  },
  examineeReports: {
    title: 'Examinee Reports',
    key: 'examinee-reports',
    path: '/examinee-reports',
  },
  examAnalytics: {
    title: 'Exam Analytics',
    key: 'exam-analytics',
    path: '/exam-analytics',
  },
  bulkExamImport: {
    title: 'Bulk Exam Import',
    key: 'bulk-exam-import',
    path: '/bulk-exam-import',
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

  // client
  clientViewDetails: {
    title: 'Client View Details',
    key: 'client-view-details',
    path: '/client/:id',
  },
  clientIndividualSubPackage: {
    title: 'Client Sub Package',
    key: 'client-sub-package',
    path: '/client/:id/sub-package',
  },
  clientLicenseHistory: {
    title: 'License History',
    key: 'client-license-history',
    path: '/client-management/client-license-history',
  },
  mspLicenseHistory: {
    title: 'License History',
    key: 'msp-license-history',
    path: '/msp-management/msp-license-history',
  },
};
