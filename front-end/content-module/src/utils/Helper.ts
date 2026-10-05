import clsx, { ClassValue } from 'clsx';
import type { DateRange } from 'react-day-picker';
import { twMerge } from 'tailwind-merge';

import { IGetListParams } from 'models/Global';
import { ALPHANUMERIC_PATTERN } from 'utils/Constants';

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

export const cn = (...inputs: Array<ClassValue>) => {
  return twMerge(clsx(inputs));
};

// export const objectToQueryString = (params: IGetListParams): string => {
//   let queryString: string[] = [];
//   for (let param in params) {
//     if (
//       params.hasOwnProperty(param) &&
//       (params?.[param as keyof IGetListParams] || param === 'offset')
//     ) {
//       queryString.push(
//         `${encodeURIComponent(param)}=${encodeURIComponent(String(params[param as keyof IGetListParams]))}`,
//       );
//     }
//   }
//   return queryString.join('&');
// };

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

export const truncateText = (text: string, wordLimit: number) => {
  const words = text.split(' ');
  return words.length > wordLimit
    ? words.slice(0, wordLimit).join(' ') + '.....'
    : text;
};

export const isSuccessResponse = (statusCode: number) => {
  return statusCode >= 200 && statusCode < 300;
};

export const timeToSeconds = (timeStr: string) => {
  const parts = timeStr.split(':').map(Number);

  if (parts.length === 1) {
    const [seconds] = parts;
    return seconds;
  }

  if (parts.length === 2) {
    const [minutes, seconds] = parts;
    return minutes * 60 + seconds;
  }

  if (parts.length === 3) {
    const [hours, minutes, seconds] = parts;
    return hours * 3600 + minutes * 60 + seconds;
  }

  return 0;
};

export const formatTime = (time: number) => {
  if (!time) return '00:00:00';
  const hours = Math.floor(time / 3600);
  const minutes = Math.floor((time % 3600) / 60);
  const seconds = Math.floor(time % 60);
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
};

// export const getVideoDuration = (file: File): Promise<number> => {
//   return new Promise((resolve, reject) => {
//     const video = document.createElement('video');
//     video.preload = 'metadata';

//     video.onloadedmetadata = () => {
//       URL.revokeObjectURL(video.src);
//       resolve(video.duration); // in seconds
//     };

//     video.onerror = () => {
//       reject(new Error('Failed to load video metadata'));
//     };

//     video.src = URL.createObjectURL(file);
//   });
// };

export const getMediaDuration = (
  source: string | File,
  type: 'audio' | 'video',
): Promise<number> => {
  return new Promise((resolve, reject) => {
    const element = document.createElement(type);
    element.preload = 'metadata';

    const cleanup = () => {
      if (source instanceof File) {
        URL.revokeObjectURL(element.src);
      }
    };

    element.onloadedmetadata = () => {
      cleanup();
      resolve(element.duration);
    };

    element.onerror = () => {
      cleanup();
      reject(new Error(`Failed to load ${type} metadata`));
    };

    if (typeof source === 'string') {
      element.src = source;
    } else {
      element.src = URL.createObjectURL(source);
    }
  });
};

export const rgbaToHex = (color: {
  r: number;
  g: number;
  b: number;
  a: number;
}) => {
  return (
    '#' +
    color.r.toString(16).padStart(2, '0') +
    color.g.toString(16).padStart(2, '0') +
    color.b.toString(16).padStart(2, '0') +
    Math.round(color.a * 255)
      .toString(16)
      .padStart(2, '0')
  ).toLowerCase();
};

export const CopyText = async (
  copyText: string,
  isCopied?: React.Dispatch<React.SetStateAction<boolean>>,
) => {
  if (!copyText) return;
  try {
    await navigator.clipboard.writeText(copyText);
    if (isCopied) {
      isCopied(true);
      setTimeout(() => isCopied(false), 1000);
    }
  } catch (err) {
    console.error('Copy failed', err);
  }
};

// Oct 25, 2025
export const HumanizeDate = (dateString?: string): string => {
  if (!dateString) return 'N/A';

  // Ensure UTC if backend forgot the timezone
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

export function sliceWords(text: string, maxWords: number): string {
  const words = text.trim().split(/\s+/);
  return words.length > maxWords
    ? words.slice(0, maxWords).join(' ') + '...'
    : text;
}

export const isValidEmail = (email: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

// Oct 25, 2025, 02:47 PM
export const formatDateAndTime = (dateString: string) => {
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

export const humanizeText = (value?: string): string => {
  if (!value) return '-';

  return value
    .replace(/[_-]+/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, char => char.toUpperCase())
    .trim();
};

export const getDaysLeft = (date?: string) => {
  if (!date) return 'N/A';

  const today = new Date();
  const dueDate = new Date(date);

  const diffTime = dueDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) return 'Expired';
  if (diffDays === 0) return 'Expires today';

  return `${diffDays} days left`;
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