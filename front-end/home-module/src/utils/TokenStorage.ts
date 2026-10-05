import CryptoJS from 'crypto-js';

import { LocalStorageKey } from 'utils/Constants';

const getStorageSecret = (): string => {
  const secret = import.meta.env.VITE_STORAGE_SECRET?.trim();
  if (!secret) {
    throw new Error(
      'VITE_STORAGE_SECRET must be configured for encrypted token storage.',
    );
  }
  return secret;
};

const PERSISTENT_STORAGE_KEYS: ReadonlyArray<string> = [
  LocalStorageKey.DEVICE_ID,
  LocalStorageKey.REMEMBERED_USERNAME,
];

const readPersistentTokens = () => {
  const preserved: Record<string, string> = {};

  PERSISTENT_STORAGE_KEYS.forEach(key => {
    const value = localStorage.getItem(key);
    if (value) {
      preserved[key] = value;
    }
  });

  return preserved;
};

const writePersistentTokens = (preserved: Record<string, string>) => {
  Object.entries(preserved).forEach(([key, value]) => {
    localStorage.setItem(key, value);
  });
};

export const setToken = (key: string, value: string) => {
  const encrypted = CryptoJS.AES.encrypt(value, getStorageSecret()).toString();
  localStorage.setItem(key, encrypted);
};

export const getToken = (key: string) => {
  const encrypted = localStorage.getItem(key);
  if (!encrypted) return '';

  try {
    const bytes = CryptoJS.AES.decrypt(encrypted, getStorageSecret());
    const decrypted = bytes.toString(CryptoJS.enc.Utf8);
    return decrypted || '';
  } catch (err) {
    console.error('Token decryption failed:', err);
    return '';
  }
};

export const removeToken = (key: string) => {
  localStorage.removeItem(key);
};

export const removeAllTokens = () => {
  localStorage.clear();
};

export const removeAllTokensButPersistents = () => {
  const preserved = readPersistentTokens();
  localStorage.clear();
  writePersistentTokens(preserved);
};
