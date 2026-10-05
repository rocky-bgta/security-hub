import { type ClassValue, clsx } from 'clsx';
import {
  IEmailTemplateListFilters,
  ITemplateListParams,
  TemplateType,
} from 'models/EmailTemplate';
import { IGetListParams, IList } from 'models/Global';
import { twMerge } from 'tailwind-merge';

export const TEMPLATE_LIST_PAGE_SIZE = 12;

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Convert object to query string
 */
export const objectToQueryString = (params: IGetListParams): string => {
  return Object.entries(params)
    .filter(
      ([, value]) => value !== undefined && value !== null && value !== '',
    )
    .map(
      ([key, value]) =>
        `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`,
    )
    .join('&');
};

export const dataUrlToFile = (dataUrl: string, filename: string): File => {
  const [header, base64] = dataUrl.split(',');
  const mime = header.match(/:(.*?);/)?.[1] ?? 'image/jpeg';
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return new File([bytes], filename, { type: mime });
};
export function buildTemplateListParams(options: {
  templateType: TemplateType;
  searchParam?: string;
  applied: IEmailTemplateListFilters;
  offset?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}): ITemplateListParams {
  const {
    templateType,
    searchParam,
    applied,
    offset = 0,
    pageSize = TEMPLATE_LIST_PAGE_SIZE,
    sortBy = 'createdAt',
    sortOrder = 'desc',
  } = options;

  const params: ITemplateListParams = {
    offset,
    pageSize,
    searchParam: searchParam ?? '',
    templateType,
    sortBy,
    sortOrder,
  };

  if (applied.difficulty) params.difficultyLevel = applied.difficulty;
  if (applied.payloadType) params.payloadType = applied.payloadType;
  if (applied.location) params.location = applied.location;
  if (applied.language) params.language = applied.language;
  if (applied.status) params.status = applied.status;
  if (applied.tags.length > 0) params.tags = applied.tags;

  return params;
}

function normalizeTemplateListParams(
  params: ITemplateListParams,
): Record<string, string | number | string[]> {
  const normalized: Record<string, string | number | string[]> = {};
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue;
    if (Array.isArray(value) && value.length === 0) continue;
    normalized[key] = value;
  }
  return normalized;
}

export function areTemplateListParamsEqual(
  a: ITemplateListParams,
  b: ITemplateListParams,
): boolean {
  const normA = normalizeTemplateListParams(a);
  const normB = normalizeTemplateListParams(b);
  const keysA = Object.keys(normA).sort();
  const keysB = Object.keys(normB).sort();
  if (keysA.length !== keysB.length) return false;
  return keysA.every((key, index) => {
    if (key !== keysB[index]) return false;
    const valA = normA[key];
    const valB = normB[key];
    if (Array.isArray(valA) && Array.isArray(valB)) {
      return valA.length === valB.length && valA.every((v, i) => v === valB[i]);
    }
    return valA === valB;
  });
}

/**
 * Format date to human readable format
 */
export const formatDate = (dateString: string | null): string => {
  if (!dateString) return 'N/A';

  const safeDateString =
    dateString.endsWith('Z') || dateString.includes('+')
      ? dateString
      : dateString + 'Z';
  const utcDate = new Date(safeDateString);
  if (isNaN(utcDate.getTime())) return 'N/A';

  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

  return new Intl.DateTimeFormat('en-US', {
    timeZone: userTimeZone,
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(utcDate);
};

/**
 * Format date to relative time (e.g., "2 hours ago")
 */
export const formatRelativeTime = (dateString: string | null): string => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);
  const diffMinutes = Math.floor(diffSeconds / 60);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSeconds < 60) return 'Just now';
  if (diffMinutes < 60)
    return `${diffMinutes} minute${diffMinutes > 1 ? 's' : ''} ago`;
  if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
  if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

  return formatDate(dateString);
};

/**
 * Extract domain from email address
 */
export const extractDomainFromEmail = (email: string): string => {
  if (!email || !email.includes('@')) return '';
  return email.split('@')[1];
};

/**
 * Debounce function
 */
export const debounce = <T extends (...args: unknown[]) => unknown>(
  func: T,
  wait: number,
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout> | null = null;

  return (...args: Parameters<T>) => {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
};

export const isSuccessResponse = (statusCode: number) => {
  return statusCode >= 200 && statusCode < 300;
};

export const humanizeText = (value?: string): string => {
  if (!value) return '-';

  return value
    .replace(/[_-]+/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, char => char.toUpperCase())
    .trim();
};

export interface ITimezoneLookupOption {
  id: string;
  timezoneId: string;
  displayName: string;
}

const pad2 = (value: number): string => String(value).padStart(2, '0');

const toSafeUtcDate = (iso: string): Date | null => {
  if (!iso) return null;

  const safeDateString =
    iso.endsWith('Z') || iso.includes('+') || /-\d{2}:\d{2}$/.test(iso)
      ? iso
      : `${iso}Z`;
  const date = new Date(safeDateString);
  return isNaN(date.getTime()) ? null : date;
};

/**
 * Convert an ISO UTC string to `datetime-local` format (`YYYY-MM-DDTHH:mm`).
 */
export const isoToDatetimeLocal = (
  iso: string,
  ianaTimeZone?: string,
): string => {
  if (!iso) return '';

  const date = toSafeUtcDate(iso);
  if (!date) return iso.slice(0, 16);

  const timeZone =
    ianaTimeZone || Intl.DateTimeFormat().resolvedOptions().timeZone;

  try {
    const parts = new Intl.DateTimeFormat('en-CA', {
      timeZone,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).formatToParts(date);

    const get = (type: Intl.DateTimeFormatPartTypes) =>
      parts.find(part => part.type === type)?.value ?? '';

    let hour = get('hour');
    if (hour === '24') hour = '00';

    return `${get('year')}-${get('month')}-${get('day')}T${hour}:${get('minute')}`;
  } catch {
    return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}T${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
  }
};

/**
 * Convert `datetime-local` value to ISO UTC using the selected IANA timezone.
 */
export const datetimeLocalToIso = (
  local: string,
  ianaTimeZone?: string,
): string => {
  if (!local) return '';

  if (!ianaTimeZone) {
    const date = new Date(local);
    return isNaN(date.getTime()) ? local : date.toISOString();
  }

  const [datePart, timePart] = local.split('T');
  if (!datePart || !timePart) return local;

  const [year, month, day] = datePart.split('-').map(Number);
  const [hour, minute] = timePart.split(':').map(Number);
  if ([year, month, day, hour, minute].some(Number.isNaN)) return local;

  try {
    let guess = Date.UTC(year, month - 1, day, hour, minute, 0);

    for (let attempt = 0; attempt < 5; attempt += 1) {
      const shown = isoToDatetimeLocal(
        new Date(guess).toISOString(),
        ianaTimeZone,
      );
      if (shown === local) {
        return new Date(guess).toISOString();
      }

      const [shownDate, shownTime] = shown.split('T');
      const [, , shownDay] = (shownDate ?? '').split('-').map(Number);
      const [shownHour, shownMinute] = (shownTime ?? '').split(':').map(Number);
      const diffMinutes =
        (day - shownDay) * 24 * 60 +
        (hour - shownHour) * 60 +
        (minute - shownMinute);

      guess += diffMinutes * 60 * 1000;
    }

    return new Date(guess).toISOString();
  } catch {
    const date = new Date(local);
    return isNaN(date.getTime()) ? local : date.toISOString();
  }
};

/**
 * Resolve a stored timezone string (id, display name, or IANA id) to dropdown id.
 */
export const resolveTimezoneId = (
  stored: string,
  options: ITimezoneLookupOption[],
): string => {
  if (!stored || options.length === 0) return stored;

  const normalized = stored.trim().toLowerCase();

  const byId = options.find(option => option.id === stored);
  if (byId) return byId.id;

  const byDisplayName = options.find(
    option => option.displayName.toLowerCase() === normalized,
  );
  if (byDisplayName) return byDisplayName.id;

  const byTimezoneId = options.find(
    option => option.timezoneId.toLowerCase() === normalized,
  );
  if (byTimezoneId) return byTimezoneId.id;

  const partialMatch = options.find(
    option =>
      option.displayName.toLowerCase().includes(normalized) ||
      normalized.includes(option.displayName.toLowerCase()),
  );
  if (partialMatch) return partialMatch.id;

  return stored;
};

export const isIsoDateTimeString = (value?: string): boolean =>
  !!value &&
  (value.endsWith('Z') ||
    value.includes('+') ||
    /-\d{2}:\d{2}$/.test(value) ||
    /T\d{2}:\d{2}:\d{2}/.test(value));

export const formateDateAndTime = (dateString?: string): string => {
  if (!dateString) return 'N/A';

  const safeDateString =
    dateString.endsWith('Z') || dateString.includes('+')
      ? dateString
      : dateString + 'Z';

  const date = new Date(safeDateString);
  if (isNaN(date.getTime())) return 'N/A';

  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

  return new Intl.DateTimeFormat('en-US', {
    timeZone: userTimeZone,
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
};

export const isFileAccepted = (file: File, accept: string): boolean => {
  if (!accept || accept.trim() === '*' || accept.trim() === '*/*') {
    return true;
  }

  const fileType = file.type.toLowerCase();
  const fileName = file.name.toLowerCase();

  return accept.split(',').some(rawToken => {
    const token = rawToken.trim().toLowerCase();
    if (!token || token === '*' || token === '*/*') return true;

    if (token.startsWith('.')) {
      return fileName.endsWith(token);
    }

    if (token.endsWith('/*')) {
      const category = token.slice(0, -2);
      return fileType.startsWith(`${category}/`);
    }

    if (token.includes('/')) {
      return fileType === token;
    }

    return fileName.endsWith(`.${token}`);
  });
};

export const getAcceptDescription = (accept: string): string => {
  if (!accept || accept.trim() === '*' || accept.trim() === '*/*') {
    return 'all file types';
  }

  return accept
    .split(',')
    .map(token => token.trim())
    .filter(Boolean)
    .join(', ');
};

/** Saves a downloaded file blob to disk under the given filename. */
export const downloadBlob = (blob: Blob, filename: string): void => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

/**
 * Most services wrap payloads in `{ message, statusCode, data }`, but a few
 * return the payload at the top level. Accepts either shape.
 */
export const unwrapResponseData = <T>(response: unknown): T | undefined => {
  if (!response || typeof response !== 'object') return undefined;
  const body = response as Record<string, unknown>;
  if ('data' in body) return body.data as T | undefined;
  return response as T;
};

type PaginatedPayload<T> = {
  data?: T[] | PaginatedPayload<T>;
  items?: T[];
  total?: number;
  offset?: number;
  pageSize?: number;
};

export const normalizePaginatedList = <T>(
  response: PaginatedPayload<T> | IList<T> | T[] | null | undefined,
  fallback: IList<T> = { offset: 0, pageSize: 0, total: 0, items: [] },
): IList<T> => {
  if (!response) return fallback;

  // Some endpoints return the rows directly instead of a paginated envelope.
  if (Array.isArray(response)) {
    return {
      offset: 0,
      pageSize: response.length,
      total: response.length,
      items: response,
    };
  }

  const payload = response as PaginatedPayload<T>;

  const withPagination = (rows: T[]): IList<T> => ({
    offset: payload.offset ?? 0,
    pageSize: payload.pageSize ?? rows.length,
    total: payload.total ?? rows.length,
    items: rows,
  });

  if (Array.isArray(payload.items)) return withPagination(payload.items);
  if (Array.isArray(payload.data)) return withPagination(payload.data);
  if (payload.data) return normalizePaginatedList<T>(payload.data, fallback);

  return fallback;
};

