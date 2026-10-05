import { z } from 'zod';

export const loginSchema = z.object({
  username: z
    .string()
    .trim()
    .min(1, 'Email address is required')
    .max(254, 'Email must not exceed 254 characters')
    .email('Enter a valid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(8, 'Password must be at least 8 characters')
    .max(64, 'Password must not exceed 64 characters'),
  deviceInfo: z.object({
    platformType: z.string(),
    platformInfo: z.string(),
    platformVersion: z.string(),
    deviceIdentifier: z.string(),
    appLanguage: z.string(),
    appVersion: z.string(),
  }),
});

export const DefaultLoginValues = {
  username: '',
  password: '',
  deviceInfo: {
    platformType: 'WEB',
    platformInfo: 'GOOGLE CHROME',
    platformVersion: '1001.0.1.1',
    deviceIdentifier: '42345245',
    appLanguage: 'ENGLISH',
    appVersion: '1.0.1',
  },
};
