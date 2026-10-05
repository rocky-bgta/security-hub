import { AI_GENERATE_PROVIDER_TYPES } from 'models/EmailTemplate';
import { TemplateStatus } from 'models/Global';
import { LandingPageType } from 'models/LandingPage';
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

/**
 * Zod validation schemas for Landing Page forms
 * Based on Task-04, Task-05 Landing Page Creation
 */

// Update form schema
export const LandingPageUpdateSchema = z.object({
  name: z
    .string()
    .min(3, 'Name must be at least 3 characters')
    .max(100, 'Name cannot exceed 100 characters'),
  description: z
    .string()
    .max(255, 'Description cannot exceed 255 characters')
    .optional()
    .or(z.literal('')),
  status: z.enum(['ACTIVE', 'INACTIVE', 'DRAFT']).optional(),
  category: idNameSchema,
  difficultyLevel: idNameSchema,
  htmlContent: z.string().optional(),
  tags: z.array(z.string()).optional(),
  captureSubmittedData: z.boolean(),
  captureFields: z.array(z.string()),
  thumbnailUrl: z.string().optional().or(z.literal('')),
  redirectUrl: z
    .string()
    .url('Please enter a valid URL')
    .optional()
    .or(z.literal('')),
  trackingDomainId: z.string().min(1, 'Tracking domain is required'),
});

export type TLandingPageUpdateForm = z.infer<typeof LandingPageUpdateSchema>;

// Default values for the form
export const landingPageUpdateDefaultValues: Partial<TLandingPageUpdateForm> = {
  name: '',
  description: '',
  status: 'DRAFT',
  category: emptyIdName,
  difficultyLevel: emptyIdName,
  tags: [],
  captureSubmittedData: false,
  captureFields: [],
  thumbnailUrl: '',
  redirectUrl: '',
  trackingDomainId: '',
};

// --- Task-05: Creation Schemas ---

// Create form schema
export const LandingPageCreateSchema = z.object({
  name: z
    .string()
    .min(3, 'Name must be at least 3 characters')
    .max(100, 'Name cannot exceed 100 characters'),
  description: z
    .string()
    .max(255, 'Description cannot exceed 255 characters')
    .optional()
    .or(z.literal('')),
  status: z.enum(['ACTIVE', 'INACTIVE', 'DRAFT']),
  pageType: z.nativeEnum(LandingPageType, {
    error: () => ({ message: 'Please select a page type' }),
  }),
  category: idNameSchema,
  difficultyLevel: idNameSchema,
  htmlContent: z.string().min(1, 'HTML content is required'),
  thumbnailUrl: z.string().optional().or(z.literal('')),
  websiteUrl: z.string().url().optional().or(z.literal('')),
  tags: z.array(z.string()),
  captureSubmittedData: z.boolean(),
  captureFields: z.array(z.string()),
  redirectUrl: z
    .string()
    .url('Please enter a valid URL')
    .optional()
    .or(z.literal('')),
  trackingDomainId: z.string().min(1, 'Tracking domain is required'),
});

export type TLandingPageCreateForm = z.infer<typeof LandingPageCreateSchema>;

// Default values for create form
export const landingPageCreateDefaultValues: TLandingPageCreateForm = {
  name: '',
  description: '',
  status: TemplateStatus.DRAFT,
  pageType: LandingPageType.LANDING_PAGE,
  category: emptyIdName,
  difficultyLevel: emptyIdName,
  htmlContent: '',
  thumbnailUrl: '',
  websiteUrl: '',
  tags: [],
  captureSubmittedData: false,
  captureFields: [],
  redirectUrl: '',
  trackingDomainId: '',
};

// Import site schema
export const ImportSiteSchema = z.object({
  websiteUrl: z.string().url('Please enter a valid URL'),
  includeAssets: z.boolean(),
});

export type TImportSiteForm = z.infer<typeof ImportSiteSchema>;

// AI generation schema — matches POST /api/v1/phishing/landing-pages/ai-generate
export const AILandingPageSchema = z.object({
  name: z
    .string()
    .min(3, 'Name must be at least 3 characters')
    .max(100, 'Name cannot exceed 100 characters'),
  description: z.string().max(255).optional().or(z.literal('')),
  pageType: z.nativeEnum(LandingPageType),
  category: idNameSchema,
  difficultyLevel: idNameSchema,
  trackingDomainId: z.string().min(1, 'Tracking domain is required'),
  thumbnailUrl: z.string().optional().or(z.literal('')),
  tags: z.array(z.string()),
  generationMode: z.string().min(1, 'Generation mode is required'),
  layoutStyle: z.string().optional().or(z.literal('')),
  targetDepartment: idNameSchema,
  dataCaptureType: optionalIdNameSchema,
  urgencyLevel: idNameSchema,
  emotionalTrigger: idNameSchema,
  voiceInput: z.string().optional().or(z.literal('')),
  additionalContext: z.string().optional(),
  inputLanguage: z.string().optional(),
  providerType: z.enum(AI_GENERATE_PROVIDER_TYPES, {
    error: () => ({ message: 'AI provider is required' }),
  }),
  model: z.string().min(1, 'Model is required'),
  generationOptions: z.object({
    tone: idNameSchema,
    brand: optionalIdNameSchema.optional(),
    contentLength: z.string().optional(),
    callToAction: optionalIdNameSchema.optional(),
    urgencyLevel: optionalIdNameSchema.optional(),
    emotionalTrigger: optionalIdNameSchema.optional(),
    language: z.string().optional(),
  }),
});

export type TAILandingPageForm = z.infer<typeof AILandingPageSchema>;

export const DefaultAILandingPageValues: TAILandingPageForm = {
  name: '',
  description: '',
  pageType: LandingPageType.LANDING_PAGE,
  category: emptyIdName,
  difficultyLevel: emptyIdName,
  trackingDomainId: '',
  thumbnailUrl: '',
  tags: [],
  generationMode: 'FULLY_AI',
  layoutStyle: 'CORPORATE',
  targetDepartment: emptyIdName,
  dataCaptureType: emptyIdName,
  urgencyLevel: emptyIdName,
  emotionalTrigger: emptyIdName,
  voiceInput: '',
  additionalContext: '',
  inputLanguage: 'English',
  providerType: 'OPENAI',
  model: '',
  generationOptions: {
    tone: emptyIdName,
    brand: emptyIdName,
    contentLength: '',
    callToAction: emptyIdName,
    urgencyLevel: emptyIdName,
    emotionalTrigger: emptyIdName,
    language: 'English',
  },
};
