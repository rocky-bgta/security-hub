import type {
  CampaignSetup,
  IVishingAudienceRequest,
  IVishingCampaign,
  IVishingCampaignCreateRequest,
  IVishingScenarioAttachRequest,
  IVishingScenarioCreateRequest,
  IVishingScheduleRequest,
  IVishingTagsRequest,
  IVishingTelephonyRequest,
  IVishingTrainingRequest,
  IVishingVoiceSetupRequest,
  LlmEscalationLimit,
  StepKey,
  VishingApiCampaignType,
  VishingLearningMode,
  VoiceResponseStage,
} from 'models/Vishing';
import { CampaignValidityUnit } from 'models/Campaign';
import { datetimeLocalToIso } from 'utils/Helper';

export type UiEscalationLevel = 'low' | 'medium' | 'high';

const UI_ESCALATION_MAP: Record<UiEscalationLevel, LlmEscalationLimit> = {
  low: 'LOW',
  medium: 'MEDIUM',
  high: 'HIGH',
};

export const toLlmEscalationLimit = (
  level: UiEscalationLevel,
): LlmEscalationLimit => UI_ESCALATION_MAP[level];

export const toVishingScenarioCreateRequest = (input: {
  name: string;
  script: string;
  templateId: string;
  templateName: string;
  llm: boolean;
  escalation: UiEscalationLevel;
}): IVishingScenarioCreateRequest => ({
  scenarioName: input.name.trim(),
  scriptBody: input.script.trim(),
  attackTemplateId: input.templateId,
  attackTemplateName: input.templateName,
  enableLlmResponses: input.llm,
  escalationLimit: toLlmEscalationLimit(input.escalation),
});

const UI_CAMPAIGN_TYPE_MAP: Record<
  CampaignSetup['type'],
  VishingApiCampaignType
> = {
  VISHING_SIMULATION: 'VISHING_SIMULATION',
  VISHING_WITH_TRAINING: 'VISHING_WITH_TRAINING',
};

const API_CAMPAIGN_TYPE_MAP: Record<
  VishingApiCampaignType,
  CampaignSetup['type']
> = {
  VISHING_SIMULATION: 'VISHING_SIMULATION',
  VISHING_WITH_TRAINING: 'VISHING_WITH_TRAINING',
};

const UI_RESPONSE_STAGE_MAP: Record<
  CampaignSetup['stages'][number],
  VoiceResponseStage
> = {
  CALL_ENGAGED: 'CALL_ENGAGED',
  COMPROMISED: 'COMPROMISED',
};

const API_RESPONSE_STAGE_MAP: Record<
  VoiceResponseStage,
  CampaignSetup['stages'][number]
> = {
  CALL_ENGAGED: 'CALL_ENGAGED',
  COMPROMISED: 'COMPROMISED',
};

export const toVishingCampaignCreateRequest = (
  setup: CampaignSetup,
): IVishingCampaignCreateRequest => {
  const payload: IVishingCampaignCreateRequest = {
    campaignName: setup.name.trim(),
    campaignType: UI_CAMPAIGN_TYPE_MAP[setup.type],
    channel: 'VOICE',
    productPackageId: setup.productPackageId,
    expireDate: {
      validityUnit: setup.expireDate.validityUnit as CampaignValidityUnit,
      validityPeriod: setup.expireDate.validityPeriod,
    },
  };

  payload.responseStages = setup.stages.map(
    stage => UI_RESPONSE_STAGE_MAP[stage],
  );

  if (setup.type === 'VISHING_WITH_TRAINING') {
    payload.learningMode = 'MICRO_CONTENT' satisfies VishingLearningMode;
  }

  return payload;
};

export const toCampaignSetup = (campaign: IVishingCampaign): CampaignSetup => ({
  name: campaign.campaignName ?? '',
  type: API_CAMPAIGN_TYPE_MAP[campaign.campaignType] ?? 'vishing',
  stages: (campaign.responseStages ?? ['COMPROMISED']).map(
    stage => API_RESPONSE_STAGE_MAP[stage],
  ),
  learningMode: 'MICRO_CONTENT',
  productPackageId: campaign.productPackageId || '',
  expireDate: campaign.expireDate
    ? {
        validityUnit: campaign.expireDate.validityUnit as CampaignValidityUnit,
        validityPeriod: campaign.expireDate.validityPeriod,
      }
    : {
        validityUnit: CampaignValidityUnit.DAYS,
        validityPeriod: 1,
      },
});

export const getVishingCampaignId = (
  campaign: IVishingCampaign | null | undefined,
): string | null => campaign?.campaignId ?? null;

export type UiScheduleWhen = 'immediate' | 'later' | 'recurring';
export type UiSendingPattern = 'all' | 'staggered';
export type UiAudienceMode = 'all' | 'department' | 'risk' | 'individual';

const AUDIENCE_MODE_MAP: Record<UiAudienceMode, string> = {
  all: 'ALL_USERS',
  department: 'DEPARTMENTS',
  risk: 'RISK_GROUPS',
  individual: 'INDIVIDUAL',
};

export const toVishingScheduleRequest = (input: {
  when: UiScheduleWhen;
  startDateTime: string;
  timezone: string;
  pattern: UiSendingPattern;
  ianaTimeZone?: string;
}): IVishingScheduleRequest => ({
  scheduleType:
    input.when === 'immediate'
      ? 'IMMEDIATELY'
      : input.when === 'later'
        ? 'SCHEDULED'
        : 'RECURRING',
  startDateTime:
    input.when === 'later' && input.startDateTime
      ? datetimeLocalToIso(input.startDateTime, input.ianaTimeZone)
      : undefined,
  timezone: input.timezone,
  sendingConfig: {
    sendingPattern: input.pattern === 'all' ? 'ALL_AT_ONCE' : 'STAGGERED',
  },
});

export const toVishingAudienceRequest = (input: {
  mode: UiAudienceMode;
  selectedUserIds: string[];
  departmentIds?: string[];
  riskGroups?: string[];
}): IVishingAudienceRequest => ({
  audienceType: AUDIENCE_MODE_MAP[input.mode],
  userIds: input.mode === 'individual' ? input.selectedUserIds : undefined,
  departmentIds: input.mode === 'department' ? input.departmentIds : undefined,
  riskGroups: input.mode === 'risk' ? input.riskGroups : undefined,
});

export const toVishingTrainingRequest = (input: {
  selectedModuleIds: string[];
  period: string;
  length: string;
  packageId?: string;
}): IVishingTrainingRequest => ({
  trainingModuleId: input.selectedModuleIds[0],
  packageId: input.packageId,
  completionDays: input.length
    ? {
        durationUnit: input.period.toUpperCase(),
        durationValue: Number(input.length),
      }
    : undefined,
});

// ============ Draft recovery ============

export interface IVishingWizardDraft {
  setup: CampaignSetup;
  voiceSetup: IVishingVoiceSetupRequest;
  scenarioAttach: IVishingScenarioAttachRequest;
  selectedScenarioId: string | null;
  telephony: IVishingTelephonyRequest;
  tags: IVishingTagsRequest;
  audience: IVishingAudienceRequest;
  training: IVishingTrainingRequest;
  schedule: IVishingScheduleRequest;
  resumeStepKey: StepKey;
  view: 'wizard' | 'dashboard';
}

const isAudienceComplete = (audience?: IVishingAudienceRequest): boolean => {
  if (!audience?.audienceType) return false;
  if (audience.audienceType === 'ALL_USERS') return true;
  if (audience.audienceType === 'INDIVIDUAL') {
    return Boolean(audience.userIds?.length);
  }
  if (audience.audienceType === 'DEPARTMENTS') {
    return Boolean(audience.departmentIds?.length);
  }
  if (
    audience.audienceType === 'RISK_GROUPS' ||
    audience.audienceType === 'GROUPS'
  ) {
    return Boolean(audience.groupIds?.length || audience.riskGroups?.length);
  }
  return Boolean(audience.groupIds?.length);
};

const isStepComplete = (
  campaign: IVishingCampaign,
  stepKey: StepKey,
): boolean => {
  switch (stepKey) {
    case 'setup':
      return Boolean(campaign.campaignName?.trim());
    case 'voice':
      return Boolean(campaign.voiceData?.voiceCloneId);
    case 'scenario':
      return Boolean(campaign.voiceScenario?.scenarioId);
    case 'telephony':
      return Boolean(campaign.telephonyData?.voiceServerConfigurationId);
    case 'tags':
      return true;
    case 'audience':
      return isAudienceComplete(campaign.audience);
    case 'training':
      return Boolean(
        campaign.trainingData?.trainingModuleId ||
        (campaign.trainingData?.topicId?.length ?? 0) >= 2,
      );
    case 'schedule':
      return Boolean(campaign.schedule?.scheduleType);
    case 'review':
      return false;
    case 'live':
      return (
        campaign.status === 'RUNNING' ||
        campaign.status === 'COMPLETED' ||
        campaign.status === 'PAUSED'
      );
    default:
      return false;
  }
};

export const inferResumeStepKey = (campaign: IVishingCampaign): StepKey => {
  if (
    campaign.status === 'RUNNING' ||
    campaign.status === 'COMPLETED' ||
    campaign.status === 'PAUSED'
  ) {
    return 'live';
  }

  const hasTraining = campaign.campaignType === 'VISHING_WITH_TRAINING';
  const order: StepKey[] = hasTraining
    ? [
        'setup',
        'voice',
        'scenario',
        'telephony',
        'tags',
        'audience',
        'training',
        'schedule',
        'review',
      ]
    : [
        'setup',
        'voice',
        'scenario',
        'telephony',
        'tags',
        'audience',
        'schedule',
        'review',
      ];

  for (const stepKey of order) {
    if (!isStepComplete(campaign, stepKey)) {
      return stepKey;
    }
  }

  return 'review';
};

export const mapCampaignToWizardDraft = (
  campaign: IVishingCampaign,
): IVishingWizardDraft => {
  const voiceData = campaign.voiceData;
  const scenarioId = campaign.voiceScenario?.scenarioId ?? '';

  return {
    setup: toCampaignSetup(campaign),
    voiceSetup: {
      consentConfirmed: voiceData?.consentConfirmed ?? true,
      consentText: voiceData?.consentText ?? '',
      voiceCloneId: voiceData?.voiceCloneId ?? '',
      voiceName: voiceData?.voiceName ?? '',
      fileName: voiceData?.fileName,
      callerId: voiceData?.callerId,
      cloningEngine: voiceData?.cloningEngine ?? 'ELEVENLABS',
      language: voiceData?.language ?? 'en',
    },
    scenarioAttach: {
      scenarioId,
      enableLlmResponses: campaign.voiceScenario?.enableLlmResponses ?? true,
      escalationLimit: campaign.voiceScenario?.escalationLimit ?? 'MEDIUM',
    },
    selectedScenarioId: scenarioId || null,
    telephony: campaign.telephonyData ?? {
      voiceServerConfigurationId: '',
    },
    tags: { tags: campaign.tags ?? [] },
    audience: campaign.audience ?? { audienceType: 'INDIVIDUAL' },
    training: campaign.trainingData ?? {},
    schedule: campaign.schedule ?? {
      scheduleType: 'IMMEDIATELY',
      timezone: '',
    },
    resumeStepKey: inferResumeStepKey(campaign),
    view:
      campaign.status === 'RUNNING' ||
      campaign.status === 'COMPLETED' ||
      campaign.status === 'PAUSED'
        ? 'dashboard'
        : 'wizard',
  };
};

export const scheduleToUiState = (
  schedule: IVishingScheduleRequest,
): {
  when: UiScheduleWhen;
  startDateTime: string;
  timezone: string;
  pattern: UiSendingPattern;
} => {
  const when: UiScheduleWhen =
    schedule.scheduleType === 'IMMEDIATELY'
      ? 'immediate'
      : schedule.scheduleType === 'RECURRING'
        ? 'recurring'
        : 'later';

  const sendingPattern = schedule.sendingConfig?.sendingPattern;
  const pattern: UiSendingPattern =
    sendingPattern === 'STAGGERED' ? 'staggered' : 'all';

  let startDateTime = '';
  if (schedule.startDateTime) {
    const date = new Date(schedule.startDateTime);
    if (!Number.isNaN(date.getTime())) {
      const pad = (n: number) => String(n).padStart(2, '0');
      startDateTime = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
    }
  }

  return {
    when,
    startDateTime,
    timezone: schedule.timezone ?? '',
    pattern,
  };
};

export const audienceTypeToMode = (audienceType?: string): UiAudienceMode => {
  switch (audienceType) {
    case 'ALL_USERS':
      return 'all';
    case 'DEPARTMENTS':
      return 'department';
    case 'RISK_GROUPS':
      return 'risk';
    default:
      return 'individual';
  }
};

export const stepKeyToIndex = (
  steps: { key: StepKey; id: number }[],
  stepKey: StepKey,
): number => steps.find(step => step.key === stepKey)?.id ?? 1;

export const parseStepKey = (value: string | null): StepKey | null => {
  const allowed: StepKey[] = [
    'setup',
    'voice',
    'scenario',
    'telephony',
    'tags',
    'audience',
    'training',
    'schedule',
    'review',
    'live',
  ];
  return allowed.includes(value as StepKey) ? (value as StepKey) : null;
};
