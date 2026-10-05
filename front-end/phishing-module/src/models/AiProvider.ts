/**
 * AI API provider configuration (phishing module)
 */
export enum AIProviderType {
  GEMINI = 'GEMINI',
  OPENAI = 'OPENAI',
  CLAUDE = 'CLAUDE',
  ZAI = 'ZAI',
  ANTHROPIC = 'ANTHROPIC',
}

export interface IAIProviderConfigRequest {
  providerType: AIProviderType;
  apiKey: string;
  apiSecret: string;
  description: string;
}

export interface IAIProviderConfigItem {
  id: string;
  clientAdminId: string;
  providerType: AIProviderType;
  secretName: string;
  description: string;
  apiKeyParameterName: string;
  apiSecretParameterName: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface IAIModelRequest {
  name: string;
  providerType: AIProviderType;
  default: boolean;
  active: boolean;
}

export interface IAIModelItem {
  id: string;
  name: string;
  providerType: AIProviderType;
  createdAt: string;
  default: boolean;
  active: boolean;
}
