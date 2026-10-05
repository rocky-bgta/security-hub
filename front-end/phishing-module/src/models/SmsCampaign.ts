/**
 * SMS campaign wizard configuration — campaigns use unified ICampaign with channel SMS.
 */

export type { ICampaign } from 'models/Campaign';

export {
  CampaignType,
  SMS_CAMPAIGN_TYPE_OPTIONS,
  getCampaignTypeLabel,
} from 'models/Campaign';

export type {
  ICampaignAudienceForm,
  ICampaignScheduleForm,
  ICampaignTagsForm,
  ICampaignTrainingForm,
} from 'models/Campaign';

export {
  AudienceType,
  CampaignStatus,
  CampaignValidityUnit,
  PhishingAssignedFor,
  ScheduleType,
  SendingPattern,
  formatCampaignExpireSummary,
  getAudienceTypeLabel,
  getCampaignStatusColor,
  getCampaignStatusLabel,
  getScheduleTypeLabel,
} from 'models/Campaign';

export interface ISmsCampaignServerForm {
  smsServerConfigurationId: string;
}

export const SMS_WIZARD_STEPS = [
  { step: 1, name: 'Setup', description: 'Campaign name and type' },
  { step: 2, name: 'SMS Template', description: 'Select phishing SMS' },
  { step: 3, name: 'Landing Page', description: 'Select landing page' },
  { step: 4, name: 'SMS Server', description: 'Sender configuration' },
  { step: 5, name: 'Tags', description: 'Campaign tags' },
  { step: 6, name: 'Audience', description: 'Select recipients' },
  { step: 7, name: 'Training', description: 'Training module' },
  { step: 8, name: 'Schedule', description: 'Timing configuration' },
  { step: 9, name: 'Review', description: 'Review and launch' },
];
