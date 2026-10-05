import { z } from 'zod';

import { AIProviderType } from 'models/AiProvider';

export const AIProviderConfigFormSchema = z.object({
  providerType: z.nativeEnum(AIProviderType),
  apiKey: z.string().min(1, 'API key is required'),
  apiSecret: z.string(),
  description: z.string(),
});

export type TAIProviderConfigForm = z.infer<typeof AIProviderConfigFormSchema>;

export const defaultAIProviderConfigForm: TAIProviderConfigForm = {
  providerType: AIProviderType.GEMINI,
  apiKey: '',
  apiSecret: '',
  description: '',
};
