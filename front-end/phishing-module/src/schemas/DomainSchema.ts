import { z } from 'zod';

/** RFC 5321 maximum length for an email address string */
export const EMAIL_ADDRESS_MAX_LENGTH = 254;

export const EMAIL_ADDRESS_MIN_LENGTH = 6;

export const EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE =
  'Email address must be at least 6 characters';

export const EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE = `Email address cannot exceed ${EMAIL_ADDRESS_MAX_LENGTH} characters`;

const domainEmailAddressField = z
  .string()
  .trim()
  .min(1, 'Email address is required')
  .min(EMAIL_ADDRESS_MIN_LENGTH, EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE)
  .max(EMAIL_ADDRESS_MAX_LENGTH, EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE)
  .email('Please enter a valid email address');

/**
 * Schema for generating verification email (Step 1)
 */
export const GenerateVerificationSchema = z.object({
  emailAddress: domainEmailAddressField,
});

export type TGenerateVerificationForm = z.infer<
  typeof GenerateVerificationSchema
>;

export const DefaultGenerateVerificationValues: TGenerateVerificationForm = {
  emailAddress: '',
};

export const DomainAddSchema = z.object({
  domain: z
    .string()
    .trim()
    .min(1, 'Domain is required')
    .min(EMAIL_ADDRESS_MIN_LENGTH, EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE)
    .max(EMAIL_ADDRESS_MAX_LENGTH, EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE),
});

/**
 * Schema for verifying domain with code (Step 2)
 */
export const DomainVerificationSchema = z.object({
  emailAddress: domainEmailAddressField,
  verificationCode: z
    .string()
    .min(1, 'Verification code is required')
    .min(6, 'Verification code must be at least 6 characters')
    .max(8, 'Verification code cannot exceed 8 characters'),
});

export type TDomainAddForm = z.infer<typeof DomainAddSchema>;

export type TDomainVerificationForm = z.infer<typeof DomainVerificationSchema>;

export const DefaultDomainAddValues: TDomainAddForm = {
  domain: '',
};

export const DefaultDomainVerificationValues: TDomainVerificationForm = {
  emailAddress: '',
  verificationCode: '',
};
