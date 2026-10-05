import { z } from 'zod';

import { PROVIDER_CREDENTIAL_CATEGORIES } from 'models/ProviderCredential';

const categoryValues = PROVIDER_CREDENTIAL_CATEGORIES.map(c => c.value) as [
  'VOICE_CLONING',
  'VIDEO_RENDERING',
];

export const ProviderCredentialFormSchema = z
  .object({
    providerName: z.string().min(1, 'Provider is required').max(100),
    category: z.enum(categoryValues, {
      error: () => ({ message: 'Category is required' }),
    }),
    modelName: z.string().max(200),
    apiKey: z.string().max(2000),
    apiSecret: z.string().max(2000),
    baseUrl: z.string().max(500),
    isActive: z.boolean(),
    isDefault: z.boolean(),
  })
  .superRefine((data, ctx) => {
    // apiKey required only on create — validated in the modal with mode flag
    if (data.baseUrl.trim() && !/^https?:\/\/.+/i.test(data.baseUrl.trim())) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['baseUrl'],
        message: 'Enter a valid URL starting with http:// or https://',
      });
    }
  });

export type TProviderCredentialForm = z.infer<
  typeof ProviderCredentialFormSchema
>;

export const defaultProviderCredentialForm: TProviderCredentialForm = {
  providerName: '',
  category: 'VOICE_CLONING',
  modelName: '',
  apiKey: '',
  apiSecret: '',
  baseUrl: '',
  isActive: true,
  isDefault: false,
};
