import {
  BULK_IMPORT_FAILURE_REASON_LABELS,
  BulkImportFailureReason,
  IBulkImportFailedUser,
  IBulkImportUser,
  IBulkImportUserDraft,
  IBulkImportUpdateUser,
} from 'models/BulkImport';
import { IList } from 'models/Global';
import { DEFAULT_PAGINATION_LIMIT } from 'utils/Constants';

export const BULK_IMPORT_MAX_BYTES = 15 * 1024 * 1024;
export const BULK_IMPORT_ACCEPT =
  '.csv,.xls,.xlsx,text/csv,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
export const BULK_IMPORT_EXTENSIONS = ['.csv', '.xls', '.xlsx'];
export const BULK_IMPORT_PAGE_SIZE = DEFAULT_PAGINATION_LIMIT;

const SESSION_ERROR_SNIPPETS = ['expired', 'no longer available', 'not found'];

export const getBulkImportFileError = (
  file: File | null | undefined,
): string => {
  if (!file) return 'Please select a file to import.';
  const ext = file.name.slice(file.name.lastIndexOf('.')).toLowerCase();
  if (!BULK_IMPORT_EXTENSIONS.includes(ext)) {
    return 'Only CSV and Excel files (.csv, .xls, .xlsx) are allowed.';
  }
  if (file.size > BULK_IMPORT_MAX_BYTES) {
    return 'File is too large. Maximum size is 15 MB.';
  }
  return '';
};

export const createEmptyUserPage = (
  pageSize = BULK_IMPORT_PAGE_SIZE,
): IList<IBulkImportUser> => ({
  offset: 0,
  pageSize,
  total: 0,
  items: [],
});

export const normalizeUserPage = (
  page?: IList<IBulkImportUser> | null,
): IList<IBulkImportUser> => ({
  offset: page?.offset ?? 0,
  pageSize: page?.pageSize ?? BULK_IMPORT_PAGE_SIZE,
  total: page?.total ?? 0,
  items: page?.items ?? [],
});

export const getDisplayName = (user: IBulkImportUser): string => {
  if (user.fullName?.trim()) return user.fullName.trim();
  return [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
};

export const splitFullName = (
  fullName: string,
): { firstName: string; lastName: string } => {
  const trimmed = fullName.trim();
  const spaceIndex = trimmed.indexOf(' ');
  if (spaceIndex === -1) {
    return { firstName: trimmed, lastName: '' };
  }
  return {
    firstName: trimmed.slice(0, spaceIndex),
    lastName: trimmed.slice(spaceIndex + 1).trim(),
  };
};

export const createDraftFromUser = (
  user: IBulkImportUser,
): IBulkImportUserDraft => ({
  fullName: getDisplayName(user),
  email: user.email || '',
  phoneNumber: user.phoneNumber || '',
  department: user.department || '',
});

export const isDraftDirty = (
  original: IBulkImportUser,
  draft: IBulkImportUserDraft,
): boolean => {
  return (
    draft.fullName.trim() !== getDisplayName(original) ||
    draft.email.trim() !== (original.email || '') ||
    draft.phoneNumber.trim() !== (original.phoneNumber || '') ||
    (draft.department || '').trim() !== (original.department || '').trim()
  );
};

export const toUpdatePayload = (
  original: IBulkImportUser,
  draft: IBulkImportUserDraft,
): IBulkImportUpdateUser => {
  const names =
    draft.fullName.trim() === getDisplayName(original)
      ? {
          firstName: original.firstName,
          lastName: original.lastName || '',
        }
      : splitFullName(draft.fullName);

  return {
    rowIndex: original.rowIndex,
    firstName: names.firstName,
    lastName: names.lastName,
    email: draft.email.trim(),
    phoneNumber: draft.phoneNumber.trim(),
    countryCode: original.countryCode,
    department: draft.department?.trim() || undefined,
  };
};

export const getFailureReasonLabel = (code?: string | null): string => {
  if (!code) return 'Invalid';
  return (
    BULK_IMPORT_FAILURE_REASON_LABELS[code as BulkImportFailureReason] ?? code
  );
};

export const isBulkImportSessionError = (
  statusCode: number,
  message?: string,
): boolean => {
  if (statusCode === 404) return true;
  const lower = (message || '').toLowerCase();
  return (
    statusCode === 400 &&
    SESSION_ERROR_SNIPPETS.some(snippet => lower.includes(snippet))
  );
};

export const groupFailedUsersByReason = (
  failedUsers: Array<IBulkImportFailedUser> = [],
): Array<{ count: number; label: string }> => {
  const counts = new Map<string, number>();
  for (const user of failedUsers) {
    const key = user.reason || 'creation_failed';
    counts.set(key, (counts.get(key) || 0) + 1);
  }
  return Array.from(counts.entries()).map(([reason, count]) => ({
    count,
    label: getFailureReasonLabel(reason),
  }));
};
