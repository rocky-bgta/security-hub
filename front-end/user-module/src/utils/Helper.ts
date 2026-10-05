import clsx, { ClassValue } from 'clsx';
import type { DateRange } from 'react-day-picker';
import { twMerge } from 'tailwind-merge';

import { IGetListParams } from 'models/Global';

export const EMAIL_ADDRESS_MAX_LENGTH = 255;

export const cn = (...inputs: Array<ClassValue>) => {
  return twMerge(clsx(inputs));
};

export const isValidEmail = (email: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

export const nullOrUndefined = (val: unknown) =>
  val === null || val === undefined;

export const isNumber = (val: unknown): val is number =>
  !isNaN(val as number) && isFinite(val as number) && typeof val === 'number';

export const isSuccessResponse = (statusCode: number) => {
  return statusCode >= 200 && statusCode < 300;
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

export const HumanizeDate = (dateString?: string): string => {
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
    month: "short",
    day: "numeric",
  }).format(date);
};

export const objectToQueryStringWithArray = (
  params: IGetListParams,
): string => {
  const queryString: string[] = [];

  for (const param in params) {
    if (Object.prototype.hasOwnProperty.call(params, param)) {
      const value = params[param as keyof IGetListParams];

      if (value !== undefined && value !== null && value !== '') {
        // Handle arrays (for multi-select filters like departments)
        if (Array.isArray(value)) {
          value.forEach(item => {
            if (item !== undefined && item !== null && item !== '') {
              // Use plural form for array parameters (department -> departments)
              const paramKey = param === 'department' ? 'departments' : param;
              queryString.push(
                `${encodeURIComponent(paramKey)}=${encodeURIComponent(String(item))}`,
              );
            }
          });
        } else {
          // Handle regular string/number values
          queryString.push(
            `${encodeURIComponent(param)}=${encodeURIComponent(String(value))}`,
          );
        }
      }
    }
  }

  return queryString.length > 0 ? queryString.join('&') : '';
};

// Oct 25, 2025, 02:47 PM
export const formatDateAndTime = (dateString: string) => {
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

export const humanizeText = (value?: string): string => {
  if (!value) return '-';

  return value
    .replace(/[_-]+/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, char => char.toUpperCase())
    .trim();
};

export const toLocalDateString = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const parseLocalDateString = (value: string): Date => {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
};

export const toDateRange = (
  startDate?: string,
  endDate?: string,
): DateRange | undefined => {
  if (!startDate) return undefined;
  return {
    from: parseLocalDateString(startDate),
    to: endDate ? parseLocalDateString(endDate) : undefined,
  };
};

export const fromDateRange = (
  range?: DateRange,
): { startDate: string; endDate: string } => ({
  startDate: range?.from ? toLocalDateString(range.from) : '',
  endDate: range?.to ? toLocalDateString(range.to) : '',
});
