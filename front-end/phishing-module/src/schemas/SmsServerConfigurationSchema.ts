import {
  SmsServerConfigurationStatus,
  ISmsServerConfigurationForm,
} from 'models/SmsServerConfiguration';
import { z } from 'zod';

export const CONFIGURATION_NAME_MAX_LENGTH = 100;
export const CONFIGURATION_NAME_MAX_ERROR_MESSAGE = `Configuration name cannot exceed ${CONFIGURATION_NAME_MAX_LENGTH} characters`;

export const PROVIDER_MAX_LENGTH = 100;
export const PROVIDER_MAX_ERROR_MESSAGE = `Provider cannot exceed ${PROVIDER_MAX_LENGTH} characters`;

export const API_KEY_MAX_LENGTH = 256;
export const API_KEY_MAX_ERROR_MESSAGE = `API key cannot exceed ${API_KEY_MAX_LENGTH} characters`;

export const API_SECRET_MAX_LENGTH = 256;
export const API_SECRET_MAX_ERROR_MESSAGE = `API secret cannot exceed ${API_SECRET_MAX_LENGTH} characters`;

export const SENDER_ID_MAX_LENGTH = 50;
export const SENDER_ID_MAX_ERROR_MESSAGE = `Sender ID cannot exceed ${SENDER_ID_MAX_LENGTH} characters`;

export const BASE_URL_MAX_LENGTH = 500;
export const BASE_URL_MAX_ERROR_MESSAGE = `Base URL cannot exceed ${BASE_URL_MAX_LENGTH} characters`;

const senderIdPattern = /^[a-zA-Z0-9+_-]+$/;

export const SmsServerConfigurationSchema = z.object({
  name: z
    .string()
    .min(1, 'Configuration name is required')
    .max(CONFIGURATION_NAME_MAX_LENGTH, CONFIGURATION_NAME_MAX_ERROR_MESSAGE),
  provider: z
    .string()
    .min(1, 'Provider is required')
    .max(PROVIDER_MAX_LENGTH, PROVIDER_MAX_ERROR_MESSAGE),
  apiKey: z
    .string()
    .min(1, 'API key is required')
    .max(API_KEY_MAX_LENGTH, API_KEY_MAX_ERROR_MESSAGE),
  apiSecret: z
    .string()
    .min(1, 'API secret is required')
    .max(API_SECRET_MAX_LENGTH, API_SECRET_MAX_ERROR_MESSAGE),
  senderId: z
    .string()
    .min(1, 'Sender ID is required')
    .max(SENDER_ID_MAX_LENGTH, SENDER_ID_MAX_ERROR_MESSAGE)
    .regex(
      senderIdPattern,
      'Sender ID may only contain letters, numbers, +, _, and -',
    ),
  baseUrl: z
    .string()
    .min(1, 'Base URL is required')
    .max(BASE_URL_MAX_LENGTH, BASE_URL_MAX_ERROR_MESSAGE)
    .url('Please enter a valid URL'),
  status: z.enum(SmsServerConfigurationStatus),
  isDefault: z.boolean(),
});

export type TSmsServerConfigurationForm = z.infer<
  typeof SmsServerConfigurationSchema
>;

const smsServerConfigurationSharedFields = {
  name: z
    .string()
    .min(1, 'Configuration name is required')
    .max(CONFIGURATION_NAME_MAX_LENGTH, CONFIGURATION_NAME_MAX_ERROR_MESSAGE),
  provider: z
    .string()
    .min(1, 'Provider is required')
    .max(PROVIDER_MAX_LENGTH, PROVIDER_MAX_ERROR_MESSAGE),
  senderId: z
    .string()
    .min(1, 'Sender ID is required')
    .max(SENDER_ID_MAX_LENGTH, SENDER_ID_MAX_ERROR_MESSAGE)
    .regex(
      senderIdPattern,
      'Sender ID may only contain letters, numbers, +, _, and -',
    ),
  baseUrl: z
    .string()
    .min(1, 'Base URL is required')
    .max(BASE_URL_MAX_LENGTH, BASE_URL_MAX_ERROR_MESSAGE)
    .url('Please enter a valid URL'),
  status: z.enum(SmsServerConfigurationStatus),
  isDefault: z.boolean(),
};

export const SmsServerConfigurationUpdateSchema = z.object({
  ...smsServerConfigurationSharedFields,
  apiKey: z
    .string()
    .max(API_KEY_MAX_LENGTH, API_KEY_MAX_ERROR_MESSAGE)
    .optional()
    .or(z.literal('')),
  apiSecret: z
    .string()
    .max(API_SECRET_MAX_LENGTH, API_SECRET_MAX_ERROR_MESSAGE)
    .optional()
    .or(z.literal('')),
});

export type TSmsServerConfigurationUpdateForm = z.infer<
  typeof SmsServerConfigurationUpdateSchema
>;

export const smsServerConfigurationDefaultValues: TSmsServerConfigurationForm =
  {
    name: '',
    provider: '',
    apiKey: '',
    apiSecret: '',
    senderId: '',
    baseUrl: '',
    status: SmsServerConfigurationStatus.ACTIVE,
    isDefault: false,
  };

export const buildSmsServerConfigurationPayload = (
  data: TSmsServerConfigurationForm,
): ISmsServerConfigurationForm => ({
  name: data.name.trim(),
  provider: data.provider.trim(),
  apiKey: data.apiKey.trim(),
  apiSecret: data.apiSecret.trim(),
  senderId: data.senderId.trim(),
  baseUrl: data.baseUrl.trim(),
  status: data.status,
  default: data.isDefault,
});

export const buildSmsServerConfigurationUpdatePayload = (
  data: TSmsServerConfigurationUpdateForm,
): ISmsServerConfigurationForm => ({
  name: data.name.trim(),
  provider: data.provider.trim(),
  apiKey: data.apiKey?.trim() ?? '',
  apiSecret: data.apiSecret?.trim() ?? '',
  senderId: data.senderId.trim(),
  baseUrl: data.baseUrl.trim(),
  status: data.status,
  default: data.isDefault,
});
