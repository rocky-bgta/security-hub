import type { IGetListParams } from 'models/Global';
import { CampaignValidityUnit } from './Campaign';

// ============ Wizard UI types ============

export type StepKey =
  | 'setup'
  | 'voice'
  | 'scenario'
  | 'telephony'
  | 'tags'
  | 'audience'
  | 'training'
  | 'schedule'
  | 'review'
  | 'live';

export type CampaignType = 'VISHING_SIMULATION' | 'VISHING_WITH_TRAINING';
export type ResponseStage = 'CALL_ENGAGED' | 'COMPROMISED';
export type LearningMode = 'MICRO_CONTENT';

export type CampaignSetup = {
  name: string;
  type: CampaignType;
  stages: ResponseStage[];
  learningMode: LearningMode;
  productPackageId: string;
  expireDate: {
    validityUnit: CampaignValidityUnit;
    validityPeriod: number;
  };
};

// ============ Enums ============

export type VishingCampaignChannel = 'VOICE';

export type VishingApiCampaignType =
  | 'VISHING_SIMULATION'
  | 'VISHING_WITH_TRAINING';

export type VishingCampaignStatus =
  | 'DRAFT'
  | 'SCHEDULED'
  | 'RUNNING'
  | 'PAUSED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXPIRED';

export type VoiceResponseStage = 'CALL_ENGAGED' | 'COMPROMISED';

export type VishingLearningMode = 'MICRO_CONTENT';

export type VoiceCloneProvider = 'ELEVENLABS' | 'FISH_AUDIO';

export type {
  VoiceProviderType,
  VoiceServerStatus,
  IVoiceServerConfiguration,
  IVoiceServerTestRequest,
} from './VoiceServerConfiguration';

export type VishingScenarioStatus = 'DRAFT' | 'PUBLISHED';

export type LlmEscalationLimit = 'LOW' | 'MEDIUM' | 'HIGH';

export type VishingCallOutcome =
  | 'ANSWERED'
  | 'ENGAGED'
  | 'COMPROMISED'
  | 'NO_ANSWER'
  | 'FAILED'
  | 'ANSWERED_BUT_REPORTED'
  | 'REPORTED_WITHOUT_ANSWER';

export type VishingRecipientStatus =
  | 'PENDING'
  | 'SENT'
  | 'DELIVERED'
  | 'BOUNCED'
  | 'OPENED'
  | 'CLICKED'
  | 'DATA_SUBMITTED'
  | 'REPORTED'
  | 'CALL_QUEUED'
  | 'CALL_RINGING'
  | 'ANSWERED'
  | 'NO_ANSWER'
  | 'COMPROMISED'
  | 'CALL_FAILED'
  | 'VOICE_ENGAGED';

// ============ Request bodies ============

export interface IVishingCampaignCreateRequest {
  campaignName: string;
  campaignType: VishingApiCampaignType;
  channel: VishingCampaignChannel;
  productPackageId?: string;
  responseStages?: VoiceResponseStage[];
  learningMode?: VishingLearningMode;
  expireDate?: {
    validityUnit: CampaignValidityUnit;
    validityPeriod: number;
  };
}

export interface IVishingVoiceSetupRequest {
  consentConfirmed: boolean;
  consentText?: string;
  voiceCloneId?: string;
  voiceName?: string;
  fileName?: string;
  callerId?: string;
  cloningEngine?: VoiceCloneProvider;
  language?: string;
}

/** Wizard-local voice draft; `audioFile` is uploaded on Save & Continue. */
export interface IVishingVoiceSetupDraft extends IVishingVoiceSetupRequest {
  audioFile?: File | null;
  sampleDuration?: number;
}

export interface IVishingScenarioAttachRequest {
  scenarioId: string;
  enableLlmResponses?: boolean;
  escalationLimit?: LlmEscalationLimit;
}

export interface IVishingScenarioCreateRequest {
  scenarioName: string;
  scriptBody: string;
  description?: string;
  attackTemplateId?: string;
  attackTemplateName?: string;
  language?: string;
  tone?: string;
  role?: string;
  enableLlmResponses?: boolean;
  escalationLimit?: LlmEscalationLimit;
  isGlobal?: boolean;
}

export interface IVishingTelephonyRequest {
  voiceServerConfigurationId: string;
  region?: string;
  countryCode?: string;
  retryPolicy?: {
    maxRetries?: number;
    intervalMinutes?: number;
  };
}

export interface IVishingTagsRequest {
  tags?: string[];
}

export interface IVishingAudienceRequest {
  audienceType?: string;
  departmentIds?: string[];
  groupIds?: string[];
  userIds?: string[];
  riskGroups?: string[];
}

export interface IVishingTrainingRequest {
  name?: string;
  subPackageName?: string;
  description?: string;
  productId?: string;
  packageId?: string;
  productPackageId?: string;
  clientId?: string;
  topicId?: string[];
  topicIdDetails?: Array<{ tid: string; topicName?: string }>;
  trainingModuleId?: string;
  assignedFor?: string;
  completionDays?: {
    durationUnit?: string;
    durationValue?: number;
  };
}

export interface IVishingScheduleRequest {
  scheduleType?: string;
  startDateTime?: string;
  timezone?: string;
  sendingConfig?: Record<string, unknown>;
}

export interface IVishingSuccessKeywordsRequest {
  keywords?: string[];
}

export interface IVishingTeachableMomentRequest {
  recipientId: string;
  message?: string;
}

export interface IVishingTestCallRequest {
  phoneNumber: string;
  recipientId?: string;
}

// ============ Domain DTOs ============

export interface IVishingCampaign {
  campaignId: string;
  campaignName: string;
  campaignType: VishingApiCampaignType;
  channel: VishingCampaignChannel;
  productPackageId?: string;
  status: VishingCampaignStatus;
  responseStages?: VoiceResponseStage[];
  learningMode?: VishingLearningMode;
  expireDate?: {
    validityUnit: CampaignValidityUnit;
    validityPeriod: number;
  };
  canLaunch?: boolean;
  canEdit?: boolean;
  canPause?: boolean;
  canResume?: boolean;
  canCancel?: boolean;
  voiceData?: IVishingVoiceSetupRequest;
  voiceScenario?: IVishingScenarioAttachRequest & {
    scenarioName?: string;
    scriptBody?: string;
  };
  telephonyData?: IVishingTelephonyRequest;
  tags?: string[];
  audience?: IVishingAudienceRequest;
  trainingData?: IVishingTrainingRequest;
  schedule?: IVishingScheduleRequest;
  createdAt?: string;
  updatedAt?: string;
  launchedAt?: string;
  completedAt?: string;
}

export interface IVishingCampaignStats {
  callsTotal?: number;
  callsAnswered?: number;
  callsEngaged?: number;
  callsReported?: number;
  callsCompromised?: number;
  callsNoAnswer?: number;
  callsFailed?: number;
  retriesTriggered?: number;
  trainingAssignedCount?: number;
  trainingCompletedCount?: number;
}

export interface IVishingRecipient {
  recipientId: string;
  campaignId: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  status?: VishingRecipientStatus;
}

export type VishingCallLogStatus = VishingRecipientStatus;

export interface IVishingCallLog {
  id: string;
  recipientId?: string;
  recipientName?: string;
  phoneNumber?: string;
  status?: VishingCallLogStatus | null;
  outcome?: VishingCallOutcome | null;
  transcript?: string | null;
  durationSeconds?: number;
  retries?: number;
  detectedKeywords?: string[];
  sensitiveDataCaptured?: Record<string, unknown>;
  recordingS3Key?: string | null;
  startedAt?: string | null;
  endedAt?: string | null;
}

export interface IVishingDataCapture {
  totalCalls: number;
  compromisedCount: number;
  callLogs: IVishingCallLog[];
}

export interface IVishingRemediation {
  answerRate: number;
  engagedRate: number;
  compromiseRate: number;
  failureRate: number;
  averageRiskScore: number;
  trainingAssignedCount: number;
  trainingCompletedCount: number;
}

export interface IVishingReport {
  campaignId: string;
  callsTotal: number;
  callsAnswered: number;
  callsEngaged: number;
  callsReported: number;
  callsCompromised: number;
  callsNoAnswer: number;
  callsFailed: number;
  retriesTriggered: number;
}

export interface IVishingLiveMetrics {
  activeCalls: number;
  answeredCalls: number;
  engagedCalls: number;
  completedCalls: number;
}

export interface IVishingScenario {
  id: string;
  scenarioName: string;
  status: VishingScenarioStatus;
  scriptBody?: string;
  description?: string;
  attackTemplateId?: string;
  attackTemplateName?: string;
  language?: string;
  tone?: string;
  role?: string;
  enableLlmResponses?: boolean;
  escalationLimit?: LlmEscalationLimit;
  isGlobal?: boolean;
}

export interface IVishingAttackTemplate {
  id: string;
  name: string;
  script: string;
  variables?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface IVishingAttackTemplateRequest {
  name: string;
  script: string;
}

export interface IVishingEndUser {
  id: string;
  fullName?: string;
  email?: string;
  phoneNumber?: string;
  department?: string;
  riskGroup?: string;
  status?: string;
}

export type VishingVoiceStatus =
  | 'DRAFT'
  | 'COMPLETED'
  | 'PENDING'
  | 'PROCESSING'
  | 'FAILED'
  | string;

export interface IVishingVoice {
  voiceCloneId: string;
  voiceName?: string;
  provider: VoiceCloneProvider | string;
  fileName: string;
  sampleUrl: string;
  language: string;
  status: VishingVoiceStatus;
  usedFallbackVoice: boolean;
  createdAt: string;
}

export type IVishingVoiceListParams = {
  offset?: number;
  pageSize?: number;
  provider?: VoiceCloneProvider | '';
  language?: string;
  status?: VishingVoiceStatus | '';
  search?: string;
};

// ============ List query params ============

export type IVishingCampaignListParams = IGetListParams & {
  status?: VishingCampaignStatus | '';
  channel?: VishingCampaignChannel;
  sortOrder?: 'asc' | 'desc';
};
