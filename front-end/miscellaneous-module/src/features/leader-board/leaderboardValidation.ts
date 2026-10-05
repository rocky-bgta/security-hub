import { getVideoMetadata } from 'utils/Helper';

export const THUMBNAIL_MAX_BYTES = 2 * 1024 * 1024;
export const VIDEO_MAX_BYTES = 100 * 1024 * 1024;
export const VIDEO_MIN_DURATION = 10;
export const VIDEO_MAX_DURATION = 600;

export const THUMBNAIL_ACCEPT =
  'image/jpeg,image/jpg,image/png,.jpg,.jpeg,.png';
export const VIDEO_ACCEPT =
  'video/mp4,video/quicktime,video/webm,.mp4,.mov,.webm';

const THUMBNAIL_MIME_TYPES = new Set(['image/jpeg', 'image/jpg', 'image/png']);
const THUMBNAIL_EXTENSIONS = ['.jpg', '.jpeg', '.png'];

const VIDEO_MIME_TYPES = new Set([
  'video/mp4',
  'video/quicktime',
  'video/webm',
]);
const VIDEO_EXTENSIONS = ['.mp4', '.mov', '.webm'];

const BLOCKED_EXTENSIONS = [
  '.html',
  '.htm',
  '.exe',
  '.bat',
  '.cmd',
  '.sh',
  '.msi',
  '.dll',
  '.js',
  '.php',
];

const getFileExtensionFromName = (fileName: string): string => {
  const dotIndex = fileName.lastIndexOf('.');
  if (dotIndex === -1) return '';
  return fileName.slice(dotIndex).toLowerCase();
};

const isBlockedExtension = (fileName: string): boolean => {
  const ext = getFileExtensionFromName(fileName);
  return BLOCKED_EXTENSIONS.includes(ext);
};

const isValidThumbnailType = (file: File): boolean => {
  const ext = getFileExtensionFromName(file.name);
  if (THUMBNAIL_EXTENSIONS.includes(ext)) return true;
  return THUMBNAIL_MIME_TYPES.has(file.type.toLowerCase());
};

const isValidVideoType = (file: File): boolean => {
  const ext = getFileExtensionFromName(file.name);
  if (VIDEO_EXTENSIONS.includes(ext)) return true;
  return VIDEO_MIME_TYPES.has(file.type.toLowerCase());
};

export const validateThumbnailFile = (
  file: File | null | undefined,
): string | null => {
  if (!file) {
    return 'Thumbnail image is required.';
  }

  if (isBlockedExtension(file.name)) {
    return 'Please upload a JPG, JPEG, or PNG image.';
  }

  if (!isValidThumbnailType(file)) {
    return 'Please upload a JPG, JPEG, or PNG image.';
  }

  if (file.size > THUMBNAIL_MAX_BYTES) {
    return 'Thumbnail image cannot exceed 2 MB.';
  }

  return null;
};

export const validateVideoFile = async (
  file: File | null | undefined,
): Promise<string | null> => {
  if (!file) {
    return 'Video file is required.';
  }

  if (isBlockedExtension(file.name)) {
    return 'Please upload a valid MP4, MOV, or WEBM video.';
  }

  if (!isValidVideoType(file)) {
    return 'Please upload a valid MP4, MOV, or WEBM video.';
  }

  if (file.size > VIDEO_MAX_BYTES) {
    return 'Video size cannot exceed 100 MB.';
  }

  try {
    const metadata = await getVideoMetadata(file);

    if (
      !metadata.duration ||
      Number.isNaN(metadata.duration) ||
      metadata.videoWidth === 0 ||
      metadata.videoHeight === 0
    ) {
      return 'The uploaded file is not a valid video.';
    }

    if (metadata.duration < VIDEO_MIN_DURATION) {
      return 'Video must be at least 10 seconds long.';
    }

    if (metadata.duration > VIDEO_MAX_DURATION) {
      return 'Video cannot exceed 10 minutes.';
    }
  } catch {
    return 'The uploaded video appears to be corrupted.';
  }

  return null;
};
