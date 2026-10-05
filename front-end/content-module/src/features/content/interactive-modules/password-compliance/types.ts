export type FrameworkCategory = 'security' | 'privacy' | 'other';

export type CheckStatus = 'unchecked' | 'pass' | 'fail' | 'na';

export type RequirementKey =
  | 'length'
  | 'lowercase'
  | 'uppercase'
  | 'number'
  | 'special'
  | 'mfa'
  | 'change';

export type StrengthLabel = 'None' | 'Weak' | 'Medium' | 'Strong';

export interface IFramework {
  name: string;
  minLength: number;
  requiresLowercase: boolean;
  requiresUppercase: boolean;
  requiresNumber: boolean;
  requiresSpecial: boolean;
  requiresMFA: boolean;
  maxChangeDays: number | null;
  additional: string;
}

export interface IFrameworksCatalog {
  security: Array<IFramework>;
  privacy: Array<IFramework>;
  other: Array<IFramework>;
}

export interface IComplianceInput {
  password: string;
  usesMfa: boolean;
  /** Parsed day value, or null when unset / "never" */
  changeFrequencyDays: number | null;
}

export interface IFrameworkEvaluation {
  isCompliant: boolean;
  checks: Record<RequirementKey, CheckStatus>;
}

export type ChangeFrequencyValue =
  | ''
  | '30'
  | '60'
  | '90'
  | '120'
  | '180'
  | 'never';

export const CHANGE_FREQUENCY_OPTIONS: Array<{
  value: ChangeFrequencyValue;
  label: string;
}> = [
  { value: '', label: 'Select frequency' },
  { value: '30', label: 'Every 30 days' },
  { value: '60', label: 'Every 60 days' },
  { value: '90', label: 'Every 90 days' },
  { value: '120', label: 'Every 120 days' },
  { value: '180', label: 'Every 180 days' },
  { value: 'never', label: 'Never' },
];
