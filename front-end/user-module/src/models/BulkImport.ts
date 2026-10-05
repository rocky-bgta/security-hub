import { IList } from 'models/Global';

export type BulkImportView =
  | 'upload'
  | 'summary'
  | 'review'
  | 'edit'
  | 'processing'
  | 'complete';

export type BulkImportUserStatus = 'VALID' | 'INVALID';

export type BulkImportFailureReason =
  | 'invalid_email_format'
  | 'invalid_domain'
  | 'duplicate_email'
  | 'email_already_exists'
  | 'missing_required_field'
  | 'creation_failed';

export const BULK_IMPORT_FAILURE_REASON_LABELS: Record<
  BulkImportFailureReason,
  string
> = {
  invalid_email_format: 'Invalid email format',
  invalid_domain: 'Email domain must match company domain',
  duplicate_email: 'Duplicate email',
  email_already_exists: 'Email already exists',
  missing_required_field: 'Missing required field',
  creation_failed: 'Failed to create user',
};

export interface IBulkImportUser {
  rowIndex: number;
  firstName: string;
  lastName?: string;
  fullName?: string;
  email: string;
  phoneNumber: string;
  countryCode?: string;
  department?: string;
  failureReason?: BulkImportFailureReason | string;
}

export interface IBulkImportUserDraft {
  fullName: string;
  email: string;
  phoneNumber: string;
  department: string;
}

export interface IBulkImportUpdateUser {
  rowIndex: number;
  firstName: string;
  lastName?: string;
  email: string;
  phoneNumber: string;
  countryCode?: string;
  department?: string;
}

export interface IBulkImportSession {
  importSessionId: string;
  totalValid: number;
  totalInvalid: number;
  validUsers: IList<IBulkImportUser>;
  invalidUsers: IList<IBulkImportUser>;
}

export interface IBulkImportFailedUser {
  email: string;
  fullName: string;
  reason: string;
}

export interface IBulkImportOnboardResult {
  totalUsers: number;
  successful: number;
  failed: number;
  failedUsers: Array<IBulkImportFailedUser>;
}
