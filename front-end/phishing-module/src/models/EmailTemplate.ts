/**
 * Email Template models and interfaces
 * Based on Task-02 Email Template Library
 */

import { TemplateStatus } from './Global';

/** Configuration option with id and display name (API create/update payload). */
export interface IIdName {
  id: string;
  name: string;
}

/** Normalize API or legacy string values into `{ id, name }`. */
export const toIdName = (
  value: unknown,
  items: IIdName[] = [],
): IIdName => {
  if (
    value &&
    typeof value === 'object' &&
    'id' in value &&
    'name' in value
  ) {
    const v = value as IIdName;
    return { id: String(v.id), name: String(v.name) };
  }
  if (typeof value === 'string' && value) {
    const match = items.find(
      item => item.id === value || item.name === value,
    );
    return match
      ? { id: match.id, name: match.name }
      : { id: value, name: value };
  }
  return { id: '', name: '' };
};

export enum TemplateType {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
}

export interface IEmailTemplate {
  status: TemplateStatus;
  templateId: string;
  templateType?: TemplateType;
  templateName: string;
  description: string;
  emailSubject: string;
  emailBodyPreview: string;
  emailType: EmailType;
  payloadType: IIdName;
  difficultyLevel: IIdName;
  serviceLocation: string;
  tags: string[];
  employeeDataRequired: EmployeeDataField[];
  thumbnailUrl: string;
  language: string;
  popularity: number;
  isGlobal: boolean;
  isPremium: boolean;
  canEdit: boolean;
  canDelete: boolean;
  createdByRole: string;
  createdAt: string;
  updatedAt: string;
  attachments: string[];
  landingPageIds?: string[];
}

export interface IEmailTemplatePreview {
  templateId: string;
  templateName: string;
  templateType?: TemplateType;
  emailSubject: string;
  emailBody: string;
  emailBodyText: string;
  smsBody?: string;
  attachments: string[];
}

export enum EmailType {
  STANDARD_PHISH = 'STANDARD_PHISH',
  SPEAR_PHISH = 'SPEAR_PHISH',
}

export enum PayloadType {
  PHISHING_WEBSITE = 'PHISHING_WEBSITE',
  PHISHING_ATTACHMENT = 'PHISHING_ATTACHMENT',
  QR = 'QR',
  INFORMATION_REQUEST = 'INFORMATION_REQUEST',
  CALLBACK_REQUEST = 'CALLBACK_REQUEST',
  SOCIAL_MEDIA_PHISHING = 'SOCIAL_MEDIA_PHISHING',
  FAKE_SOFTWARE_UPDATE = 'FAKE_SOFTWARE_UPDATE',
  FAKE_PAYMENT_REQUEST = 'FAKE_PAYMENT_REQUEST',
}

export enum DifficultyLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  SPEAR_PHISHING = 'SPEAR_PHISHING',
}

export enum EmployeeDataField {
  EMAIL_ADDRESS = 'EMAIL_ADDRESS',
  FIRST_NAME = 'FIRST_NAME',
  LAST_NAME = 'LAST_NAME',
  DEPARTMENT = 'DEPARTMENT',
  PHONE_NUMBER = 'PHONE_NUMBER',
  ORGANIZATION = 'ORGANIZATION',
  LOCATION = 'LOCATION',
}

export interface IFilterOptions {
  difficultyLevels: string[];
  payloadTypes: string[];
  locations: string[];
  tags: string[];
  languages: string[];
}

export type EmailTemplateStatusFilter = 'ACTIVE' | 'INACTIVE' | 'DRAFT';

/** Applied/draft filters for the email/SMS template library list. */
export interface IEmailTemplateListFilters {
  difficulty?: string;
  payloadType?: string;
  location?: string;
  tags: string[];
  language?: string;
  status?: EmailTemplateStatusFilter;
}

export const EMPTY_EMAIL_TEMPLATE_FILTERS: IEmailTemplateListFilters = {
  tags: [],
};

export interface IEmailTemplateFixHtmlRequest {
  providerType: AIGenerateProviderType;
  model: string;
  prompt: string;
  elementHtml: string;
  templateCode: string;
}

export interface ITemplateListParams {
  offset?: number;
  pageSize?: number;
  searchParam?: string;
  templateType?: TemplateType;
  difficultyLevel?: string;
  payloadType?: string;
  location?: string;
  tags?: string[];
  language?: string;
  status?: EmailTemplateStatusFilter;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface IEmailTemplateListResponse {
  offset: number;
  pageSize: number;
  total: number;
  items: IEmailTemplate[];
}

// Helper functions
export const getEmailTemplateStatusLabel = (
  status: EmailTemplateStatusFilter,
): string => {
  const labels: Record<EmailTemplateStatusFilter, string> = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    DRAFT: 'Draft',
  };
  return labels[status] || status;
};

export const getDifficultyLabel = (level: DifficultyLevel): string => {
  switch (level) {
    case DifficultyLevel.BEGINNER:
      return 'Beginner';
    case DifficultyLevel.INTERMEDIATE:
      return 'Intermediate';
    case DifficultyLevel.ADVANCED:
      return 'Advanced';
    case DifficultyLevel.SPEAR_PHISHING:
      return 'Spear Phishing';
    default:
      return level;
  }
};

export const getDifficultyLevelLabel = getDifficultyLabel;

export const getDifficultyColor = (level: DifficultyLevel): string => {
  switch (level) {
    case DifficultyLevel.BEGINNER:
      return 'bg-yellow-600';
    case DifficultyLevel.INTERMEDIATE:
      return 'bg-green-600';
    case DifficultyLevel.ADVANCED:
      return 'bg-vibrant-red';
    case DifficultyLevel.SPEAR_PHISHING:
      return 'bg-purple-600';
    default:
      return '0';
  }
};

export const getPayloadTypeLabel = (type: PayloadType): string => {
  switch (type) {
    case PayloadType.PHISHING_WEBSITE:
      return 'Website';
    case PayloadType.PHISHING_ATTACHMENT:
      return 'Attachment';
    case PayloadType.QR:
      return 'QR Code';
    case PayloadType.INFORMATION_REQUEST:
      return 'Info Request';
    case PayloadType.CALLBACK_REQUEST:
      return 'Callback';
    case PayloadType.SOCIAL_MEDIA_PHISHING:
      return 'Social Media';
    case PayloadType.FAKE_SOFTWARE_UPDATE:
      return 'Fake Update';
    case PayloadType.FAKE_PAYMENT_REQUEST:
      return 'Fake Payment';
    default:
      return type;
  }
};

export const PREDEFINED_TAGS = [
  'Security',
  'Awareness',
  'Phishing',
  'Training',
  'New Employees',
  'GDPR',
  'Compliance',
];

// --- Task-03: Template Creation Interfaces ---

export interface IEmailTemplateCreateForm {
  templateName: string;
  description: string;
  emailType: EmailType;
  payloadType: PayloadType;
  emailSubject: string;
  emailBody: string;
  emailBodyText: string;
  difficultyLevel: DifficultyLevel;
  serviceLocation: string;
  tags: string[];
  employeeDataRequired: EmployeeDataField[];
  language: string;
}

/** Subset of AI providers allowed for email template AI generation */
export const AI_GENERATE_PROVIDER_TYPES = [
  'OPENAI',
  'GEMINI',
  'CLAUDE',
  'ZAI',
] as const;

export type AIGenerateProviderType =
  (typeof AI_GENERATE_PROVIDER_TYPES)[number];

/** Request body for POST .../email-templates/ai-generate */
export interface IAIGenerateParams {
  templateType?: TemplateType;
  templateName: string;
  description: string;
  payloadType: IIdName;
  emailSubject: string;
  targetIndustry?: IIdName;
  department?: IIdName;
  attackerPersona: IIdName;
  attackTechnique: IIdName;
  triggerEvent: IIdName;
  expectedUserAction: IIdName;
  socialEngineeringStrategy: IIdName;
  campaignObjective: IIdName;
  voiceInputContent: string;
  additionalContext: string;
  inputLanguage: string;
  generationMode: string;
  difficultyLevel: IIdName;
  thumbnailUrl: string;
  tags: string[];
  employeeDataRequired: EmployeeDataField[];
  providerType: AIGenerateProviderType;
  model: string;
  generationOptions: {
    tone: IIdName;
    language?: string;
    constraints?: IIdName;
  };
  landingPageIds: string[];
}

export interface ITranscriptionResult {
  transcribedText: string;
  detectedLanguage: string;
  detectedLanguageName: string;
  confidenceScore: number;
  isAutoDetected: boolean;
  warningMessage?: string;
}

export interface IAIGenerationOptions {
  targetIndustries: string[];
  attackerPersonas: string[];
  socialEngineeringStrategies: string[];
  campaignObjectives: string[];
}

// AI Generation Constants
export const TARGET_INDUSTRIES = [
  'Banking',
  'Healthcare',
  'Technology',
  'Retail',
  'Email Service Providers',
  'Government',
  'Education',
  'Insurance',
  'Telecommunications',
  'Manufacturing',
  'Legal',
  'Real Estate',
];

export const ATTACKER_PERSONAS = [
  'Bank Representative',
  'Tech Support',
  'Government Agency',
  'HR Department',
  'IT Security Team',
  'CEO/Executive',
  'Delivery Service',
  'Tax Authority',
  'Insurance Company',
  'Cloud Service Provider',
  'Lottery/Prize',
  'Family Emergency',
  'Vendor/Supplier',
  'Social Media Platform',
  'Healthcare Provider',
  'Telecom Provider',
];

export const SOCIAL_ENGINEERING_STRATEGIES = [
  'Urgency',
  'Authority',
  'Intimidation',
  'Helpfulness',
  'Curiosity',
  'Fear',
  'Greed',
  'Scarcity',
  'Social Proof',
  'Reciprocity',
  'Commitment',
  'Liking',
  'Trust',
  'Familiarity',
  'Compliance',
];

export const CAMPAIGN_OBJECTIVES = [
  'Credential Harvesting',
  'Malware Distribution',
  'Financial Fraud',
  'Data Exfiltration',
  'Reconnaissance',
  'Account Takeover',
  'Business Email Compromise',
  'Ransomware Delivery',
];

export const AI_TEMPLATE_CAMPAIGN_OBJECTIVES = [
  'Credential Harvest',
  'Link Click Only',
  'Attachment Download',
  'MFA Fatigue Approval',
  'Data Entry / Form Fill',
  'QR Code Scan',
] as const;

export const AI_TEMPLATE_ATTACK_TECHNIQUES = [
  'Link Phishing',
  'Attachment Phishing',
  'OAuth Consent Phish',
  'MFA Push Fatigue',
  'Business Email Compromise',
] as const;

export const AI_TEMPLATE_SOCIAL_ENGINEERING_STRATEGIES = [
  'Urgency / Fear',
  'Authority',
  'Curiosity',
  'Trust / Relationship',
  'Scarcity',
  'Compliance / Policy',
] as const;

export const AI_TEMPLATE_DIFFICULTY_LEVELS = [
  DifficultyLevel.BEGINNER,
  DifficultyLevel.INTERMEDIATE,
  DifficultyLevel.ADVANCED,
  DifficultyLevel.SPEAR_PHISHING,
] as DifficultyLevel[];

export const AI_TEMPLATE_TRIGGER_EVENTS = [
  'Password Expiration',
  'New Document Shared',
  'Invoice Overdue',
  'Security Policy Update',
  'Unusual Login Activity',
] as const;

export const AI_TEMPLATE_ATTACKER_PERSONAS = [
  'Internal IT / Helpdesk',
  'HR / Payroll',
  'Executive',
  'Vendor / Partner',
  'Cloud Service Provider',
] as const;

export const AI_TEMPLATE_EXPECTED_USER_ACTIONS = [
  'Click Link',
  'Click + submit',
  'Download attachment',
  'Submit Credentials',
  'Download File',
  'Approve MFA',
  'Scan QR Code',
] as const;

export const AI_TEMPLATE_DEPARTMENTS = [
  'IT',
  'Finance',
  'Human Resources',
  'Operations',
  'Sales',
  'Legal',
] as const;

export const AI_TEMPLATE_TONES = [
  'Urgent',
  'Friendly',
  'Calm',
  'Corporate',
  'Threatening',
  'Aggressive',
  'Reassuring',
] as const;

export const AI_TEMPLATE_CONSTRAINTS = [
  'Avoid mentioning passwords',
  'Do not reference company name',
  'Limit to 120 words',
  'Avoid emojis',
] as const;

export const SUPPORTED_LANGUAGES: Record<string, string> = {
  en: 'English',
  zh: 'Mandarin Chinese',
  hi: 'Hindi',
  es: 'Spanish',
  ar: 'Arabic',
  fr: 'French',
  bn: 'Bengali',
  pt: 'Portuguese',
  ru: 'Russian',
  ur: 'Urdu',
};

export const getEmployeeDataFieldLabel = (field: EmployeeDataField): string => {
  switch (field) {
    case EmployeeDataField.EMAIL_ADDRESS:
      return 'Email Address';
    case EmployeeDataField.FIRST_NAME:
      return 'First Name';
    case EmployeeDataField.LAST_NAME:
      return 'Last Name';
    case EmployeeDataField.ORGANIZATION:
      return 'Organization';
    case EmployeeDataField.PHONE_NUMBER:
      return 'Phone Number';
    case EmployeeDataField.LOCATION:
      return 'Location';
    case EmployeeDataField.DEPARTMENT:
      return 'Department';
    default:
      return field;
  }
};
