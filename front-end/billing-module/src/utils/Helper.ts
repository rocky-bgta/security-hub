import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

import { IGetListParams } from 'models/Global';
import { ALPHANUMERIC_PATTERN } from 'utils/Constants';

export const cn = (...inputs: Array<ClassValue>) => {
  return twMerge(clsx(inputs));
};

export const objectToQueryString = (params: IGetListParams): string => {
  const queryString: string[] = [];

  for (const param in params) {
    if (Object.prototype.hasOwnProperty.call(params, param)) {
      const value = params[param as keyof IGetListParams];
      if (value !== undefined && value !== null && value !== '') {
        queryString.push(
          `${encodeURIComponent(param)}=${encodeURIComponent(String(value))}`,
        );
      }
    }
  }

  return queryString.join('&');
};

export const nullOrUndefined = (val: unknown) =>
  val === null || val === undefined;

export const isNumber = (val: unknown): val is number =>
  !isNaN(val as number) && isFinite(val as number) && typeof val === 'number';

export const isObject = (checkObj: unknown): checkObj is object =>
  checkObj !== null &&
  typeof checkObj === 'object' &&
  Object.keys(checkObj).length > 0;

export const isAlphaNumeric = (val: string): boolean =>
  ALPHANUMERIC_PATTERN.test(val);

export const isEmpty = (data: unknown) => {
  if (Array.isArray(data)) return !data.length;
  else if (data !== null && typeof data === 'object')
    return !Object.keys(data).length;
  else return !data;
};

export const range = (start: number, end: number) => {
  if (
    nullOrUndefined(start) ||
    !isNumber(start) ||
    nullOrUndefined(end) ||
    !isNumber(end)
  )
    return [];

  const length = end - start + 1;
  return Array.from({ length }, (_, idx) => idx + start);
};

export const formatDate = (dateString: string): string => {
  if (!dateString) return "N/A";

  // Ensure UTC if backend forgot the timezone
  const safeDateString =
    dateString.endsWith("Z") || dateString.includes("+")
      ? dateString
      : dateString + "Z";

  const date = new Date(safeDateString);
  if (isNaN(date.getTime())) return "N/A";

  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

  return new Intl.DateTimeFormat("en-US", {
    timeZone: userTimeZone,
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(date);
};

export const formatDateTime = (dateString: string) => {
  if (!dateString) return "N/A";

  const safeDateString =
    dateString.endsWith("Z") || dateString.includes("+")
      ? dateString
      : dateString + "Z";
  const utcDate = new Date(safeDateString);
  if (isNaN(utcDate.getTime())) return "N/A";

  const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

  return new Intl.DateTimeFormat("en-US", {
    timeZone: userTimeZone,
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(utcDate);
};

export const formatCurrency = (amount: number, currency: string): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency || 'USD',
  }).format(amount);
};

export const isExpired = (validUntil: string): boolean => {
  return new Date(validUntil) < new Date();
};

export const getUsagePercentage = (
  totalUsed: number,
  usageLimit: number,
): number => {
  if (usageLimit === 0) return 0;
  return Math.min((totalUsed / usageLimit) * 100, 100);
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
