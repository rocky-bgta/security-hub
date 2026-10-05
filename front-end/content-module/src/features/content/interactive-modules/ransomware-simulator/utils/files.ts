export const getFilenameWithoutExtension = (filename: string): string => {
  const lastDotIndex = filename.lastIndexOf('.');
  if (lastDotIndex === -1) return filename;
  return filename.substring(0, lastDotIndex);
};

export const buildEncryptedDisplayName = (
  originalName: string,
  extension: string,
): string => {
  if (!extension) return originalName;
  return `${getFilenameWithoutExtension(originalName)}${extension}`;
};

export const fileElementId = (name: string): string =>
  `file-${name.replace(/\./g, '-')}`;
