import {
  IVoiceServerConfigurationForm,
  VoiceProvider,
  VoiceServerConfigurationStatus,
} from 'models/VoiceServerConfiguration';
import { z } from 'zod';

export const CONFIGURATION_NAME_MAX_LENGTH = 100;
export const CONFIGURATION_NAME_MAX_ERROR_MESSAGE = `Configuration name cannot exceed ${CONFIGURATION_NAME_MAX_LENGTH} characters`;

export const API_KEY_MAX_LENGTH = 256;
export const API_KEY_MAX_ERROR_MESSAGE = `API key cannot exceed ${API_KEY_MAX_LENGTH} characters`;

export const API_SECRET_MAX_LENGTH = 256;
export const API_SECRET_MAX_ERROR_MESSAGE = `API secret cannot exceed ${API_SECRET_MAX_LENGTH} characters`;

export const CALLER_ID_MAX_LENGTH = 50;
export const CALLER_ID_MAX_ERROR_MESSAGE = `Caller ID cannot exceed ${CALLER_ID_MAX_LENGTH} characters`;

export const BASE_URL_MAX_LENGTH = 500;
export const BASE_URL_MAX_ERROR_MESSAGE = `Base URL cannot exceed ${BASE_URL_MAX_LENGTH} characters`;

export const REGION_MAX_LENGTH = 100;
export const COUNTRY_CODE_MAX_LENGTH = 10;

const callerIdPattern = /^[a-zA-Z0-9+_-]+$/;

const optionalUrl = z
  .string()
  .max(BASE_URL_MAX_LENGTH, BASE_URL_MAX_ERROR_MESSAGE)
  .refine(
    value => !value.trim() || /^https?:\/\/.+/i.test(value.trim()),
    'Please enter a valid URL',
  );

export const VoiceServerConfigurationSchema = z.object({
  name: z
    .string()
    .min(1, 'Configuration name is required')
    .max(CONFIGURATION_NAME_MAX_LENGTH, CONFIGURATION_NAME_MAX_ERROR_MESSAGE),
  provider: z.enum(VoiceProvider, {
    error: 'Provider is required',
  }),
  apiKey: z
    .string()
    .min(1, 'API key is required')
    .max(API_KEY_MAX_LENGTH, API_KEY_MAX_ERROR_MESSAGE),
  apiSecret: z
    .string()
    .min(1, 'API secret is required')
    .max(API_SECRET_MAX_LENGTH, API_SECRET_MAX_ERROR_MESSAGE),
  callerId: z
    .string()
    .min(1, 'Caller ID is required')
    .max(CALLER_ID_MAX_LENGTH, CALLER_ID_MAX_ERROR_MESSAGE)
    .regex(
      callerIdPattern,
      'Caller ID may only contain letters, numbers, +, _, and -',
    ),
  baseUrl: optionalUrl,
  region: z.string().max(REGION_MAX_LENGTH),
  countryCode: z.string().max(COUNTRY_CODE_MAX_LENGTH),
  status: z.enum(VoiceServerConfigurationStatus),
  isDefault: z.boolean(),
});

export type TVoiceServerConfigurationForm = z.infer<
  typeof VoiceServerConfigurationSchema
>;

const voiceServerConfigurationSharedFields = {
  name: z
    .string()
    .min(1, 'Configuration name is required')
    .max(CONFIGURATION_NAME_MAX_LENGTH, CONFIGURATION_NAME_MAX_ERROR_MESSAGE),
  provider: z.enum(VoiceProvider, {
    error: 'Provider is required',
  }),
  callerId: z
    .string()
    .min(1, 'Caller ID is required')
    .max(CALLER_ID_MAX_LENGTH, CALLER_ID_MAX_ERROR_MESSAGE)
    .regex(
      callerIdPattern,
      'Caller ID may only contain letters, numbers, +, _, and -',
    ),
  baseUrl: optionalUrl,
  region: z.string().max(REGION_MAX_LENGTH),
  countryCode: z.string().max(COUNTRY_CODE_MAX_LENGTH),
  status: z.enum(VoiceServerConfigurationStatus),
  isDefault: z.boolean(),
};

export const VoiceServerConfigurationUpdateSchema = z.object({
  ...voiceServerConfigurationSharedFields,
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

export type TVoiceServerConfigurationUpdateForm = z.infer<
  typeof VoiceServerConfigurationUpdateSchema
>;

export const voiceServerConfigurationDefaultValues: TVoiceServerConfigurationForm =
  {
    name: '',
    provider: VoiceProvider.TWILIO,
    apiKey: '',
    apiSecret: '',
    callerId: '',
    baseUrl: '',
    region: '',
    countryCode: 'US',
    status: VoiceServerConfigurationStatus.ACTIVE,
    isDefault: false,
  };

export const buildVoiceServerConfigurationPayload = (
  data: TVoiceServerConfigurationForm,
): IVoiceServerConfigurationForm => ({
  name: data.name.trim(),
  provider: data.provider.trim(),
  apiKey: data.apiKey.trim(),
  apiSecret: data.apiSecret.trim(),
  callerId: data.callerId.trim(),
  baseUrl: data.baseUrl.trim(),
  region: data.region.trim(),
  countryCode: data.countryCode.trim(),
  status: data.status,
  default: data.isDefault,
});

export const buildVoiceServerConfigurationUpdatePayload = (
  data: TVoiceServerConfigurationUpdateForm,
): IVoiceServerConfigurationForm => ({
  name: data.name.trim(),
  provider: data.provider.trim(),
  apiKey: data.apiKey?.trim() ?? '',
  apiSecret: data.apiSecret?.trim() ?? '',
  callerId: data.callerId.trim(),
  baseUrl: data.baseUrl.trim(),
  region: data.region.trim(),
  countryCode: data.countryCode.trim(),
  status: data.status,
  default: data.isDefault,
});
