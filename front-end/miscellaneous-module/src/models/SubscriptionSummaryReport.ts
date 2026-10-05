import { IList } from 'models/Global';

export interface ISubscriptionSummaryTotals {
  totalSubscriptions: number;
  active: number;
  expired: number;
  expiringSoon: number;
}

export interface ISubscriptionSummaryDetailItem {
  client: string;
  plan: string;
  startDate: string;
  endDate: string;
  status: string;
  usage: number;
}

export interface ISubscriptionSummaryReportData {
  totalSubscriptions: number;
  active: number;
  expired: number;
  expiringSoon: number;
  subscriptions: IList<ISubscriptionSummaryDetailItem>;
}
