/**
 * Constants for the Phishing module
 */

export const VERIFICATION_CODE_EXPIRY_MINUTES = 15;

export const PAGE_SIZE_OPTIONS = [10, 25, 50, 100];

export const DEFAULT_PAGE_SIZE = 10;

export const DEBOUNCE_DELAY = 300;

export const PUBLIC_URL = import.meta.env.VITE_PUBLIC_URL;

export const FILE_PATH_PREFIX = 'https://content.aspireelearning.com/';

export const SENDER_PROFILE_IMPORT_TEMPLATE_URL =
  'https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/sender-profile-import-sample.csv';

/**
 * Toast messages
 */
export const TOAST_MESSAGES = {
  // Domain verification
  DOMAIN_VERIFICATION_EMAIL_SENT:
    'Verification email sent successfully. Please check your inbox.',
  DOMAIN_ADDED: 'Domain added successfully!',
  DOMAIN_VERIFIED: 'Domain verified successfully!',
  DOMAIN_LOCKED: 'Domain locked successfully!',
  DOMAIN_UNLOCKED: 'Domain unlocked successfully!',
  DOMAIN_DELETED: 'Domain deleted successfully!',

  // Error messages
  DOMAIN_ALREADY_VERIFIED: 'This domain is already verified.',
  DOMAIN_LOCKED_BY_ANOTHER: 'This domain is locked by another tenant.',
  INVALID_VERIFICATION_CODE: 'Invalid verification code. Please try again.',
  EXPIRED_VERIFICATION_CODE:
    'Verification code has expired. Please request a new one.',
  TOO_MANY_ATTEMPTS: 'Too many verification attempts. Please try again later.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
};
