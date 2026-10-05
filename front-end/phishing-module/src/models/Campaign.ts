/**
 * Campaign TypeScript interfaces and enums
 */

// ============ Enums ============

export enum CampaignType {
  SIMULATED_PHISHING = 'SIMULATED_PHISHING',
  PHISHING_WITH_TRAINING = 'PHISHING_WITH_TRAINING',
  SMISHING_SIMULATION = 'SMISHING_SIMULATION',
  SMISHING_WITH_TRAINING = 'SMISHING_WITH_TRAINING',
  VISHING_WITH_TRAINING = 'VISHING_WITH_TRAINING',
  VISHING_SIMULATION = 'VISHING_SIMULATION',
}

export enum CampaignChannel {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  VOICE = 'VOICE',
}

export enum CampaignLearningMode {
  MICRO_CONTENT = 'MICRO_CONTENT',
}

export enum PhishingAssignedFor {
  SIMULATED_PHISHING = 'SIMULATED_PHISHING',
  PHISHING_TRAINING_FOR_CLICKS = 'PHISHING_TRAINING_FOR_CLICKS',
  PHISHING_TRAINING_FOR_COMPROMISES = 'PHISHING_TRAINING_FOR_COMPROMISES',
  PHISHING_TRAINING_FOR_ALL = 'PHISHING_TRAINING_FOR_ALL',
}

export enum CampaignStatus {
  DRAFT = 'DRAFT',
  SCHEDULED = 'SCHEDULED',
  RUNNING = 'RUNNING',
  PAUSED = 'PAUSED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export enum AudienceType {
  ALL_USERS = 'ALL_USERS',
  DEPARTMENTS = 'DEPARTMENTS',
  GROUPS = 'GROUPS',
  INDIVIDUAL = 'INDIVIDUAL',
}

export enum ScheduleType {
  IMMEDIATELY = 'IMMEDIATELY',
  SCHEDULED = 'SCHEDULED',
  RECURRING = 'RECURRING',
}

export enum SendingPattern {
  ALL_AT_ONCE = 'ALL_AT_ONCE',
  STAGGERED = 'STAGGERED',
}

/** How long phishing simulation tracking stays active before expiring */
export enum CampaignValidityUnit {
  DAYS = 'DAYS',
  WEEKS = 'WEEKS',
  MONTHS = 'MONTHS',
}

export enum RecipientStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  DELIVERED = 'DELIVERED',
  BOUNCED = 'BOUNCED',
  OPENED = 'OPENED',
  CLICKED = 'CLICKED',
  DATA_SUBMITTED = 'DATA_SUBMITTED',
  REPORTED = 'REPORTED',
}

// ============ Interfaces ============

export interface ICampaignExpireDate {
  validityUnit: CampaignValidityUnit;
  validityPeriod: number;
}

export interface ICampaignAudience {
  type: AudienceType;
  departmentIds: string[];
  departmentNames?: string[];
  groupIds: string[];
  groupNames?: string[];
  userIds: string[];
  recipientCount: number;
}

export interface ICampaignSchedule {
  type: ScheduleType;
  startDateTime?: string;
  endDateTime?: string;
  timezone?: string;
  /** API alias for timezone display name or id */
  timeZone?: string;
  sendingPattern: SendingPattern;
  batchSize?: number;
  batchIntervalMinutes?: number;
  recurringFrequency?: string;
  recurringDescription?: string;
}

export interface ICampaignStats {
  attachmentsOpened: number;
  callsAnswered: number;
  callsCompromised: number;
  callsFailed: number;
  callsNoAnswer: number;
  callsTotal: number;
  clickRate: number;
  dataSubmitted: number;
  deliveryRate: number;
  emailsBounced: number;
  emailsDelivered: number;
  emailsOpened: number;
  emailsReported: number;
  emailsSent: number;
  lastUpdatedAt: string;
  linksClicked: number;
  openRate: number;
  reportRate: number;
  retriesTriggered: number;
  smsDelivered: number;
  smsFailed: number;
  smsSent: number;
  submissionRate: number;
  totalRecipients: number;
}

export interface ICampaignCompletionDays {
  durationUnit: string;
  durationValue: number;
}

export interface ICampaignTrainingData {
  trainingModuleId?: string;
  name?: string;
  subPackageName?: string;
  description?: string;
  productId?: string;
  packageId?: string;
  productPackageId?: string;
  clientId?: string;
  topicId?: string[];
  topicIdDetails?: Array<{ tid: string; topicName?: string }>;
  completionDays?: ICampaignCompletionDays;
}

export interface ICampaignVoiceScenario {
  scenarioId?: string;
  scenarioName?: string;
  scriptBody?: string;
}

export interface ICampaign {
  campaignId: string;
  campaignName: string;
  campaignType: CampaignType;
  channel?: CampaignChannel;
  productPackageId?: string;
  assignedFor: PhishingAssignedFor;
  learningMode?: CampaignLearningMode;
  expireDate?: ICampaignExpireDate;
  status: CampaignStatus;
  emailTemplateId?: string;
  emailTemplateName?: string;
  landingPageId?: string;
  landingPageName?: string;
  landingPageType?: string;
  senderProfileId?: string;
  senderProfileName?: string;
  smsServerConfigurationId?: string;
  smsServerConfigurationName?: string;
  voiceScenario?: ICampaignVoiceScenario;
  campaignTags: string[];
  audience?: ICampaignAudience;
  trainingData?: ICampaignTrainingData;
  schedule?: ICampaignSchedule;
  stats?: ICampaignStats;
  currentStep: number;
  totalSteps: number;
  isComplete: boolean;
  canEdit: boolean;
  canLaunch: boolean;
  canPause: boolean;
  canResume: boolean;
  canCancel: boolean;
  canDelete: boolean;
  createdAt: string;
  updatedAt: string;
  launchedAt?: string;
  completedAt?: string;
  createdBy?: string;
}

export interface ICampaignRecipient {
  recipientId: string;
  campaignId: string;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  department?: string;
  phoneNumber?: string;
  status: RecipientStatus;
  emailSentAt?: string;
  emailOpenedAt?: string;
  linkClickedAt?: string;
  dataSubmittedAt?: string;
  reportedAt?: string;
  openCount: number;
  clickCount: number;
  hasSubmittedData: boolean;
}

// ============ Form Interfaces ============

export interface ICampaignCreateForm {
  campaignName: string;
  campaignType: CampaignType;
  channel: CampaignChannel;
  assignedFor: PhishingAssignedFor;
  productPackageId: string;
  learningMode: CampaignLearningMode;
  expireDate: ICampaignExpireDate;
}

export interface ICampaignEmailTemplateForm {
  emailTemplateId: string;
  templateLanguage?: string;
}

export interface ICampaignLandingPageForm {
  landingPageId?: string;
  landingPageType?: string;
}

export interface ICampaignSenderProfileForm {
  senderProfileId: string;
}

export interface ICampaignTagsForm {
  tags: string[];
}

export interface ICampaignAudienceForm {
  audienceType: AudienceType;
  departmentIds: string[];
  groupIds: string[];
  userIds: string[];
}

export interface IAllocateLicenceRequest {
  audienceType: AudienceType;
  departmentIds: string[];
  groupIds: string[];
  userIds: string[];
  confirm: boolean;
}

export interface IAllocateLicenceResult {
  licenseCount: number;
  usedLicenseCount: number;
  availableLicenseCount: number;
  selectedUserCount: number;
  existingLicensedUserCount: number;
  newLicenseRequiredCount: number;
  newLicenseAllocatedCount: number;
  requiresConfirmation: boolean;
  confirmationMessage: string | null;
  userIds: string[];
}

export interface ICampaignTrainingForm {
  /** Licensed package name */
  name?: string;
  /** User-entered training bundle name */
  subPackageName?: string;
  description?: string;
  productId?: string;
  packageId?: string;
  productPackageId?: string;
  clientId?: string;
  /** Selected training topic IDs */
  topicId?: string[];
  topicIdDetails?: Array<{ tid: string; topicName?: string }>;
  completionDays?: ICampaignCompletionDays;
  trainingModuleId?: string;
  trainingModuleIds?: string[];
}

export interface ICampaignScheduleForm {
  scheduleType: ScheduleType;
  startDateTime?: string;
  endDateTime?: string;
  timezone: string;
  sendingPattern: SendingPattern;
  batchSize?: number;
  batchIntervalMinutes?: number;
  recurringFrequency?: string;
  daysOfWeek?: number[];
  dayOfMonth?: number;
  timeOfDay?: string;
  repeatCount?: number;
  neverExpires?: boolean;
}

// ============ Helper Functions ============

export const formatCampaignExpireSummary = (
  expire?: ICampaignExpireDate,
): string | undefined => {
  if (!expire?.validityPeriod || expire.validityPeriod < 1) return undefined;
  const unit =
    expire.validityUnit === CampaignValidityUnit.MONTHS ? 'month' : 'day';
  const plural = expire.validityPeriod === 1 ? '' : 's';
  return `${expire.validityPeriod} ${unit}${plural}`;
};

export const getCampaignTypeLabel = (type: CampaignType): string => {
  switch (type) {
    case CampaignType.SIMULATED_PHISHING:
      return 'Simulated Phishing';
    case CampaignType.PHISHING_WITH_TRAINING:
      return 'Phishing with Training';
    case CampaignType.SMISHING_SIMULATION:
      return 'Smishing Simulation';
    case CampaignType.SMISHING_WITH_TRAINING:
      return 'Smishing with Training';
    case CampaignType.VISHING_WITH_TRAINING:
      return 'Vishing with Training';
    case CampaignType.VISHING_SIMULATION:
      return 'Vishing Simulation';
    default:
      return type;
  }
};

export const getCampaignStatusLabel = (status: CampaignStatus): string => {
  switch (status) {
    case CampaignStatus.DRAFT:
      return 'Draft';
    case CampaignStatus.SCHEDULED:
      return 'Scheduled';
    case CampaignStatus.RUNNING:
      return 'Running';
    case CampaignStatus.PAUSED:
      return 'Paused';
    case CampaignStatus.COMPLETED:
      return 'Completed';
    case CampaignStatus.CANCELLED:
      return 'Cancelled';
    default:
      return status;
  }
};

export const getCampaignStatusColor = (status: CampaignStatus): string => {
  switch (status) {
    case CampaignStatus.DRAFT:
      return 'bg-gray-100 text-gray-800';
    case CampaignStatus.SCHEDULED:
      return 'bg-blue-100 text-blue-800';
    case CampaignStatus.RUNNING:
      return 'bg-green-100 text-green-800';
    case CampaignStatus.PAUSED:
      return 'bg-yellow-100 text-yellow-800';
    case CampaignStatus.COMPLETED:
      return 'bg-purple-100 text-purple-800';
    case CampaignStatus.CANCELLED:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

export const getCampaignChannelLabel = (channel: CampaignChannel): string => {
  switch (channel) {
    case CampaignChannel.EMAIL:
      return 'Email';
    case CampaignChannel.SMS:
      return 'SMS';
    case CampaignChannel.VOICE:
      return 'Voice';
    default:
      return channel;
  }
};

export const getCampaignChannelColor = (channel: CampaignChannel): string => {
  switch (channel) {
    case CampaignChannel.EMAIL:
      return 'bg-blue-100 text-blue-800';
    case CampaignChannel.SMS:
      return 'bg-violet-100 text-violet-800';
    case CampaignChannel.VOICE:
      return 'bg-orange-100 text-orange-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

export const getAudienceTypeLabel = (type: AudienceType): string => {
  switch (type) {
    case AudienceType.ALL_USERS:
      return 'All Users';
    case AudienceType.DEPARTMENTS:
      return 'By Department';
    case AudienceType.GROUPS:
      return 'By Group';
    case AudienceType.INDIVIDUAL:
      return 'Individual Selection';
    default:
      return type;
  }
};

export const getScheduleTypeLabel = (type: ScheduleType): string => {
  switch (type) {
    case ScheduleType.IMMEDIATELY:
      return 'Send Immediately';
    case ScheduleType.SCHEDULED:
      return 'Schedule for Later';
    case ScheduleType.RECURRING:
      return 'Recurring';
    default:
      return type;
  }
};

export const getRecipientStatusLabel = (status: RecipientStatus): string => {
  switch (status) {
    case RecipientStatus.PENDING:
      return 'Pending';
    case RecipientStatus.SENT:
      return 'Sent';
    case RecipientStatus.DELIVERED:
      return 'Delivered';
    case RecipientStatus.BOUNCED:
      return 'Bounced';
    case RecipientStatus.OPENED:
      return 'Opened';
    case RecipientStatus.CLICKED:
      return 'Clicked';
    case RecipientStatus.DATA_SUBMITTED:
      return 'Compromised';
    case RecipientStatus.REPORTED:
      return 'Reported';
    default:
      return status;
  }
};

export const getRecipientStatusColor = (status: RecipientStatus): string => {
  switch (status) {
    case RecipientStatus.PENDING:
      return 'bg-gray-100 text-gray-800';
    case RecipientStatus.SENT:
      return 'bg-blue-100 text-blue-800';
    case RecipientStatus.DELIVERED:
      return 'bg-cyan-100 text-cyan-800';
    case RecipientStatus.BOUNCED:
      return 'bg-red-100 text-red-800';
    case RecipientStatus.OPENED:
      return 'bg-yellow-100 text-yellow-800';
    case RecipientStatus.CLICKED:
      return 'bg-orange-100 text-orange-800';
    case RecipientStatus.DATA_SUBMITTED:
      return 'bg-red-200 text-red-900';
    case RecipientStatus.REPORTED:
      return 'bg-green-100 text-green-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

// Wizard step configuration
export const WIZARD_STEPS = [
  { step: 1, name: 'Setup', description: 'Campaign name and type' },
  { step: 2, name: 'Email Template', description: 'Select phishing email' },
  { step: 3, name: 'Landing Page', description: 'Select landing page' },
  { step: 4, name: 'Mail Server', description: 'Sender profile' },
  { step: 5, name: 'Tags', description: 'Campaign tags' },
  { step: 6, name: 'Audience', description: 'Select recipients' },
  { step: 7, name: 'Training', description: 'Training module' },
  { step: 8, name: 'Schedule', description: 'Timing configuration' },
  { step: 9, name: 'Review', description: 'Review and launch' },
];

export const CAMPAIGN_TYPE_OPTIONS = [
  {
    value: CampaignType.SIMULATED_PHISHING,
    label: 'Simulated Phishing',
    description: 'Send phishing simulation emails without training',
  },
  {
    value: CampaignType.PHISHING_WITH_TRAINING,
    label: 'Phishing with Training',
    description: 'Send phishing emails and assign training to users who fail',
  },
];

export const SMS_CAMPAIGN_TYPE_OPTIONS = [
  {
    value: CampaignType.SMISHING_SIMULATION,
    label: 'Smishing Simulation',
    description: 'Send smishing simulation SMS without training',
  },
  {
    value: CampaignType.SMISHING_WITH_TRAINING,
    label: 'Smishing with Training',
    description: 'Send smishing SMS and assign training to users who fail',
  },
];

export const SCHEDULE_TYPE_OPTIONS = [
  {
    value: ScheduleType.IMMEDIATELY,
    label: 'Send Immediately',
    description: 'Start sending emails right away',
  },
  {
    value: ScheduleType.SCHEDULED,
    label: 'Schedule for Later',
    description: 'Choose a specific date and time',
  },
  {
    value: ScheduleType.RECURRING,
    label: 'Recurring',
    description: 'Repeat on a schedule',
  },
];

export const SENDING_PATTERN_OPTIONS = [
  {
    value: SendingPattern.ALL_AT_ONCE,
    label: 'All at Once',
    description: 'Send all emails simultaneously',
  },
  {
    value: SendingPattern.STAGGERED,
    label: 'Staggered',
    description: 'Send in batches over time',
  },
];

export const STATUS_FILTERS = [
  { value: 'all', label: 'All Campaigns' },
  { value: CampaignStatus.DRAFT, label: 'Draft' },
  { value: CampaignStatus.SCHEDULED, label: 'Scheduled' },
  { value: CampaignStatus.RUNNING, label: 'Running' },
  { value: CampaignStatus.COMPLETED, label: 'Completed' },
  { value: CampaignStatus.CANCELLED, label: 'Cancelled' },
];

export const CHANNEL_FILTERS = [
  { value: 'all', label: 'All Channels' },
  { value: CampaignChannel.EMAIL, label: 'Email' },
  { value: CampaignChannel.SMS, label: 'SMS' },
  { value: CampaignChannel.VOICE, label: 'Voice' },
];

export const ACTION_BUTTON_LABELS_ENUM = {
  LAUNCH: 'LAUNCH',
  PAUSE: 'PAUSE',
  RESUME: 'RESUME',
  CANCEL: 'CANCEL',
  DELETE: 'DELETE',
} as const;

export type CampaignActionType = keyof typeof ACTION_BUTTON_LABELS_ENUM;

export interface ICampaignActionConfirm {
  type: CampaignActionType;
  campaign: ICampaign;
}

export const ACTION_TEXT: Record<
  CampaignActionType,
  {
    label: string;
    present: string;
    progressive: string;
    past: string;
  }
> = {
  LAUNCH: {
    label: 'Launch',
    present: ACTION_BUTTON_LABELS_ENUM.LAUNCH,
    progressive: 'Launching',
    past: 'launched',
  },
  PAUSE: {
    label: 'Pause',
    present: ACTION_BUTTON_LABELS_ENUM.PAUSE,
    progressive: 'Pausing',
    past: 'paused',
  },
  RESUME: {
    label: 'Resume',
    present: ACTION_BUTTON_LABELS_ENUM.RESUME,
    progressive: 'Resuming',
    past: 'resumed',
  },
  CANCEL: {
    label: 'Cancel',
    present: ACTION_BUTTON_LABELS_ENUM.CANCEL,
    progressive: 'Cancelling',
    past: 'cancelled',
  },
  DELETE: {
    label: 'Delete',
    present: ACTION_BUTTON_LABELS_ENUM.DELETE,
    progressive: 'Deleting',
    past: 'deleted',
  },
};
