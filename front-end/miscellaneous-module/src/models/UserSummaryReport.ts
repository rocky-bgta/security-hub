import { IList } from 'models/Global';

export interface IUserSummaryTotals {
  totalUsers: number;
  activeUsers: number;
  suspendedUsers: number;
  newSignupsLast30Days: number;
}

export interface IUserSummaryGrowthTrendItem {
  year: number;
  month: number;
  label: string;
  totalUsers: number;
}

export interface IUserCardInfo {
  totalMsp: number;
  totalClientAdmin: number;
  totalLicenseUser: number;
  totalActiveUser: number;
  totalSuspendedUser: number;
}

export interface IPlatformGrowthTrendPoint {
  year: number;
  month: number | null;
  quarter: number | null;
  label: string;
  totalMsp: number;
  totalClientAdmin: number;
  totalLicenseUser: number;
  totalActiveUser: number;
}

export type TPlatformGrowthTrendRange =
  | 'MONTHLY'
  | 'QUARTERLY'
  | 'HALF_YEARLY'
  | 'YEARLY';

export type TRiskGroup =
  | 'LOW_RISK'
  | 'MEDIUM_RISK'
  | 'HIGH_RISK'
  | 'CRITICAL_RISK';

export interface IUserSummaryDetailItem {
  name: string;
  email: string;
  department: string;
  riskGroup: TRiskGroup;
  role: string;
  status: string;
  lastLoginAt: string;
  createdAt: string;
}

export interface IUserSummaryReportData {
  totals: IUserSummaryTotals;
  growthTrend: Array<IUserSummaryGrowthTrendItem>;
  platformGrowthTrend: Array<IPlatformGrowthTrendPoint>;
  userCardInfo: IUserCardInfo;
  details: IList<IUserSummaryDetailItem>;
}
