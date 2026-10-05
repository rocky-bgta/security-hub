/**
 * Landing Page models and interfaces
 * Based on Task-04 Landing Page Library
 */

import type {
  AIGenerateProviderType,
  IIdName,
} from 'models/EmailTemplate';
import { TemplateStatus } from './Global';

// Enums
export enum LandingPageType {
  LANDING_PAGE = 'LANDING_PAGE',
  PAGE_NOT_FOUND_404 = 'PAGE_NOT_FOUND_404',
  CUSTOM = 'CUSTOM',
}

// Landing page interface
export interface ILandingPage {
  pageId: string;
  name: string;
  description: string;
  status: TemplateStatus;
  pageType: LandingPageType;
  category: IIdName;
  difficultyLevel: IIdName;
  thumbnailUrl: string;
  websiteUrl: string;
  tags: string[];
  captureSubmittedData: boolean;
  popularity: number;
  isGlobal: boolean;
  isPremium: boolean;
  canEdit: boolean;
  canDelete: boolean;
  createdByRole: string;
  createdAt: string;
  updatedAt: string;
  captureFields: string[];
  redirectUrl: string;
  trackingDomainId?: string;
  landingPageBodyPreview: string;
}

// Template-linked landing page option (campaign wizard)
export interface ITemplateLandingPage {
  pageId: string;
  name: string;
  description: string;
  pageType: string;
}

// Preview interface with full HTML content
export interface ILandingPagePreview {
  pageId: string;
  name: string;
  htmlContent: string;
  captureSubmittedData: boolean;
  captureFields: string[];
  redirectUrl?: string;
}

// Update request interface
export interface ILandingPageUpdateForm {
  name: string;
  description?: string;
  status?: 'ACTIVE' | 'INACTIVE' | 'DRAFT';
  category: IIdName;
  difficultyLevel: IIdName;
  htmlContent?: string;
  thumbnailUrl?: string;
  tags?: string[];
  captureSubmittedData?: boolean;
  captureFields?: string[];
  redirectUrl?: string;
  trackingDomainId?: string;
}

// List params interface
export type LandingPageStatusFilter = 'ACTIVE' | 'INACTIVE' | 'DRAFT';

export interface ILandingPageListParams {
  offset?: number;
  pageSize?: number;
  searchParam?: string;
  pageType?: string;
  category?: string;
  difficulty?: string;
  tags?: string[];
  status?: LandingPageStatusFilter;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

// Helper functions for labels
export const getLandingPageStatusLabel = (
  status: LandingPageStatusFilter,
): string => {
  const labels: Record<LandingPageStatusFilter, string> = {
    ACTIVE: 'Active',
    INACTIVE: 'Inactive',
    DRAFT: 'Draft',
  };
  return labels[status] || status;
};

export const getLandingPageTypeLabel = (type: LandingPageType): string => {
  const labels: Record<LandingPageType, string> = {
    [LandingPageType.LANDING_PAGE]: 'Landing Page',
    [LandingPageType.PAGE_NOT_FOUND_404]: '404 Error Page',
    [LandingPageType.CUSTOM]: 'Custom Page',
  };
  return labels[type] || type;
};

// Predefined capture fields
export const CAPTURE_FIELDS = [
  { id: 'username', label: 'Username' },
  { id: 'password', label: 'Password' },
  { id: 'email', label: 'Email Address' },
  { id: 'phone', label: 'Phone Number' },
  { id: 'credit_card', label: 'Credit Card' },
  { id: 'ssn', label: 'SSN/Tax ID' },
  { id: 'address', label: 'Address' },
  { id: 'dob', label: 'Date of Birth' },
];

export type CaptureFieldId = (typeof CAPTURE_FIELDS)[number]['id'];

const FIELD_INPUT_TYPE: Record<string, string> = {
  password: 'password',
  email: 'email',
  phone: 'tel',
  dob: 'date',
};

export function getInputTypeForField(fieldId: string): string {
  return FIELD_INPUT_TYPE[fieldId] ?? 'text';
}

export function generateCaptureFormHtml(selectedFieldIds: string[]): string {
  const fields = CAPTURE_FIELDS.filter(f => selectedFieldIds.includes(f.id));
  if (fields.length === 0) return '';

  const fieldMarkup = fields
    .map(f => {
      const type = getInputTypeForField(f.id);
      return [
        `  <div style="margin-bottom:16px">`,
        `    <label style="display:block;margin-bottom:4px;font-weight:600">${f.label}</label>`,
        `    <input type="${type}" name="${f.id}" placeholder="Enter ${f.label.toLowerCase()}" style="width:100%;padding:8px 12px;border:1px solid #ccc;border-radius:4px;font-size:14px" />`,
        `  </div>`,
      ].join('\n');
    })
    .join('\n\n');

  return [
    `<form style="max-width:480px;margin:0 auto;padding:32px;font-family:sans-serif">`,
    fieldMarkup,
    ``,
    `  <button type="submit" style="width:100%;padding:10px 0;background:#4f46e5;color:#fff;border:none;border-radius:4px;font-size:16px;font-weight:600;cursor:pointer">Submit</button>`,
    `</form>`,
  ].join('\n');
}

// Device preview sizes
export const DEVICE_SIZES = {
  desktop: { width: '100%', label: 'Desktop' },
  tablet: { width: '768px', label: 'Tablet' },
  mobile: { width: '375px', label: 'Mobile' },
} as const;

export type DeviceType = keyof typeof DEVICE_SIZES;

// --- Task-05: Creation Interfaces ---

// Create form interface
export interface ILandingPageCreateForm {
  name: string;
  description: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DRAFT';
  pageType: LandingPageType;
  category: IIdName;
  difficultyLevel: IIdName;
  htmlContent: string;
  thumbnailUrl?: string;
  websiteUrl?: string;
  tags: string[];
  captureSubmittedData: boolean;
  captureFields: string[];
  redirectUrl: string;
  trackingDomainId: string;
}

// Imported website content
export interface IImportedSite {
  htmlContent: string;
  thumbnailUrl?: string;
  originalUrl: string;
  extractedAssets: string[];
  hasLoginForm: boolean;
  detectedFormFields: string[];
  pageTitle?: string;
  metaDescription?: string;
}

/** POST .../landing-pages/fix/html-page — AI edit of selected element HTML */
export interface ILandingPageFixHtmlInput {
  prompt: string;
  elementHtml: string;
  templateCode: string;
}

export interface ILandingPageFixHtmlRequest {
  providerType: AIGenerateProviderType;
  model: string;
  input: ILandingPageFixHtmlInput;
}

// AI generation parameters — matches POST /api/v1/phishing/landing-pages/ai-generate
export interface IAILandingPageParams {
  name: string;
  description?: string;
  pageType: LandingPageType;
  category: IIdName;
  difficultyLevel: IIdName;
  trackingDomainId: string;
  thumbnailUrl?: string;
  tags: string[];
  generationMode: string;
  layoutStyle?: string;
  targetDepartment: IIdName;
  dataCaptureType?: IIdName;
  urgencyLevel: IIdName;
  emotionalTrigger: IIdName;
  voiceInput?: string;
  additionalContext?: string;
  inputLanguage?: string;
  providerType: AIGenerateProviderType;
  model: string;
  generationOptions: {
    tone: IIdName;
    brand?: IIdName;
    contentLength?: string;
    callToAction?: IIdName;
    urgencyLevel?: IIdName;
    emotionalTrigger?: IIdName;
    language?: string;
  };
  htmlContent?: string;
}

export const AI_FORM_OPTIONS = {
  departments: [
    'Finance & Accounts',
    'Information Technology',
    'Human Resources',
    'Sales & Marketing',
    'Customer Success',
  ],

  tones: ['Urgent', 'Friendly', 'Corporate', 'Threatening'],

  urgencyLevels: ['Low', 'Medium', 'High', 'Critical'],

  emotionalTriggers: ['Fear', 'Curiosity', 'Reward', 'Authority'],

  landingPageTypes: [
    'Login Page (O365, Google)',
    'Payment Page',
    'File Download Page',
    'MFA Prompt Page',
    'SSO / Identity Provider (IdP) Page',
    'Session Timeout / Re-authentication Page',
    'Account Recovery / Password Reset Page',
    'Policy Acknowledgment Page',
    'Survey / Feedback Portal',
    'Internal App "Under Maintenance" Page',
    'Document Review / E-Signature Page',
    'Shared Drive / Folder Permission Page',
    'Meeting / Webinar Join Page',
    'SaaS Integration "Approve Permissions" Page',
    'AWS / Azure Management Console',
  ],

  dataCaptureTypes: [
    'Form data (Low)',
    'File upload (Low)',
    'Security Q&A (Low)',
    'Credentials (Medium)',
    'Credentials + OTP (Medium)',
    'OAuth Access (High)',
    'Session Token (High)',
    'Payment Info (Critical)',
  ],

  ctas: [
    'Login to View Documents',
    'Sign in to Continue',
    'Reset Password',
    'Update Password Now',
    'View Document',
    'Review Activity',
    'Check Alert',
    'Join Meeting',
    'Start Call',
    'Access Shared File',
    'Download Invoice',
    'Download Report',
    'Approve Request',
    'Review and Approve',
    'Update Billing Info',
    'Confirm Payment',
    'Claim Reward',
    'Get Bonus',
    'Verify Account',
    'Confirm Identity',
    'Continue with Microsoft',
    'Sign in with Google',
    'Scan QR to Login',
    'Scan for Secure Access',
    'Upload ID',
    'Submit Documents',
    'Enable Security',
    'Disable Restriction',
  ],

  brands: [
    'Microsoft',
    'Google',
    'Amazon',
    'DHL',
    'Bank',
    'PayPal',
    'Internal HR',
    'LinkedIn',
    'Facebook',
    'Cloud Storage',
    'Internal Finance',
    'Payroll Systems',
    'Payment',
    'Email Security',
    'IT Admin Alerts',
    'HR',
    'Finance',
    'E-commerce',
    'Delivery Services',
    'Teams',
    'Subscription Services',
    'Social Media Platforms',
    'Job Portals',
    'Google Drive',
    'Dropbox',
    'Remote Access Portals',
    'Government',
    'Tax',
    'Legal',
    'Salesforce',
    'ChatGPT',
  ],

  believabilityLevels: [
    'Very Low - Awareness training',
    'Low - Beginner testing',
    'Medium - General employees',
    'High - Advanced users',
    'Very High - Executives / targeted',
    'Adaptive - AI-driven campaigns',
    'Red Team - Real-world simulation',
  ],
} as const;

// HTML validation result
export interface IValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
  detectedFormFields: string[];
  hasLoginForm: boolean;
}

// Generation modes
export const GENERATION_MODES = [
  {
    id: 'CLONE_STYLE',
    label: 'Clone Style',
    description: 'Clone styling from a reference website',
  },
  {
    id: 'BRAND_BASED',
    label: 'Brand Based',
    description: 'Generate based on a brand name',
  },
  {
    id: 'FULLY_AI',
    label: 'Fully AI',
    description: 'Let AI create from scratch',
  },
] as const;

// Layout styles for AI generation
export const LAYOUT_STYLES = [
  { id: 'MINIMAL', label: 'Minimal', description: 'Clean, simple design' },
  {
    id: 'CORPORATE',
    label: 'Corporate',
    description: 'Professional business look',
  },
  {
    id: 'MOBILE_FIRST',
    label: 'Mobile First',
    description: 'Optimized for mobile devices',
  },
  { id: 'DARK_MODE', label: 'Dark Mode', description: 'Dark theme design' },
] as const;

// Popular brands for brand-based generation
export const BRAND_OPTIONS = [
  'Microsoft',
  'Google',
  'Apple',
  'Amazon',
  'Facebook',
  'LinkedIn',
  'Twitter',
  'Instagram',
  'Netflix',
  'PayPal',
  'Bank of America',
  'Chase',
  'Wells Fargo',
  'Dropbox',
  'Slack',
] as const;

// Page type descriptions
export const PAGE_TYPE_INFO = {
  [LandingPageType.LANDING_PAGE]: {
    title: 'Landing Page',
    description: 'Standard phishing landing page for capturing credentials',
    icon: 'document',
  },
  [LandingPageType.PAGE_NOT_FOUND_404]: {
    title: '404 Error Page',
    description: 'Simulated error page to reduce suspicion',
    icon: 'exclamation',
  },
  [LandingPageType.CUSTOM]: {
    title: 'Custom Page',
    description: 'Fully custom HTML with complete control',
    icon: 'code',
  },
} as const;
