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

  // -------------------------------------------------------------------------
  // Phishing Management
  // -------------------------------------------------------------------------
  phishingDashboard: {
    title: 'Phishing Dashboard',
    key: 'phishing-dashboard',
    path: '/phishing-management/dashboard',
  },
  phishingDomainManagement: {
    title: 'Domain Management',
    key: 'domain-management',
    path: '/phishing-management/domain-management',
  },
  phishingCampaings: {
    title: 'Campaign List',
    key: 'campaign-list',
    path: '/phishing-management/campaigns',
  },
  phishingCampaignCreate: {
    title: 'Create Campaign',
    key: 'campaign-create',
    path: '/phishing-management/create-campaign',
  },
  phishingCampaignDetails: {
    title: 'Campaign Details',
    key: 'campaign-details',
    path: '/phishing-management/campaigns/:id',
  },
  phishingCampaignEdit: {
    title: 'Edit Campaign',
    key: 'campaign-edit',
    path: '/phishing-management/edit-campaign/:id',
  },
  phishingTemplateLibrary: {
    title: 'Template Library',
    key: 'template-library',
    path: '/phishing-management/template-library',
  },
  phishingTemplateLibraryCreate: {
    title: 'Create Template',
    key: 'template-library-create',
    path: '/phishing-management/create-template',
  },
  phishingTemplateLibraryEdit: {
    title: 'Edit Template',
    key: 'template-library-edit',
    path: '/phishing-management/edit-template/:id',
  },
  phishingLandingPages: {
    title: 'Phishing Landing Pages',
    key: 'phishing-landing-pages',
    path: '/phishing-management/landing-pages',
  },
  phishingLandingPageCreate: {
    title: 'Create Landing Page',
    key: 'landing-page-create',
    path: '/phishing-management/create-landing-page',
  },
  phishingLandingPageEdit: {
    title: 'Edit Landing Page',
    key: 'landing-page-edit',
    path: '/phishing-management/edit-landing-page/:id',
  },
  phishingSenderProfiles: {
    title: 'Phishing Sender Profiles',
    key: 'phishing-sender-profiles',
    path: '/phishing-management/sender-profiles',
  },
  phishingReports: {
    title: 'Phishing Reports',
    key: 'phishing-reports',
    path: '/phishing-management/reports',
  },
  phishingCampaignRiskImpacts: {
    title: 'Phishing Campaign Risk Impacts',
    key: 'phishing-campaign-risk-impacts',
    path: '/phishing-management/campaign-risk-impacts',
  },
  phishingCampaignReports: {
    title: 'Phishing Campaign Reports',
    key: 'phishing-campaign-reports',
    path: '/phishing-management/campaign-reports',
  },
  phishingCampaignDetailReport: {
    title: 'Phishing Campaign Detail Report',
    key: 'phishing-campaign-detail-report',
    path: '/phishing-management/campaign-reports/:id',
  },
  phishingUserRiskReport: {
    title: 'User Risk Report',
    key: 'user-risk-report',
    path: '/phishing-management/user-risk-report',
  },
  phishingRecentActivity: {
    title: 'Recent Activity',
    key: 'recent-activity',
    path: '/phishing-management/recent-activity',
  },
  phishingCourseStatisticsDetails: {
    title: 'Course Statistics Details',
    key: 'course-statistics-details',
    path: '/phishing-management/course-statistics-details',
  },

  // -------------------------------------------------------------------------
  // Smishing Management
  // -------------------------------------------------------------------------
  smishingDashboard: {
    title: 'Smishing Dashboard',
    key: 'smishing-dashboard',
    path: '/smishing-management/dashboard',
  },
  smishingCampaigns: {
    title: 'Smishing Campaigns',
    key: 'smishing-campaigns',
    path: '/smishing-management/campaigns',
  },
  smishingCampaignDetails: {
    title: 'Smishing Campaign Details',
    key: 'smishing-campaign-details',
    path: '/smishing-management/campaigns/:id',
  },
  smishingSimulationCreate: {
    title: 'Create Smishing Campaign',
    key: 'create-smishing-simulation',
    path: '/smishing-management/create-simulation',
  },
  smishingSimulationEdit: {
    title: 'Edit Smishing Simulation',
    key: 'edit-smishing-simulation',
    path: '/smishing-management/edit-simulation/:id',
  },
  smsTemplateLibrary: {
    title: 'SMS Template Library',
    key: 'sms-template-library',
    path: '/smishing-management/sms-template-library',
  },
  smsTemplateLibraryCreate: {
    title: 'Create SMS Template',
    key: 'sms-template-library-create',
    path: '/smishing-management/create-sms-template',
  },
  smsTemplateLibraryEdit: {
    title: 'Edit SMS Template',
    key: 'sms-template-library-edit',
    path: '/smishing-management/edit-sms-template/:id',
  },
  smsServerConfigurations: {
    title: 'SMS Server Configurations',
    key: 'sms-server-configurations',
    path: '/smishing-management/sms-server-configurations',
  },
  smishingLandingPages: {
    title: 'Smishing Landing Pages',
    key: 'smishing-landing-pages',
    path: '/smishing-management/landing-pages',
  },
  smishingLandingPageCreate: {
    title: 'Create Smishing Landing Page',
    key: 'smishing-landing-page-create',
    path: '/smishing-management/create-landing-page',
  },
  smishingLandingPageEdit: {
    title: 'Edit Smishing Landing Page',
    key: 'smishing-landing-page-edit',
    path: '/smishing-management/edit-landing-page/:id',
  },
  smishingReports: {
    title: 'Smishing Reports',
    key: 'smishing-reports',
    path: '/smishing-management/reports',
  },
  smishingCampaignRiskImpacts: {
    title: 'Smishing Campaign Risk Impacts',
    key: 'smishing-campaign-risk-impacts',
    path: '/smishing-management/campaign-risk-impacts',
  },
  smishingCourseStatisticsDetails: {
    title: 'Smishing Course Statistics Details',
    key: 'smishing-course-statistics-details',
    path: '/smishing-management/course-statistics-details',
  },
  smishingCampaignReports: {
    title: 'Smishing Campaign Reports',
    key: 'smishing-campaign-reports',
    path: '/smishing-management/campaign-reports',
  },
  smishingCampaignDetailReport: {
    title: 'Smishing Campaign Detail Report',
    key: 'smishing-campaign-detail-report',
    path: '/smishing-management/campaign-reports/:id',
  },
  smishingUserRiskReport: {
    title: 'Smishing User Risk Report',
    key: 'smishing-user-risk-report',
    path: '/smishing-management/user-risk-report',
  },
  smishingRecentActivity: {
    title: 'Smishing Recent Activity',
    key: 'smishing-recent-activity',
    path: '/smishing-management/recent-activity',
  },

  // -------------------------------------------------------------------------
  // Vishing Management
  // -------------------------------------------------------------------------
  vishingDashboard: {
    title: 'Vishing Dashboard',
    key: 'vishing-dashboard',
    path: '/vishing-management/dashboard',
  },
  vishingCampaigns: {
    title: 'Vishing Campaigns',
    key: 'vishing-campaigns',
    path: '/vishing-management/campaigns',
  },
  vishingCampaignRiskImpacts: {
    title: 'Vishing Campaign Risk Impacts',
    key: 'vishing-campaign-risk-impacts',
    path: '/vishing-management/campaign-risk-impacts',
  },
  vishingCampaignDetails: {
    title: 'Vishing Campaign Details',
    key: 'vishing-campaign-details',
    path: '/vishing-management/campaigns/:id',
  },
  vishingSimulationCreate: {
    title: 'Create Vishing Simulation',
    key: 'create-vishing-simulation',
    path: '/vishing-management/create-simulation',
  },
  vishingSimulationEdit: {
    title: 'Edit Vishing Simulation',
    key: 'edit-vishing-simulation',
    path: '/vishing-management/edit-simulation/:id',
  },
  vishingTemplateLibrary: {
    title: 'Vishing Template Library',
    key: 'vishing-template-library',
    path: '/vishing-management/template-library',
  },
  vishingAttackTemplates: {
    title: 'Vishing Attack Templates',
    key: 'vishing-attack-templates',
    path: '/vishing-management/attack-templates',
  },
  vishingReports: {
    title: 'Vishing Reports',
    key: 'vishing-reports',
    path: '/vishing-management/reports',
  },
  voiceServerConfiguration: {
    title: 'Voice Server Configuration',
    key: 'voice-server-configuration',
    path: '/vishing-management/voice-server-configuration',
  },
  vishingProviderConfiguration: {
    title: 'Vishing Provider Configuration',
    key: 'vishing-provider-configuration',
    path: '/vishing-management/provider-configuration',
  },
  vishingCourseStatisticsDetails: {
    title: 'Vishing Course Statistics Details',
    key: 'vishing-course-statistics-details',
    path: '/vishing-management/course-statistics-details',
  },
  vishingCampaignReports: {
    title: 'Vishing Campaign Reports',
    key: 'vishing-campaign-reports',
    path: '/vishing-management/campaign-reports',
  },
  vishingCampaignDetailReport: {
    title: 'Vishing Campaign Detail Report',
    key: 'vishing-campaign-detail-report',
    path: '/vishing-management/campaign-reports/:id',
  },
  vishingUserRiskReport: {
    title: 'Vishing User Risk Report',
    key: 'vishing-user-risk-report',
    path: '/vishing-management/user-risk-report',
  },
  vishingRecentActivity: {
    title: 'Vishing Recent Activity',
    key: 'vishing-recent-activity',
    path: '/vishing-management/recent-activity',
  },

  // -------------------------------------------------------------------------
  // Deepfake
  // -------------------------------------------------------------------------
  deepfakeDashboard: {
    title: 'Deepfake Dashboard',
    key: 'deepfake-dashboard',
    path: '/deepfake-management/dashboard',
  },
  deepfakeCreate: {
    title: 'Create DeepFake',
    key: 'create-deepfake',
    path: '/deepfake-management/create',
  },
  deepfakeEdit: {
    title: 'Edit DeepFake',
    key: 'edit-deepfake',
    path: '/deepfake-management/edit/:id',
  },
  deepFakeContentLibrary: {
    title: 'DeepFake Content Library',
    key: 'deepfake-content-library',
    path: '/deepfake-management/content-library',
  },
  deepfakeProviderConfiguration: {
    title: 'Deepfake Provider Configuration',
    key: 'provider-configuration',
    path: '/deepfake-management/provider-configuration',
  },

  // -------------------------------------------------------------------------
  // Admin config (under Phishing Management)
  // -------------------------------------------------------------------------
  aiProviderConfiguration: {
    title: 'AI Provider Configuration',
    key: 'ai-provider-configuration',
    path: '/phishing-management/ai-provider-configuration',
  },
  providerConfiguration: {
    title: 'Provider Configuration',
    key: 'provider-configuration',
    path: '/phishing-management/provider-configuration',
  },
  configurations: {
    title: 'Configurations',
    key: 'configurations',
    path: '/phishing-management/configurations',
  },

  // -------------------------------------------------------------------------
  // Breach Detection
  // -------------------------------------------------------------------------
  breachDashboard: {
    title: 'Breach Dashboard',
    key: 'breach-dashboard',
    path: '/phishing-management/breaches',
  },
  breachManagement: {
    title: 'Breach Management',
    key: 'breach-management',
    path: '/phishing-management/breaches-management',
  },
  recipientBreaches: {
    title: 'Recipient Breaches',
    key: 'recipient-breaches',
    path: '/phishing-management/breaches-recipients',
  },
  breachDetails: {
    title: 'Breach Details',
    key: 'breach-details',
    path: '/phishing-management/breaches/:id',
  },

  // Dashboard & Analytics

  // Reports

  // AI provider

  // -------------------------------------------------------------------------
  // Breach Monitor
  // -------------------------------------------------------------------------
  breachMonitorDashboard: {
    title: 'Breach Monitor',
    key: 'breach-monitor',
    path: '/breach-monitor/dashboard',
  },
  breachMonitorEmails: {
    title: 'Breach Monitor Emails',
    key: 'breach-monitor-emails',
    path: '/breach-monitor/emails',
  },
  breachMonitorIps: {
    title: 'Breach Monitor IPs',
    key: 'breach-monitor-ips',
    path: '/breach-monitor/ips',
  },
  breachMonitorThreatIntel: {
    title: 'Breach Monitor Threat Intelligence',
    key: 'breach-monitor-threat-intelligence',
    path: '/breach-monitor/threat-intelligence',
  },
  breachMonitorVulnerabilities: {
    title: 'Breach Monitor Vulnerability',
    key: 'breach-monitor-vulnerability',
    path: '/breach-monitor/vulnerability',
  },
  breachMonitorCompanyImpersonation: {
    title: 'Breach Monitor Company Impersonation',
    key: 'breach-monitor-company-impersonation',
    path: '/breach-monitor/company-impersonation',
  },

  // Not Found
  notFound: {
    title: 'Page Not Found',
    key: 'page-not-found',
    path: '*',
  },
};
