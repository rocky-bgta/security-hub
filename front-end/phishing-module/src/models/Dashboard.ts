/**
 * Dashboard and Analytics models for phishing module
 */

import {
  getSimulationCopy,
  type SimulationChannel,
} from 'utils/SimulationChannel';

// Enums
export enum RiskLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

export enum ActivityType {
  EMAIL_SENT = 'EMAIL_SENT',
  EMAIL_DELIVERED = 'EMAIL_DELIVERED',
  EMAIL_BOUNCED = 'EMAIL_BOUNCED',
  EMAIL_NOT_OPENED_BUT_REPORTED = 'EMAIL_NOT_OPENED_BUT_REPORTED',
  EMAIL_OPENED = 'EMAIL_OPENED',
  LINK_CLICKED = 'LINK_CLICKED',
  ATTACHMENT_OPENED = 'ATTACHMENT_OPENED',
  DATA_SUBMITTED = 'DATA_SUBMITTED',
  EMAIL_REPORTED = 'EMAIL_REPORTED',
}

// Trend data point for charts
export interface ITrendDataPoint {
  date: string;
  value: number;
  label?: string;
}

export interface ITrendData {
  openRateTrend: Array<ITrendDataPoint>;
  clickRateTrend: Array<ITrendDataPoint>;
  reportRateTrend: Array<ITrendDataPoint>;
  submissionRateTrend: Array<ITrendDataPoint>;
}

export interface IPhishProneDataPoint {
  humanRiskScore: {
    score: number;
    tier: 'LOW_RISK' | 'MEDIUM_RISK' | 'HIGH_RISK' | 'CRITICAL_RISK';
  };
  phishProneUsers: Array<{
    tier: RiskLevel;
    count: number;
  }>;
  informationSubmits: number;
  reportRate: {
    current: string;
    trend: 'stable' | 'up' | 'down';
  };
  riskTrend: 'stable' | 'up' | 'down';
  topRiskUsers: Array<{
    userId: string;
    email: string;
    phishingRiskScore: number;
    riskLevel: RiskLevel;
  }>;
}

// Dashboard Overview
export interface IDashboardOverview {
  totalCampaigns: number;
  activeCampaigns: number;
  completedCampaigns: number;
  draftCampaigns: number;
  totalEmailsSent: number;
  avgOpenRate: number;
  avgClickRate: number;
  phishPronePercentage: number;
  totalUsers: number;
  highRiskUsers: number;
  criticalRiskUsers: number;
  repeatOffenders: number;
  breachesDetected: number;
  affectedUsers: number;
  openRateTrend: ITrendDataPoint[];
  clickRateTrend: ITrendDataPoint[];
  reportRateTrend: ITrendDataPoint[];
  submissionRateTrend: ITrendDataPoint[];
}

export interface IDashboardRiskImpact {
  campaignId: string;
  campaignName: string;
  type: string;
  targetGroup: string;
  riskImpact: RiskLevel;
  aiRating: string;
  impactedUserCount: number;
  startDate: string;
  endDate: string;
  status: string;
}

// KPI Metrics (7 core cards from BRD)
export interface IDashboardKpi {
  attacks: number; // Total phishing attacks launched
  hacks: number; // Successful compromises
  reports: number; // Emails reported by users
  campaigns: number; // Total campaigns
  templates: number; // Email templates
  groups: number; // Recipient groups
  landingPages: number; // Landing pages
  compromiseRate: number;
  reportRate: number;
}

// Email Statistics
export interface IEmailStats {
  totalEmailsSent: number;
  emailsDelivered: number;
  emailsBounced: number;
  emailsOpened: number;
  linksClicked: number;
  attachmentsOpened: number;
  dataSubmitted: number;
  emailsReported: number;
  deliveryRate: number;
  openRate: number;
  clickRate: number;
  compromiseRate: number;
  reportRate: number;
  phishPronePercentage: number;
}

// User Risk Distribution
export interface IUserRiskDistribution {
  totalUsers: number;
  lowRiskCount: number;
  mediumRiskCount: number;
  highRiskCount: number;
  criticalRiskCount: number;
  lowRiskPercentage: number;
  mediumRiskPercentage: number;
  highRiskPercentage: number;
  criticalRiskPercentage: number;
  repeatOffenders: number;
  compromisedUsers: number;
}

// Campaign Performance
export interface ICampaignPerformance {
  campaignId: string;
  campaignName: string;
  status: string;
  totalRecipients: number;
  emailsSent: number;
  emailsDelivered: number;
  opened: number;
  clicked: number;
  dataSubmitted: number;
  reported: number;
  bounced: number;
  deliveryRate: number;
  openRate: number;
  clickRate: number;
  compromiseRate: number;
  reportRate: number;
  startDate: string | null;
  endDate: string | null;
  durationDays: number;
  riskRanking?: number;
}

// User Risk Summary
export interface IUserRiskSummary {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  department: string | null;
  riskLevel: RiskLevel;
  riskScore: number;
  campaignsTargeted: number;
  emailsReceived: number;
  emailsOpened: number;
  emailsClicked: number;
  dataSubmissions: number;
  emailsReported: number;
  breachesInvolved: number;
  isRepeatOffender: boolean;
  lastActivityAt: string | null;
  lastClickedAt: string | null;
}

// Email Activity
export interface IEmailActivity {
  activityId: string;
  campaignId: string;
  campaignName?: string;
  recipientId: string;
  recipientEmail?: string;
  recipientName?: string;
  activityType: ActivityType;
  activityLabel: string;
  timestamp: string;
  ipAddress?: string;
  geoLocation?: string;
  deviceType?: string;
  browser?: string;
}

// Breach Summary
export interface IBreachSummary {
  totalBreaches: number;
  affectedUsers: number;
  uniqueDomains: number;
  resolvedBreaches: number;
  pendingActions: number;
  breachesThisMonth: number;
  breachesLastMonth: number;
  monthOverMonthChange: number;
  breachesBySource: IBreachBySource[];
}

// Insecure Web Findings Summary
export interface IInsecureWebFindingsSummary {
  domainCount: number;
  breachCount: number;
}

export interface IBreachBySource {
  source: string;
  count: number;
  percentage: number;
}

export interface IPhishingCourseStatistics {
  totalUsers: number;
  completedUsers: number;
  completePercentage: number;
  inProgressUsers: number;
  inProgressPercentage: number;
  pendingUsers: number;
  pendingPercentage: number;
  expiredUsers: number;
  expiredPercentage: number;
}

export interface IPhishingCourseDetails {
  campaignName: string;
  userName: string;
  email: string;
  department: string;
  assignedDate: string;
  expireDate: string;
  status: string;
  progress: number;
}

// Helper functions
export const getRiskLevelColor = (level: RiskLevel): string => {
  switch (level) {
    case RiskLevel.LOW:
      return 'bg-green-100 text-green-800';
    case RiskLevel.MEDIUM:
      return 'bg-yellow-100 text-yellow-800';
    case RiskLevel.HIGH:
      return 'bg-orange-100 text-orange-800';
    case RiskLevel.CRITICAL:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

export const getRiskLevelLabel = (level: RiskLevel): string => {
  switch (level) {
    case RiskLevel.LOW:
      return 'Low Risk';
    case RiskLevel.MEDIUM:
      return 'Medium Risk';
    case RiskLevel.HIGH:
      return 'High Risk';
    case RiskLevel.CRITICAL:
      return 'Critical Risk';
    default:
      return 'Unknown';
  }
};

export const getActivityTypeIcon = (type: ActivityType): string => {
  switch (type) {
    case ActivityType.EMAIL_SENT:
      return '📤';
    case ActivityType.EMAIL_DELIVERED:
      return '✅';
    case ActivityType.EMAIL_BOUNCED:
      return '❌';
    case ActivityType.EMAIL_NOT_OPENED_BUT_REPORTED:
      return '📣';
    case ActivityType.EMAIL_OPENED:
      return '👁️';
    case ActivityType.LINK_CLICKED:
      return '🔗';
    case ActivityType.ATTACHMENT_OPENED:
      return '📎';
    case ActivityType.DATA_SUBMITTED:
      return '⚠️';
    case ActivityType.EMAIL_REPORTED:
      return '🚨';
    default:
      return '📧';
  }
};

export const getActivityTypeLabel = (
  type: ActivityType,
  channel: SimulationChannel = 'phishing',
): string => {
  const copy = getSimulationCopy(channel);
  switch (type) {
    case ActivityType.EMAIL_SENT:
      return copy.activitySent;
    case ActivityType.EMAIL_DELIVERED:
      return copy.activityDelivered;
    case ActivityType.EMAIL_BOUNCED:
      return copy.activityBounced;
    case ActivityType.EMAIL_NOT_OPENED_BUT_REPORTED:
      return copy.activityNotOpenedReported;
    case ActivityType.EMAIL_OPENED:
      return copy.activityOpened;
    case ActivityType.LINK_CLICKED:
      return copy.activityClicked;
    case ActivityType.ATTACHMENT_OPENED:
      return copy.activityAttachment;
    case ActivityType.DATA_SUBMITTED:
      return copy.activitySubmitted;
    case ActivityType.EMAIL_REPORTED:
      return copy.activityReported;
    default:
      return type;
  }
};

// Chart colors
export const RISK_COLORS = {
  [RiskLevel.LOW]: '#22c55e', // green-500
  [RiskLevel.MEDIUM]: '#eab308', // yellow-500
  [RiskLevel.HIGH]: '#f97316', // orange-500
  [RiskLevel.CRITICAL]: '#ef4444', // red-500
};

export const CHART_COLORS = {
  primary: '#3b82f6', // blue-500
  secondary: '#8b5cf6', // violet-500
  success: '#22c55e', // green-500
  warning: '#f59e0b', // amber-500
  danger: '#ef4444', // red-500
  info: '#06b6d4', // cyan-500
};
