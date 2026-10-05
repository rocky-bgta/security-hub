import { AI_GENERATE_PROVIDER_TYPES, TemplateType } from 'models/EmailTemplate';
import {
  PHISHING_LINK_TOKEN,
  SMS_MAX_CHARACTERS,
} from 'utils/smsSegments';
import { z } from 'zod';

const idNameSchema = z.object({
  id: z.string().min(1, 'Id is required'),
  name: z.string().min(1, 'Name is required'),
});

const optionalIdNameSchema = z.object({
  id: z.string(),
  name: z.string(),
});

export const emptyIdName = { id: '', name: '' };

const smsTemplateRefine = (
  data: {
    templateType: TemplateType;
    emailBody: string;
    allowMultiPartSms: boolean;
  },
  ctx: z.RefinementCtx,
) => {
  if (data.templateType !== TemplateType.SMS) return;

  if (!data.emailBody.includes(PHISHING_LINK_TOKEN)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: ['emailBody'],
      message: `SMS message must include ${PHISHING_LINK_TOKEN}`,
    });
  }

  if (data.allowMultiPartSms) return;

  // Raw-length safety net; URL-aware limit is enforced in the SMS editor UI
  if (data.emailBody.length > SMS_MAX_CHARACTERS) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: ['emailBody'],
      message: `SMS message cannot exceed ${SMS_MAX_CHARACTERS} characters`,
    });
  }
};

/**
 * Schema for updating an email template
 */
export const EmailTemplateUpdateSchema = z
  .object({
    templateType: z.enum([TemplateType.EMAIL, TemplateType.SMS], {
      error: () => ({ message: 'Template type is required' }),
    }),
    templateName: z
      .string()
      .min(1, 'Template name is required')
      .max(100, 'Template name cannot exceed 100 characters'),
    description: z
      .string()
      .max(500, 'Description cannot exceed 500 characters')
      .optional(),
    status: z.enum(['ACTIVE', 'INACTIVE', 'DRAFT']).optional(),
    emailType: z.enum(['STANDARD_PHISH', 'SPEAR_PHISH'], {
      error: () => ({ message: 'Email type is required' }),
    }),
    payloadType: idNameSchema,
    emailSubject: z
      .string()
      .min(1, 'Email subject is required')
      .max(200, 'Email subject cannot exceed 200 characters'),
    emailBody: z.string().min(1, 'Email body is required'),
    emailBodyText: z.string().optional(),
    difficultyLevel: idNameSchema,
    serviceLocation: z.string().optional(),
    tags: z.array(z.string()).optional(),
    employeeDataRequired: z.array(z.string()).optional(),
    thumbnailUrl: z.string().optional().or(z.literal('')),
    language: z.string().optional(),
    attachments: z.array(z.string()).optional(),
    landingPageIds: z
      .array(z.string())
      .min(1, 'At least one landing page is required'),
    allowMultiPartSms: z.boolean(),
  })
  .superRefine(smsTemplateRefine);

export type TEmailTemplateUpdateForm = z.infer<
  typeof EmailTemplateUpdateSchema
>;

export type TEmailTemplateUpdateApiPayload =
  | Omit<TEmailTemplateUpdateForm, 'allowMultiPartSms'>
  | (Omit<
      TEmailTemplateUpdateForm,
      'emailBody' | 'emailBodyText' | 'allowMultiPartSms'
    > & {
      smsBody: string;
    });

export const DefaultEmailTemplateUpdateValues: TEmailTemplateUpdateForm = {
  templateType: TemplateType.EMAIL,
  templateName: '',
  description: '',
  status: 'DRAFT',
  emailType: 'STANDARD_PHISH',
  payloadType: { id: '', name: '' },
  emailSubject: '',
  emailBody: '',
  emailBodyText: '',
  difficultyLevel: { id: '', name: '' },
  serviceLocation: '',
  tags: [],
  employeeDataRequired: [],
  thumbnailUrl: '',
  language: '',
  attachments: [],
  landingPageIds: [],
  allowMultiPartSms: false,
};

// --- Task-03: Template Creation Schemas ---

/**
 * Schema for creating a new email template manually
 */
export const EmailTemplateCreateSchema = z
  .object({
    templateType: z.enum([TemplateType.EMAIL, TemplateType.SMS], {
      error: () => ({ message: 'Template type is required' }),
    }),
    templateName: z
      .string()
      .min(1, 'Template name is required')
      .max(100, 'Template name cannot exceed 100 characters'),
    description: z.string().max(255, 'Description cannot exceed 255 characters'),
    status: z.enum(['ACTIVE', 'INACTIVE', 'DRAFT']),
    emailType: z.enum(['STANDARD_PHISH', 'SPEAR_PHISH']),
    payloadType: idNameSchema,
    emailSubject: z
      .string()
      .min(1, 'Email subject is required')
      .max(200, 'Email subject cannot exceed 200 characters'),
    emailBody: z.string().min(1, 'Email body is required'),
    emailBodyText: z.string().optional(),
    difficultyLevel: idNameSchema,
    serviceLocation: z.string(),
    tags: z.array(z.string()).max(10, 'Maximum 10 tags allowed'),
    employeeDataRequired: z
      .array(
        z.enum([
          'EMAIL_ADDRESS',
          'FIRST_NAME',
          'LAST_NAME',
          'ORGANIZATION',
          'PHONE_NUMBER',
          'LOCATION',
          'DEPARTMENT',
        ]),
      )
      .min(1, 'At least one employee data field is required'),
    thumbnailUrl: z.string().optional().or(z.literal('')),
    language: z.string(),
    attachments: z.array(z.string()).optional(),
    landingPageIds: z
      .array(z.string())
      .min(1, 'At least one landing page is required'),
    allowMultiPartSms: z.boolean(),
  })
  .superRefine(smsTemplateRefine);

export type TEmailTemplateCreateForm = z.infer<
  typeof EmailTemplateCreateSchema
>;

export type TEmailTemplateCreateApiPayload =
  | Omit<TEmailTemplateCreateForm, 'allowMultiPartSms'>
  | (Omit<
      TEmailTemplateCreateForm,
      'emailBody' | 'emailBodyText' | 'allowMultiPartSms'
    > & {
      smsBody: string;
    });

export const DefaultEmailTemplateCreateValues: TEmailTemplateCreateForm = {
  templateType: TemplateType.EMAIL,
  templateName: '',
  description: '',
  status: 'DRAFT',
  emailType: 'STANDARD_PHISH',
  payloadType: { id: '', name: '' },
  emailSubject: '',
  emailBody: '',
  emailBodyText: '',
  difficultyLevel: { id: '', name: '' },
  serviceLocation: '',
  tags: [],
  employeeDataRequired: [
    'EMAIL_ADDRESS',
    'FIRST_NAME',
    'PHONE_NUMBER',
    'DEPARTMENT',
  ],
  thumbnailUrl: '',
  language: 'en',
  attachments: [],
  landingPageIds: [],
  allowMultiPartSms: false,
};

/**
 * Schema for AI-powered template generation
 */
export const AIGenerateSchema = z
  .object({
    templateName: z
      .string()
      .min(3, 'Template name must be at least 3 characters')
      .max(100, 'Template name cannot exceed 100 characters'),
    description: z.string().optional(),
    payloadType: idNameSchema,
    emailSubject: z
      .string()
      .min(3, 'Email subject must be at least 3 characters')
      .max(200, 'Email subject cannot exceed 200 characters'),
    targetIndustry: optionalIdNameSchema.optional(),
    department: optionalIdNameSchema.optional(),
    attackerPersona: idNameSchema,
    attackTechnique: idNameSchema,
    triggerEvent: idNameSchema,
    expectedUserAction: idNameSchema,
    socialEngineeringStrategy: idNameSchema,
    campaignObjective: idNameSchema,
    voiceInputContent: z.string().optional(),
    additionalContext: z.string().optional(),
    inputLanguage: z.string().min(1, 'Input language is required'),
    generationMode: z.string().min(1, 'Generation mode is required'),
    difficultyLevel: idNameSchema,
    employeeDataRequired: z
      .array(
        z.enum([
          'EMAIL_ADDRESS',
          'FIRST_NAME',
          'LAST_NAME',
          'ORGANIZATION',
          'PHONE_NUMBER',
          'LOCATION',
          'DEPARTMENT',
        ]),
      )
      .min(1, 'At least one employee data field is required')
      .optional(),
    thumbnailUrl: z.string().optional().or(z.literal('')),
    tags: z.array(z.string()).max(10, 'Maximum 10 tags allowed').optional(),
    providerType: z.enum(AI_GENERATE_PROVIDER_TYPES, {
      error: () => ({ message: 'AI provider is required' }),
    }),
    model: z.string().min(1, 'Model is required'),
    generationOptions: z.object({
      tone: idNameSchema,
      language: z.string().optional(),
      constraints: optionalIdNameSchema.optional(),
    }),
    landingPageIds: z
      .array(z.string())
      .min(1, 'At least one landing page is required'),
  })
  .superRefine((data, ctx) => {
    const hasIndustry = Boolean(data.targetIndustry?.id?.trim());
    const hasDepartment = Boolean(data.department?.id?.trim());

    if (!hasIndustry && !hasDepartment) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['targetIndustry', 'id'],
        message: 'Select either target industry or department',
      });
    }

    if (hasIndustry && hasDepartment) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['department', 'id'],
        message:
          'You can select either target industry or department, not both',
      });
    }
  });

export type TAIGenerateForm = z.input<typeof AIGenerateSchema>;

export const DefaultAIGenerateValues: TAIGenerateForm = {
  templateName: '',
  description: '',
  payloadType: emptyIdName,
  emailSubject: '',
  targetIndustry: emptyIdName,
  department: emptyIdName,
  attackerPersona: emptyIdName,
  attackTechnique: emptyIdName,
  triggerEvent: emptyIdName,
  expectedUserAction: emptyIdName,
  socialEngineeringStrategy: emptyIdName,
  campaignObjective: emptyIdName,
  voiceInputContent: '',
  additionalContext: '',
  inputLanguage: 'English',
  generationMode: 'VOICE',
  difficultyLevel: emptyIdName,
  tags: [],
  employeeDataRequired: [
    'EMAIL_ADDRESS',
    'FIRST_NAME',
    'PHONE_NUMBER',
    'DEPARTMENT',
  ],
  thumbnailUrl: '',
  providerType: 'OPENAI',
  model: '',
  generationOptions: {
    tone: emptyIdName,
    language: 'English',
    constraints: emptyIdName,
  },
  landingPageIds: [],
};
