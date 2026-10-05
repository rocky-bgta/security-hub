/**
 * SMS Server Configuration models and interfaces
 */

export enum SmsProvider {
  TWILIO = 'TWILIO',
}

export enum SmsServerConfigurationStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum SenderConfigurationType {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
}

export interface ISmsServerConfiguration {
  id: string;
  name: string;
  provider: string;
  apiKeyMasked: string;
  apiSecretMasked: string;
  senderId: string;
  baseUrl: string;
  status: SmsServerConfigurationStatus;
  providerMetadata?: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
  default: boolean;
}

export interface ISmsServerConfigurationForm {
  name: string;
  provider: string;
  apiKey: string;
  apiSecret: string;
  senderId: string;
  baseUrl: string;
  status: SmsServerConfigurationStatus;
  default: boolean;
}

export interface ISmsServerConfigurationListParams {
  offset?: number;
  pageSize?: number;
  searchParam?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export const SMS_PROVIDER_LABELS: Record<SmsProvider, string> = {
  [SmsProvider.TWILIO]: 'Twilio',
};

export const getSmsProviderLabel = (provider: string): string =>
  SMS_PROVIDER_LABELS[provider as SmsProvider] ?? provider;

export const SMS_SERVER_CONFIGURATION_STATUS_LABELS: Record<
  SmsServerConfigurationStatus,
  string
> = {
  [SmsServerConfigurationStatus.ACTIVE]: 'Active',
  [SmsServerConfigurationStatus.INACTIVE]: 'Inactive',
};
