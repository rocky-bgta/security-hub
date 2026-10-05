/**
 * Third-party deepfake provider credentials (ElevenLabs, Fish Audio, HeyGen).
 * @see Provider credentials — /api/v1/phishing/provider-credentials
 */

export type ProviderCredentialCategory = 'VOICE_CLONING' | 'VIDEO_RENDERING';

export const PROVIDER_CREDENTIAL_CATEGORIES: {
  value: ProviderCredentialCategory;
  label: string;
}[] = [
  { value: 'VOICE_CLONING', label: 'Voice cloning' },
  { value: 'VIDEO_RENDERING', label: 'Video rendering' },
];

export interface IProviderCredential {
  id: string;
  providerName: string;
  category: ProviderCredentialCategory;
  modelName?: string;
  apiKeyLast4?: string;
  hasApiSecret?: boolean;
  baseUrl?: string;
  isActive: boolean;
  isDefault: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface IProviderCredentialListParams {
  providerName?: string;
  isActive?: boolean;
  offset?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface IProviderCredentialCreateRequest {
  providerName: string;
  category: ProviderCredentialCategory;
  modelName?: string;
  apiKey: string;
  apiSecret?: string;
  baseUrl?: string;
  isActive?: boolean;
  isDefault?: boolean;
}

export interface IProviderCredentialUpdateRequest {
  providerName: string;
  category: ProviderCredentialCategory;
  modelName?: string;
  apiKey?: string;
  apiSecret?: string;
  baseUrl?: string;
  isActive?: boolean;
  isDefault?: boolean;
}

/** Item from GET /api/v1/deepfake/video-render-providers */
export interface IVideoRenderProvider {
  provider: string;
  displayName: string;
}
