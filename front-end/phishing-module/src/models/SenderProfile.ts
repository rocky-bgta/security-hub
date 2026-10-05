/**
 * Sender Profile models and interfaces
 * Based on Task-06 Sender Profile Management
 */

import type { IIdName } from 'models/EmailTemplate';

// Enums
export enum InterfaceType {
  SMTP = 'SMTP',
}

export enum ProfileType {
  MANAGED = 'MANAGED',
  CUSTOM = 'CUSTOM',
}

export enum DeceptionLevel {
  BASIC = 'BASIC',
  MEDIUM = 'MEDIUM',
  ADVANCED = 'ADVANCED',
  APT = 'APT',
}

export enum DomainType {
  LOOKALIKE_DOMAIN = 'LOOKALIKE_DOMAIN',
  SPOOFED_DOMAIN = 'SPOOFED_DOMAIN',
}

export enum PersonalizationLevel {
  GENERIC = 'GENERIC',
  HIGHLY_TARGETED = 'HIGHLY_TARGETED',
}

export enum ProviderType {
  AWS_SES = 'AWS_SES',
  OTHER = 'OTHER',
}

// Main sender profile interface
export interface ISenderProfile {
  profileId: string;
  profileName: string;
  interfaceType: InterfaceType;
  fromAddress: string;
  displayName: string;
  replyToAddress?: string;
  host: string;
  port: number;
  username: string;
  category?: string;
  targetIndustryId?: string;
  regionId?: string;
  language?: string;
  deceptionLevel?: IIdName;
  psychologicalTriggers?: string[];
  domainType?: DomainType;
  domainName?: string;
  personalizationLevel?: IIdName;
  providerType?: ProviderType;
  tags?: string[];
  ignoreCertificateErrors: boolean;
  useTls: boolean;
  profileType: ProfileType;
  verified: boolean;
  lastTestedAt: string | null;
  lastTestResult: string | null;
  canEdit: boolean;
  canDelete: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
}

// Form interface for create/update
export interface ISenderProfileForm {
  profileName: string;
  interfaceType: InterfaceType;
  fromAddress: string;
  displayName: string;
  replyToAddress?: string;
  host: string;
  port: number;
  username: string;
  password: string;
  category?: string;
  targetIndustryId?: string;
  regionId?: string;
  language?: string;
  deceptionLevel?: IIdName;
  psychologicalTriggers?: string[];
  domainType?: DomainType;
  domainName?: string;
  personalizationLevel?: IIdName;
  providerType?: ProviderType;
  tags?: string[];
  ignoreCertificateErrors: boolean;
  useTls: boolean;
}

// Test result interface
export interface ITestResult {
  success: boolean;
  message: string;
  responseTimeMs: number;
  serverResponse: string | null;
}

// List params interface
export interface ISenderProfileListParams {
  offset?: number;
  pageSize?: number;
  searchParam?: string;
  profileType?: ProfileType;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

/** Row-level error from bulk sender profile import */
export interface ISenderProfileImportRowError {
  rowNumber: number;
  profileName: string;
  message: string;
}

/** Data payload from `SENDER_PROFILE_IMPORT` */
export interface ISenderProfileImportData {
  totalRows: number;
  successCount: number;
  failedCount: number;
  importedProfiles: ISenderProfile[];
  errors: ISenderProfileImportRowError[];
}

// Helper functions
export const getInterfaceTypeLabel = (type: InterfaceType): string => {
  const labels: Record<InterfaceType, string> = {
    [InterfaceType.SMTP]: 'SMTP',
  };
  return labels[type] || type;
};

export const getProfileTypeLabel = (type: ProfileType): string => {
  const labels: Record<ProfileType, string> = {
    [ProfileType.MANAGED]: 'Managed',
    [ProfileType.CUSTOM]: 'Custom',
  };
  return labels[type] || type;
};

export const getProfileTypeColor = (type: ProfileType): string => {
  const colors: Record<ProfileType, string> = {
    [ProfileType.MANAGED]: 'bg-purple-100 text-purple-800',
    [ProfileType.CUSTOM]: 'bg-blue-100 text-blue-800',
  };
  return colors[type] || 'bg-gray-100 text-gray-800';
};

// Common SMTP ports
export const COMMON_SMTP_PORTS = [
  { port: 25, label: '25 (SMTP)', description: 'Standard SMTP (unencrypted)' },
  { port: 465, label: '465 (SMTPS)', description: 'SMTP over SSL/TLS' },
  {
    port: 587,
    label: '587 (Submission)',
    description: 'SMTP with STARTTLS (recommended)',
  },
  {
    port: 2525,
    label: '2525 (Alternative)',
    description: 'Alternative SMTP port',
  },
];

// Get verification status info
export const getVerificationStatus = (
  profile: ISenderProfile,
): {
  label: string;
  color: string;
  icon: 'success' | 'failed' | 'pending';
} => {
  if (profile.verified) {
    return { label: 'Verified', color: 'text-green-600', icon: 'success' };
  }
  if (profile.lastTestResult?.startsWith('FAILED')) {
    return { label: 'Failed', color: 'text-red-600', icon: 'failed' };
  }
  return { label: 'Not Tested', color: 'text-gray-400', icon: 'pending' };
};
