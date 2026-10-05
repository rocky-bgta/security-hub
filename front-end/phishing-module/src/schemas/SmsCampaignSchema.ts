import {
  CampaignChannel,
  CampaignLearningMode,
  CampaignType,
  CampaignValidityUnit,
  PhishingAssignedFor,
} from 'models/Campaign';
import { z } from 'zod';

const MAX_EXPIRY_DAYS = 365;
const MAX_EXPIRY_MONTHS = 12;

const expireDateFormSchema = z.object({
  validityUnit: z.enum(CampaignValidityUnit, {
    error: () => ({ message: 'Select days or months' }),
  }),
  validityPeriod: z
    .number({
      error: () => ({ message: 'Validity period is required' }),
    })
    .int({ message: 'Enter a whole number' }),
});

export const SmsCampaignSetupSchema = z
  .object({
    campaignName: z
      .string()
      .min(1, 'Campaign name is required')
      .max(50, 'Campaign name cannot exceed 50 characters'),
    campaignType: z.enum(CampaignType, {
      error: () => ({ message: 'Please select a campaign type' }),
    }),
    channel: z.literal(CampaignChannel.SMS),
    productPackageId: z.string().min(1, 'Please select a package'),
    assignedFor: z.nativeEnum(PhishingAssignedFor),
    learningMode: z.literal(CampaignLearningMode.MICRO_CONTENT),
    expireDate: expireDateFormSchema,
  })
  .superRefine((data, ctx) => {
    const { validityUnit, validityPeriod } = data.expireDate;
    if (validityPeriod < 1) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'Validity period must be at least 1',
        path: ['expireDate', 'validityPeriod'],
      });
      return;
    }
    if (
      validityUnit === CampaignValidityUnit.DAYS &&
      validityPeriod > MAX_EXPIRY_DAYS
    ) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: `Maximum ${MAX_EXPIRY_DAYS} days`,
        path: ['expireDate', 'validityPeriod'],
      });
    }
    if (
      validityUnit === CampaignValidityUnit.MONTHS &&
      validityPeriod > MAX_EXPIRY_MONTHS
    ) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: `Maximum ${MAX_EXPIRY_MONTHS} months`,
        path: ['expireDate', 'validityPeriod'],
      });
    }
  });

export type TSmsCampaignSetupForm = z.infer<typeof SmsCampaignSetupSchema>;

export const smsCampaignSetupDefaultValues: TSmsCampaignSetupForm = {
  campaignName: '',
  campaignType: CampaignType.SMISHING_SIMULATION,
  channel: CampaignChannel.SMS,
  assignedFor: PhishingAssignedFor.PHISHING_TRAINING_FOR_ALL,
  productPackageId: '',
  learningMode: CampaignLearningMode.MICRO_CONTENT,
  expireDate: {
    validityUnit: CampaignValidityUnit.DAYS,
    validityPeriod: 1,
  },
};

export const SmsCampaignServerSchema = z.object({
  smsServerConfigurationId: z
    .string()
    .min(1, 'Please select an SMS server configuration'),
});

export type TSmsCampaignServerForm = z.infer<typeof SmsCampaignServerSchema>;
