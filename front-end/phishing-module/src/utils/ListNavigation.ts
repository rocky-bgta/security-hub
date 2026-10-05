import { CampaignChannel } from 'models/Campaign';
import { TemplateType } from 'models/EmailTemplate';
import { SenderConfigurationType } from 'models/SmsServerConfiguration';
import { routes } from 'routes/Routes';
import type { SimulationChannel } from 'utils/SimulationChannel';

export const CHANNEL_QUERY_KEY = 'channel';
export const TEMPLATE_TYPE_QUERY_KEY = 'templateType';

export function channelToQueryValue(channel: CampaignChannel): string {
  return channel.toLowerCase();
}

export function parseChannelQueryValue(
  value: string | null,
): CampaignChannel | '' {
  if (!value) {
    return '';
  }

  const normalized = value.toUpperCase();
  if (Object.values(CampaignChannel).includes(normalized as CampaignChannel)) {
    return normalized as CampaignChannel;
  }

  return '';
}

export function getCampaignListUrl(channel?: CampaignChannel | ''): string {
  if (channel === CampaignChannel.SMS) {
    return routes.smishingCampaigns.path;
  }
  if (channel === CampaignChannel.VOICE) {
    return routes.vishingCampaigns.path;
  }
  return routes.phishingCampaings.path;
}

export function getSenderProfileListUrl(
  configurationType?: SenderConfigurationType,
): string {
  if (configurationType === SenderConfigurationType.SMS) {
    return routes.smsServerConfigurations.path;
  }
  return routes.phishingSenderProfiles.path;
}

export function parseTemplateTypeQueryValue(
  value: string | null,
): TemplateType {
  if (value === TemplateType.SMS) {
    return TemplateType.SMS;
  }

  return TemplateType.EMAIL;
}

export function getTemplateLibraryUrl(templateType?: TemplateType): string {
  if (templateType === TemplateType.SMS) {
    return routes.smsTemplateLibrary.path;
  }

  return routes.phishingTemplateLibrary.path;
}

export function getTemplateCreateUrl(templateType?: TemplateType): string {
  if (templateType === TemplateType.SMS) {
    return routes.smsTemplateLibraryCreate.path;
  }

  return routes.phishingTemplateLibraryCreate.path;
}

export function getTemplateEditUrl(
  templateId: string,
  templateType?: TemplateType,
): string {
  const path =
    templateType === TemplateType.SMS
      ? routes.smsTemplateLibraryEdit.path
      : routes.phishingTemplateLibraryEdit.path;
  return path.replace(':id', templateId);
}

export function getLandingPageChannelFromPath(
  pathname: string,
): SimulationChannel {
  if (pathname.includes('smishing-management')) {
    return 'smishing';
  }
  return 'phishing';
}

export function getLandingPageLibraryUrl(
  channel?: SimulationChannel,
): string {
  if (channel === 'smishing') {
    return routes.smishingLandingPages.path;
  }
  return routes.phishingLandingPages.path;
}

export function getLandingPageCreateUrl(channel?: SimulationChannel): string {
  if (channel === 'smishing') {
    return routes.smishingLandingPageCreate.path;
  }
  return routes.phishingLandingPageCreate.path;
}

export function getLandingPageEditUrl(
  pageId: string,
  channel?: SimulationChannel,
): string {
  const path =
    channel === 'smishing'
      ? routes.smishingLandingPageEdit.path
      : routes.phishingLandingPageEdit.path;
  return path.replace(':id', pageId);
}

export const CLIENT_ADMIN_USERS_PATH = '/user-management/user-list';

export function getCampaignWizardEditPath(
  pathname: string,
  campaignId: string,
): string {
  if (pathname.includes('smishing-management')) {
    return routes.smishingSimulationEdit.path.replace(':id', campaignId);
  }
  if (pathname.includes('vishing-management')) {
    return routes.vishingSimulationEdit.path.replace(':id', campaignId);
  }
  return routes.phishingCampaignEdit.path.replace(':id', campaignId);
}

export function getAudienceReturnToPath(
  pathname: string,
  campaignId: string,
): string {
  const editPath = getCampaignWizardEditPath(pathname, campaignId);
  if (pathname.includes('vishing-management')) {
    return editPath;
  }
  return `${editPath}?step=6`;
}

export function getAllUsersListUrl(
  campaignId: string,
  returnTo: string,
): string {
  const params = new URLSearchParams({
    fromCampaign: campaignId,
    returnTo,
  });
  return `${CLIENT_ADMIN_USERS_PATH}?${params.toString()}`;
}

export function resolveWizardStepFromQuery(
  savedStep: number,
  requestedStep: number,
): number {
  if (
    Number.isInteger(requestedStep) &&
    requestedStep >= 1 &&
    requestedStep <= savedStep
  ) {
    return requestedStep;
  }
  return savedStep;
}
