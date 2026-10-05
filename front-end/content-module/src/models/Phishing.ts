export enum PhishingCampaignChannel {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  VOICE = 'VOICE',
}

export interface IPhishingCampaignEnrollment {
  hasReceivedCampaign: boolean;
  hasReceivedEmailCampaign: boolean;
  hasReceivedSmsCampaign: boolean;
  hasReceivedVoiceCampaign: boolean;
}

export interface IPhishingCampaignStatistics {
  totalCampaigns: number;
  openCount: number;
  clickCount: number;
  compromiseCount: number;
  reportCount: number;
}

export type PhishingStatisticKey = Exclude<
  keyof IPhishingCampaignStatistics,
  'totalCampaigns'
>;

export interface IPhishingStatisticMetric {
  name: string;
  colors: string;
  valueKey: PhishingStatisticKey;
}
