/**
 * Voice Server Configuration models
 * @see Voice Server Configurations — /api/v1/phishing/voice-server-configurations
 */

export enum VoiceProvider {
  TWILIO = 'TWILIO',
  GENERIC_SIP = 'GENERIC_SIP',
}

export enum VoiceServerConfigurationStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export type VoiceProviderType = `${VoiceProvider}`;
export type VoiceServerStatus = `${VoiceServerConfigurationStatus}`;

export interface IVoiceServerConfiguration {
  id: string;
  name: string;
  provider: string;
  apiKeyMasked?: string;
  apiSecretMasked?: string;
  callerId?: string;
  baseUrl?: string;
  region?: string;
  countryCode?: string;
  canEdit?: boolean;
  status?: VoiceServerConfigurationStatus;
  providerMetadata?: Record<string, string>;
  createdAt?: string;
  updatedAt?: string;
  default: boolean;
  global?: boolean;
}

export interface IVoiceServerConfigurationForm {
  name: string;
  provider: string;
  apiKey: string;
  apiSecret: string;
  callerId: string;
  baseUrl: string;
  region: string;
  countryCode: string;
  status: VoiceServerConfigurationStatus;
  default: boolean;
}

export interface IVoiceServerConfigurationListParams {
  offset?: number;
  pageSize?: number;
  clientId?: string;
  searchParam?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface IVoiceServerTestRequest {
  phoneNumber: string;
  scriptBody?: string;
}

export const VOICE_PROVIDER_LABELS: Record<VoiceProvider, string> = {
  [VoiceProvider.TWILIO]: 'Twilio',
  [VoiceProvider.GENERIC_SIP]: 'Generic SIP',
};

export const resolveVoiceProviderLabel = (provider?: string): string => {
  if (!provider) return '—';
  return VOICE_PROVIDER_LABELS[provider as VoiceProvider] ?? provider;
};

export const VOICE_SERVER_CONFIGURATION_STATUS_LABELS: Record<
  VoiceServerConfigurationStatus,
  string
> = {
  [VoiceServerConfigurationStatus.ACTIVE]: 'Active',
  [VoiceServerConfigurationStatus.INACTIVE]: 'Inactive',
};

export const VOICE_COUNTRY_OPTIONS = [
  { value: 'US', label: 'United States' },
  { value: 'BD', label: 'Bangladesh' },
] as const;

export const canEditVoiceServerConfiguration = (
  configuration: Pick<IVoiceServerConfiguration, 'canEdit'>,
): boolean => configuration.canEdit === true;

export const canDeleteVoiceServerConfiguration = (
  configuration: Pick<IVoiceServerConfiguration, 'canEdit' | 'global'>,
): boolean =>
  configuration.canEdit === true && configuration.global !== true;
