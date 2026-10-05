import { FILE_PATH_PREFIX } from 'utils/Constants';

export const SUPPORT_TICKET_MAX_FILE_BYTES = 2 * 1024 * 1024;
export const SUPPORT_TICKET_MAX_TOTAL_BYTES = 10 * 1024 * 1024;

export const SUPPORT_TICKET_ALLOWED_TYPES_HINT =
  'PDF, JPG, JPEG, PNG, DOC, or DOCX';

export const SUPPORT_TICKET_FILE_ACCEPT = [
  'application/pdf',
  'image/jpeg',
  'image/png',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  '.pdf',
  '.jpg',
  '.jpeg',
  '.png',
  '.doc',
  '.docx',
].join(',');

type AllowedExtension = 'pdf' | 'jpg' | 'jpeg' | 'png' | 'doc' | 'docx';

const ALLOWED_EXTENSIONS = new Set<AllowedExtension>([
  'pdf',
  'jpg',
  'jpeg',
  'png',
  'doc',
  'docx',
]);

const PREVIEWABLE_EXTENSIONS = new Set<AllowedExtension>([
  'pdf',
  'jpg',
  'jpeg',
  'png',
]);

const ALLOWED_MIME_BY_EXTENSION: Record<AllowedExtension, Set<string>> = {
  pdf: new Set(['application/pdf']),
  jpg: new Set(['image/jpeg', 'image/jpg']),
  jpeg: new Set(['image/jpeg', 'image/jpg']),
  png: new Set(['image/png']),
  doc: new Set(['application/msword', 'application/x-cfb']),
  docx: new Set([
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/zip',
  ]),
};

const BLOCKED_EXTENSIONS = new Set([
  '7z',
  'apk',
  'app',
  'bat',
  'bin',
  'cmd',
  'com',
  'cpl',
  'dll',
  'dmg',
  'exe',
  'gadget',
  'hta',
  'htm',
  'html',
  'img',
  'inf',
  'ini',
  'iso',
  'jar',
  'js',
  'jse',
  'jsp',
  'lnk',
  'msi',
  'msp',
  'pif',
  'php',
  'ps1',
  'py',
  'rar',
  'reg',
  'scr',
  'sh',
  'svg',
  'swf',
  'vb',
  'vbe',
  'vbs',
  'vbscript',
  'wsf',
  'wsh',
  'xls',
  'xlsm',
  'xlsx',
  'xlsb',
  'xlt',
  'xltm',
  'xml',
  'zip',
]);

const PDF_SIGNATURE = [0x25, 0x50, 0x44, 0x46];
const JPEG_SIGNATURE = [0xff, 0xd8, 0xff];
const PNG_SIGNATURE = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];
const OLE_SIGNATURE = [0xd0, 0xcf, 0x11, 0xe0, 0xa1, 0xb1, 0x1a, 0xe1];
const ZIP_SIGNATURES = [
  [0x50, 0x4b, 0x03, 0x04],
  [0x50, 0x4b, 0x05, 0x06],
  [0x50, 0x4b, 0x07, 0x08],
];

const INVALID_FILE_MESSAGE = `Only ${SUPPORT_TICKET_ALLOWED_TYPES_HINT} files are allowed.`;

const startsWithBytes = (bytes: Uint8Array, signature: number[]): boolean => {
  if (bytes.length < signature.length) return false;
  return signature.every((value, index) => bytes[index] === value);
};

const containsAscii = (bytes: Uint8Array, value: string): boolean => {
  const target = new TextEncoder().encode(value);
  if (bytes.length < target.length) return false;

  outer: for (let i = 0; i <= bytes.length - target.length; i += 1) {
    for (let j = 0; j < target.length; j += 1) {
      if (bytes[i + j] !== target[j]) continue outer;
    }
    return true;
  }

  return false;
};

const containsUtf16Le = (bytes: Uint8Array, value: string): boolean => {
  const codes = Array.from(value).map(char => char.charCodeAt(0));
  const span = codes.length * 2;
  if (bytes.length < span) return false;

  outer: for (let i = 0; i <= bytes.length - span; i += 1) {
    for (let j = 0; j < codes.length; j += 1) {
      if (bytes[i + j * 2] !== codes[j] || bytes[i + j * 2 + 1] !== 0) {
        continue outer;
      }
    }
    return true;
  }

  return false;
};

const getLastExtension = (fileName: string): string => {
  const trimmed = fileName.trim().toLowerCase();
  const dotIndex = trimmed.lastIndexOf('.');
  if (dotIndex <= 0 || dotIndex === trimmed.length - 1) return '';
  return trimmed.slice(dotIndex + 1);
};

const getExtensionParts = (fileName: string): string[] => {
  return fileName
    .trim()
    .toLowerCase()
    .split('.')
    .slice(1)
    .filter(Boolean);
};

const isAllowedExtension = (value: string): value is AllowedExtension => {
  return ALLOWED_EXTENSIONS.has(value as AllowedExtension);
};

const hasMalformedFileName = (fileName: string): boolean => {
  const trimmed = fileName.trim();

  if (!trimmed || trimmed !== fileName) return true;
  if (trimmed.length > 180) return true;
  if (/[\u0000-\u001f\u007f<>:"|?*\\/]/.test(trimmed)) return true;
  if (trimmed.startsWith('.') || trimmed.endsWith('.')) return true;
  if (trimmed.includes('..')) return true;

  const parts = trimmed.split('.');
  if (parts.length < 2) return true;
  if (parts.some(part => part.length === 0)) return true;
  if (parts[parts.length - 1].length > 5) return true;

  return false;
};

const hasDangerousDoubleExtension = (fileName: string): boolean => {
  const extensions = getExtensionParts(fileName);
  if (extensions.length <= 1) return false;

  const intermediate = extensions.slice(0, -1);
  return intermediate.some(
    part => BLOCKED_EXTENSIONS.has(part) || isAllowedExtension(part),
  );
};

const matchesSignature = (
  bytes: Uint8Array,
  extension: AllowedExtension,
): boolean => {
  switch (extension) {
    case 'pdf': {
      const payload =
        bytes.length >= 3 &&
        bytes[0] === 0xef &&
        bytes[1] === 0xbb &&
        bytes[2] === 0xbf
          ? bytes.subarray(3)
          : bytes;
      return startsWithBytes(payload, PDF_SIGNATURE);
    }
    case 'jpg':
    case 'jpeg':
      return startsWithBytes(bytes, JPEG_SIGNATURE);
    case 'png':
      return startsWithBytes(bytes, PNG_SIGNATURE);
    case 'doc':
      return (
        startsWithBytes(bytes, OLE_SIGNATURE) &&
        (containsAscii(bytes, 'WordDocument') ||
          containsUtf16Le(bytes, 'WordDocument')) &&
        !containsAscii(bytes, 'Workbook') &&
        !containsUtf16Le(bytes, 'Workbook')
      );
    case 'docx':
      return (
        ZIP_SIGNATURES.some(signature => startsWithBytes(bytes, signature)) &&
        containsAscii(bytes, '[Content_Types].xml') &&
        containsAscii(bytes, 'word/') &&
        !containsAscii(bytes, 'xl/')
      );
    default:
      return false;
  }
};

const getSafeDownloadFileName = (storageKey: string): string => {
  const raw = storageKey.split('/').pop() || 'attachment';
  const cleaned = raw.replace(/[^\w.\-]+/g, '_').slice(0, 120);
  const extension = getLastExtension(cleaned);

  if (!isAllowedExtension(extension)) {
    return 'attachment';
  }

  return cleaned;
};

export const validateSupportTicketFile = async (
  file: File | null | undefined,
): Promise<string | null> => {
  if (!file) {
    return 'Please select a file.';
  }

  if (file.size <= 0) {
    return 'The selected file is empty.';
  }

  if (file.size > SUPPORT_TICKET_MAX_FILE_BYTES) {
    return 'File size must not exceed 2 MB.';
  }

  if (
    hasMalformedFileName(file.name) ||
    hasDangerousDoubleExtension(file.name)
  ) {
    return 'The file name is invalid. Remove extra or blocked extensions and try again.';
  }

  const extension = getLastExtension(file.name);
  if (!isAllowedExtension(extension)) {
    return INVALID_FILE_MESSAGE;
  }

  const mimeType = file.type?.toLowerCase();
  if (
    mimeType &&
    mimeType !== 'application/octet-stream' &&
    !ALLOWED_MIME_BY_EXTENSION[extension].has(mimeType)
  ) {
    return INVALID_FILE_MESSAGE;
  }

  try {
    const header = new Uint8Array(await file.arrayBuffer());

    if (!matchesSignature(header, extension)) {
      return 'The file content does not match its extension.';
    }
  } catch {
    return 'The uploaded file appears to be corrupted.';
  }

  return null;
};

export const selectValidSupportTicketFiles = async (
  incoming: File[],
  alreadySelected: File[] = [],
): Promise<{ accepted: File[]; errors: string[] }> => {
  const errors: string[] = [];
  const accepted: File[] = [];
  let totalSize = alreadySelected.reduce((sum, file) => sum + file.size, 0);

  for (const file of incoming) {
    const error = await validateSupportTicketFile(file);
    if (error) {
      errors.push(`${file.name}: ${error}`);
      continue;
    }

    if (totalSize + file.size > SUPPORT_TICKET_MAX_TOTAL_BYTES) {
      errors.push('Total file size cannot exceed 10 MB.');
      break;
    }

    accepted.push(file);
    totalSize += file.size;
  }

  return { accepted, errors };
};

export const getSupportTicketAttachmentLinkProps = (storageKey: string) => {
  const filename = getSafeDownloadFileName(storageKey);
  const extension = getLastExtension(filename);
  const canPreview =
    isAllowedExtension(extension) && PREVIEWABLE_EXTENSIONS.has(extension);

  return {
    href: `${FILE_PATH_PREFIX}${storageKey}`,
    rel: 'noopener noreferrer',
    referrerPolicy: 'no-referrer' as const,
    ...(canPreview ? { target: '_blank' as const } : { download: filename }),
  };
};
