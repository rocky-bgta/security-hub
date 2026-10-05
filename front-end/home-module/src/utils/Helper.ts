import clsx, { ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { v4 as uuidv4 } from 'uuid';

import { IGetListParams } from 'models/Global';
import { LocalStorageKey } from 'utils/Constants';
import { getToken, setToken } from 'utils/TokenStorage';

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

export const isSuccessResponse = (statusCode: number) => {
  return statusCode >= 200 && statusCode < 300;
};

export const nullOrUndefined = (val: unknown) =>
  val === null || val === undefined;

export const isNumber = (val: unknown): val is number =>
  !isNaN(val as number) && isFinite(val as number) && typeof val === 'number';

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

export const isValidEmail = (email: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

export const getDeviceInfo = () => {
  const { userAgent, language } = navigator;

  let platformInfo = 'Unknown';
  let platformVersion = '0';

  if (userAgent.match(/Edg\/([\d.]+)/)) {
    platformInfo = 'Edge';
    platformVersion = userAgent.match(/Edg\/([\d.]+)/)?.[1] || '0';
  } else if (
    userAgent.match(/Chrome\/([\d.]+)/) &&
    !userAgent.includes('Edg/') &&
    !userAgent.includes('OPR/')
  ) {
    platformInfo = 'Chrome';
    platformVersion = userAgent.match(/Chrome\/([\d.]+)/)?.[1] || '0';
  } else if (userAgent.includes('OPR/') && userAgent.match(/OPR\/([\d.]+)/)) {
    platformInfo = 'Opera';
    platformVersion = userAgent.match(/OPR\/([\d.]+)/)?.[1] || '0';
  } else if (userAgent.match(/Firefox\/([\d.]+)/)) {
    platformInfo = 'Firefox';
    platformVersion = userAgent.match(/Firefox\/([\d.]+)/)?.[1] || '0';
  } else if (
    userAgent.match(/Version\/([\d.]+).*Safari/) &&
    !userAgent.includes('Chrome')
  ) {
    platformInfo = 'Safari';
    platformVersion = userAgent.match(/Version\/([\d.]+).*Safari/)?.[1] || '0';
  }

  if (!getToken(LocalStorageKey.DEVICE_ID))
    setToken(LocalStorageKey.DEVICE_ID, uuidv4());

  return {
    platformType: 'WEB',
    platformInfo,
    platformVersion,
    deviceIdentifier: getToken(LocalStorageKey.DEVICE_ID),
    appLanguage: language,
    appVersion: import.meta.env.VITE_APP_VERSION,
  };
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

export const blockEmailSpaceKey = (e: {
  key: string;
  preventDefault: () => void;
}) => {
  if (e.key === ' ') {
    e.preventDefault();
  }
};

export const sanitizeEmailInput = (value: string) => value.replace(/\s/g, '');
