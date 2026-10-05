import {
  AudienceType,
  CampaignChannel,
  CampaignLearningMode,
  CampaignType,
  CampaignValidityUnit,
  ICampaignSchedule,
  PhishingAssignedFor,
  ScheduleType,
  SendingPattern,
} from 'models/Campaign';
import {
  ITimezoneLookupOption,
  isoToDatetimeLocal,
  isIsoDateTimeString,
  resolveTimezoneId,
} from 'utils/Helper';
import { z } from 'zod';
/** RHF `valueAsNumber` uses NaN for empty number inputs; plain `z.number().optional()` rejects NaN. */
function optionalNumberFromInput() {
  return z.optional(
    z.union([z.nan().transform(() => undefined), z.number()]),
  );
}

const MAX_EXPIRY_DAYS = 365;
const MAX_EXPIRY_MONTHS = 12;

const expireDateFormSchema = z.object({
  validityUnit: z.enum(CampaignValidityUnit, {
    error: () => ({ message: 'Select days or months' }),
  }),
  /** Use `valueAsNumber` on the input; empty fields yield NaN and fail validation */
  validityPeriod: z
    .number({
      error: () => ({ message: 'Validity period is required' }),
    })
    .int({ message: 'Enter a whole number' }),
});

// Step 1: Campaign Setup Schema
export const CampaignSetupSchema = z
  .object({
    campaignName: z
      .string()
      .min(1, 'Campaign name is required')
      .max(50, 'Campaign name cannot exceed 50 characters'),
    campaignType: z.enum(CampaignType, {
      error: () => ({ message: 'Please select a campaign type' }),
    }),
    channel: z.nativeEnum(CampaignChannel),
    productPackageId: z.string().min(1, 'Please select a package'),
    assignedFor: z.nativeEnum(PhishingAssignedFor),
    learningMode: z.nativeEnum(CampaignLearningMode),
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

export type TCampaignSetupForm = z.infer<typeof CampaignSetupSchema>;

export const campaignSetupDefaultValues: TCampaignSetupForm = {
  campaignName: '',
  campaignType: CampaignType.SIMULATED_PHISHING,
  channel: CampaignChannel.EMAIL,
  assignedFor: PhishingAssignedFor.PHISHING_TRAINING_FOR_ALL,
  productPackageId: '',
  learningMode: CampaignLearningMode.MICRO_CONTENT,
  expireDate: {
    validityUnit: CampaignValidityUnit.DAYS,
    validityPeriod: undefined as unknown as number,
  },
};

// Step 2: Email Template Schema
export const CampaignEmailTemplateSchema = z.object({
  emailTemplateId: z.string().min(1, 'Please select an email template'),
  templateLanguage: z.string().optional(),
});

export type TCampaignEmailTemplateForm = z.infer<
  typeof CampaignEmailTemplateSchema
>;

export const campaignEmailTemplateDefaultValues: TCampaignEmailTemplateForm = {
  emailTemplateId: '',
  templateLanguage: '',
};

// Step 3: Landing Page Schema
export const CampaignLandingPageSchema = z.object({
  landingPageId: z.string().optional(),
  landingPageType: z.string().optional(),
});

export type TCampaignLandingPageForm = z.infer<
  typeof CampaignLandingPageSchema
>;

export const campaignLandingPageDefaultValues: TCampaignLandingPageForm = {
  landingPageId: '',
  landingPageType: 'LANDING',
};

// Step 4: Sender Profile Schema
export const CampaignSenderProfileSchema = z.object({
  senderProfileId: z.string().min(1, 'Please select a sender profile'),
});

export type TCampaignSenderProfileForm = z.infer<
  typeof CampaignSenderProfileSchema
>;

export const campaignSenderProfileDefaultValues: TCampaignSenderProfileForm = {
  senderProfileId: '',
};

// Step 5: Tags Schema
export const CampaignTagsSchema = z.object({
  tags: z.array(z.string()).max(10, 'Maximum 10 tags allowed'),
});

export type TCampaignTagsForm = z.infer<typeof CampaignTagsSchema>;

export const campaignTagsDefaultValues: TCampaignTagsForm = {
  tags: [],
};

// Step 6: Audience Schema
export const CampaignAudienceSchema = z
  .object({
    audienceType: z.enum(AudienceType, {
      error: () => ({ message: 'Please select audience type' }),
    }),
    departmentIds: z.array(z.string()),
    groupIds: z.array(z.string()),
    userIds: z.array(z.string()),
  })
  .superRefine((data, ctx) => {
    if (
      data.audienceType === AudienceType.DEPARTMENTS &&
      data.departmentIds.length === 0
    ) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select at least one recipient',
        path: ['departmentIds'],
      });
    }
    if (
      data.audienceType === AudienceType.GROUPS &&
      data.groupIds.length === 0
    ) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select at least one recipient',
        path: ['groupIds'],
      });
    }
    if (
      data.audienceType === AudienceType.INDIVIDUAL &&
      data.userIds.length === 0
    ) {
      ctx.addIssue({
        code: 'custom',
        message: 'Please select at least one recipient',
        path: ['userIds'],
      });
    }
  });

export type TCampaignAudienceForm = z.infer<typeof CampaignAudienceSchema>;

export const campaignAudienceDefaultValues: TCampaignAudienceForm = {
  audienceType: AudienceType.INDIVIDUAL,
  departmentIds: [],
  groupIds: [],
  userIds: [],
};

// Step 7: Training Schema
export const CampaignTrainingSchema = z.object({
  trainingModuleId: z.string().optional(),
});

export type TCampaignTrainingForm = z.infer<typeof CampaignTrainingSchema>;

export const campaignTrainingDefaultValues: TCampaignTrainingForm = {
  trainingModuleId: '',
};

// Step 8: Schedule Schema
export const CampaignScheduleSchema = z
  .object({
    scheduleType: z.enum(ScheduleType, {
      error: () => ({ message: 'Please select schedule type' }),
    }),
    startDateTime: z.string(),
    endDateTime: z.string(),
    timezone: z.string(),
    sendingPattern: z.enum(SendingPattern, {
      error: () => ({ message: 'Please select sending pattern' }),
    }),
    batchSize: optionalNumberFromInput(),
    batchIntervalMinutes: optionalNumberFromInput(),
    recurringFrequency: z.string(),
    daysOfWeek: z.array(z.number()),
    dayOfMonth: z.number().min(1).max(31).optional(),
    timeOfDay: z.string(),
    repeatCount: z.number().min(1).optional(),
    neverExpires: z.boolean(),
  })
  .superRefine((data, ctx) => {
    if (data.scheduleType === ScheduleType.SCHEDULED && !data.startDateTime) {
      ctx.addIssue({
        code: 'custom',
        message: 'Start date/time is required for scheduled campaigns',
        path: ['startDateTime'],
      });
    }

    if (data.sendingPattern === SendingPattern.STAGGERED) {
      if (data.batchSize == null || data.batchSize < 1) {
        ctx.addIssue({
          code: 'custom',
          message:
            data.batchSize == null
              ? 'Batch size is required for staggered sending'
              : 'Batch size must be at least 1',
          path: ['batchSize'],
        });
      }

      if (
        data.batchIntervalMinutes != null &&
        data.batchIntervalMinutes < 1
      ) {
        ctx.addIssue({
          code: 'custom',
          message: 'Interval must be at least 1 minute',
          path: ['batchIntervalMinutes'],
        });
      }
    }
  });

export type TCampaignScheduleForm = z.infer<typeof CampaignScheduleSchema>;

export const campaignScheduleDefaultValues: Partial<TCampaignScheduleForm> = {
  scheduleType: ScheduleType.IMMEDIATELY,
  startDateTime: '',
  endDateTime: '',
  timezone: '',
  sendingPattern: SendingPattern.ALL_AT_ONCE,
  recurringFrequency: '',
  daysOfWeek: [],
  timeOfDay: '',
  neverExpires: false,
};

/** Maps API schedule payload into wizard form fields. */
export function mapCampaignScheduleToForm(
  schedule?: ICampaignSchedule,
  timezoneOptions?: ITimezoneLookupOption[],
): Partial<TCampaignScheduleForm> {
  if (!schedule) return {};

  const rawTimezone = schedule.timezone ?? schedule.timeZone ?? '';
  const resolvedTimezoneId = timezoneOptions?.length
    ? resolveTimezoneId(rawTimezone, timezoneOptions)
    : rawTimezone;

  return {
    scheduleType: schedule.type,
    startDateTime: schedule.startDateTime ?? '',
    endDateTime: schedule.endDateTime ?? '',
    timezone: resolvedTimezoneId,
    sendingPattern: schedule.sendingPattern,
    batchSize: schedule.batchSize,
    batchIntervalMinutes: schedule.batchIntervalMinutes,
    recurringFrequency: schedule.recurringFrequency ?? '',
  };
}

/** Merges API / wizard partials into a full schedule form shape (null-safe). */
export function getCampaignScheduleFormDefaults(
  initial?: Partial<TCampaignScheduleForm>,
): TCampaignScheduleForm {
  const merged: Partial<TCampaignScheduleForm> = {
    ...campaignScheduleDefaultValues,
    ...initial,
  };

  const startDateTime = merged.startDateTime ?? '';
  const endDateTime = merged.endDateTime ?? '';

  return {
    scheduleType: merged.scheduleType ?? ScheduleType.IMMEDIATELY,
    startDateTime: isIsoDateTimeString(startDateTime)
      ? isoToDatetimeLocal(startDateTime)
      : startDateTime,
    endDateTime: isIsoDateTimeString(endDateTime)
      ? isoToDatetimeLocal(endDateTime)
      : endDateTime,
    timezone: merged.timezone ?? '',
    sendingPattern: merged.sendingPattern ?? SendingPattern.ALL_AT_ONCE,
    recurringFrequency: merged.recurringFrequency ?? '',
    daysOfWeek: Array.isArray(merged.daysOfWeek) ? merged.daysOfWeek : [],
    dayOfMonth: merged.dayOfMonth,
    timeOfDay: merged.timeOfDay ?? '',
    repeatCount: merged.repeatCount,
    neverExpires: merged.neverExpires ?? false,
    batchSize:
      merged.sendingPattern === SendingPattern.STAGGERED
        ? merged.batchSize
        : undefined,
    batchIntervalMinutes:
      merged.sendingPattern === SendingPattern.STAGGERED
        ? merged.batchIntervalMinutes
        : undefined,
  };
}

// Complete campaign form (for review step)
export const CampaignCompleteSchema = z.object({
  // Step 1
  campaignName: z.string().min(1),
  campaignType: z.nativeEnum(CampaignType),
  expireDate: z.object({
    validityUnit: z.nativeEnum(CampaignValidityUnit),
    validityPeriod: z.number().int().min(1),
  }),
  // Step 2
  emailTemplateId: z.string().min(1),
  // Step 3
  landingPageId: z.string().optional(),
  landingPageType: z.string().optional(),
  // Step 4
  senderProfileId: z.string().min(1),
  // Step 5
  tags: z.array(z.string()),
  // Step 6
  audienceType: z.nativeEnum(AudienceType),
  departmentIds: z.array(z.string()),
  groupIds: z.array(z.string()),
  userIds: z.array(z.string()),
  // Step 7
  trainingModuleId: z.string().optional(),
  // Step 8
  scheduleType: z.nativeEnum(ScheduleType),
  startDateTime: z.string().optional(),
  timezone: z.string(),
  sendingPattern: z.nativeEnum(SendingPattern),
  batchSize: z.number().optional(),
  batchIntervalMinutes: z.number().optional(),
});

export type TCampaignCompleteForm = z.infer<typeof CampaignCompleteSchema>;
