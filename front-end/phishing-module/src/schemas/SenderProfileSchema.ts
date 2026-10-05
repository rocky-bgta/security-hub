import {
  DomainType,
  InterfaceType,
  ProviderType,
} from 'models/SenderProfile';
import { z } from 'zod';

const optionalIdNameSchema = z.object({
  id: z.string(),
  name: z.string(),
});

export const emptyIdName = { id: '', name: '' };

/**
 * Zod validation schemas for Sender Profile forms
 * Based on Task-06 Sender Profile Management
 */

/** Max length for profile name (align with UI `maxLength` on the input). */
export const PROFILE_NAME_MAX_LENGTH = 100;

/** Same text as Zod `.max()` so the UI can show it when input is capped at max. */
export const PROFILE_NAME_MAX_ERROR_MESSAGE = `Profile name cannot exceed ${PROFILE_NAME_MAX_LENGTH} characters`;

export const DISPLAY_NAME_MAX_LENGTH = 100;
export const DISPLAY_NAME_MAX_ERROR_MESSAGE = `Display name cannot exceed ${DISPLAY_NAME_MAX_LENGTH} characters`;

const optionalEnumField = <TEnum extends Record<string, string | number>>(
  enumObject: TEnum,
) => z.union([z.literal(''), z.enum(enumObject)]);

const optionalEmailField = z.union([
  z.literal(''),
  z.email('Please enter a valid email address'),
]);

const senderProfileShape = {
  profileName: z
    .string()
    .min(1, 'Profile name is required')
    .max(PROFILE_NAME_MAX_LENGTH, PROFILE_NAME_MAX_ERROR_MESSAGE),
  interfaceType: z.enum(InterfaceType, 'Please select an interface type'),
  fromAddress: z
    .email('Please enter a valid email address')
    .min(1, 'From address is required'),
  displayName: z
    .string()
    .max(DISPLAY_NAME_MAX_LENGTH, DISPLAY_NAME_MAX_ERROR_MESSAGE),
  replyToAddress: optionalEmailField,
  host: z.string().min(1, 'SMTP host is required'),
  port: z
    .number({ error: 'Port must be a number' })
    .min(1, 'Port must be between 1 and 65535')
    .max(65535, 'Port must be between 1 and 65535'),
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
  category: z.string(),
  targetIndustryId: z.string(),
  regionId: z.string(),
  language: z.string(),
  deceptionLevel: optionalIdNameSchema,
  psychologicalTriggers: z.array(z.string()),
  domainType: optionalEnumField(DomainType),
  domainName: z.string(),
  personalizationLevel: optionalIdNameSchema,
  providerType: optionalEnumField(ProviderType),
  tags: z.array(z.string()),
  ignoreCertificateErrors: z.boolean(),
  useTls: z.boolean(),
};

const senderProfileBaseSchema = z.object({
  ...senderProfileShape,
  username: z.string(),
  password: z.string(),
});

const validateProviderCredentials = (
  data: z.infer<typeof senderProfileBaseSchema>,
  ctx: z.RefinementCtx,
) => {
  if (data.providerType === ProviderType.AWS_SES) {
    return;
  }

  if (!data.username.trim()) {
    ctx.addIssue({
      code: 'custom',
      path: ['username'],
      message: 'Username is required',
    });
  }

  if (!data.password.trim()) {
    ctx.addIssue({
      code: 'custom',
      path: ['password'],
      message: 'Password is required',
    });
  }
};

// Create/Update form schema
export const SenderProfileSchema = senderProfileBaseSchema.superRefine(
  validateProviderCredentials,
);

export type TSenderProfileForm = z.infer<typeof SenderProfileSchema>;

// Default values for create form
export const senderProfileDefaultValues: TSenderProfileForm = {
  profileName: '',
  interfaceType: InterfaceType.SMTP,
  fromAddress: '',
  displayName: '',
  replyToAddress: '',
  host: '',
  port: 587,
  username: '',
  password: '',
  category: '',
  targetIndustryId: '',
  regionId: '',
  language: '',
  deceptionLevel: emptyIdName,
  psychologicalTriggers: [],
  domainType: '',
  domainName: '',
  personalizationLevel: emptyIdName,
  providerType: '',
  tags: [],
  ignoreCertificateErrors: false,
  useTls: true,
};

// Update form schema shares the same DTO requirements as create/test-new.
export const SenderProfileUpdateSchema = senderProfileBaseSchema.superRefine(
  validateProviderCredentials,
);

export type TSenderProfileUpdateForm = z.infer<
  typeof SenderProfileUpdateSchema
>;
