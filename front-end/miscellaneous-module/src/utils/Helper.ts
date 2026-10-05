import clsx, { ClassValue } from 'clsx';
import { IGetListParams } from 'models/Global';
import { twMerge } from 'tailwind-merge';
import { FILE_PATH_PREFIX } from 'utils/Constants';

export const cn = (...inputs: Array<ClassValue>) => {
  return twMerge(clsx(inputs));
};

export const objectToQueryString = (params: IGetListParams) => {
  const queryString = [];

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

// Oct 25, 2025
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

// Oct 25, 2025, 02:47 PM
export const formateDate = (dateString: string) => {
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

export const sliceWords = (text: string, maxWords: number): string => {
  const words = text.trim().split(/\s+/);
  return words.length > maxWords
    ? words.slice(0, maxWords).join(' ') + '...'
    : text;
};

export const isPdfType = (type?: string) => type?.toUpperCase() === 'PDF';

export const buildFileUrl = (path: string) => FILE_PATH_PREFIX + path;

export const getFileExtension = (file: File | null): string => {
  if (!file) return '';
  const name = file.name.toLowerCase();

  if (name.endsWith('.pdf')) return 'PDF';
  if (name.endsWith('.docx')) return 'WORD';
  if (name.endsWith('.doc')) return 'WORD';
  if (name.endsWith('.xls')) return 'XLS';
  if (name.endsWith('.xlsx')) return 'XLSX';
  return 'OTHER';
};

export interface IVideoMetadata {
  duration: number;
  videoWidth: number;
  videoHeight: number;
}

export const getVideoMetadata = (file: File): Promise<IVideoMetadata> => {
  return new Promise((resolve, reject) => {
    const video = document.createElement('video');
    video.preload = 'metadata';

    const objectUrl = URL.createObjectURL(file);

    video.onloadedmetadata = () => {
      URL.revokeObjectURL(objectUrl);
      resolve({
        duration: video.duration,
        videoWidth: video.videoWidth,
        videoHeight: video.videoHeight,
      });
    };

    video.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error('Failed to load video metadata'));
    };

    video.src = objectUrl;
  });
};

export const getExportFileName = (
  fileUrl: string,
  fallbackFileName = 'download',
) => {
  try {
    const pathname = new URL(fileUrl).pathname;
    const name = pathname.split('/').pop();
    if (name?.includes('.')) return name;
  } catch {
    // ignore invalid URLs and fall back to the provided name
  }

  return fallbackFileName;
};

export const SUPPORT_TEAM_DISPLAY_NAME = 'Support Team';

export const getTicketAuthorDisplayName = (
  authorName: string | undefined,
  isCurrentUser: boolean,
  hideInternalContact: boolean,
): string => {
  if (isCurrentUser) {
    return 'You';
  }

  if (hideInternalContact) {
    return SUPPORT_TEAM_DISPLAY_NAME;
  }

  return authorName?.trim() || SUPPORT_TEAM_DISPLAY_NAME;
};

export const downloadFromUrl = async (
  fileUrl: string,
  fallbackFileName = 'download',
) => {
  const response = await fetch(fileUrl);
  if (!response.ok) {
    throw new Error('Failed to download file');
  }

  const blob = await response.blob();
  const objectUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = getExportFileName(fileUrl, fallbackFileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(objectUrl);
};
