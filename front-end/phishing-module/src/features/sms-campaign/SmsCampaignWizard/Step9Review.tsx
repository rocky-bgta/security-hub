import { Button } from 'common/Button';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  LoaderIcon,
  MessageSquare,
  PencilIcon,
  Server,
} from 'lucide-react';
import { useAPI } from 'hooks/UseAPI';
import {
  formatCampaignExpireSummary,
  getAudienceTypeLabel,
  getCampaignTypeLabel,
  getScheduleTypeLabel,
  ICampaign,
} from 'models/Campaign';
import { IEmailTemplate } from 'models/EmailTemplate';
import { ILandingPage } from 'models/LandingPage';
import { ISmsServerConfiguration } from 'models/SmsServerConfiguration';
import { ReactNode, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { humanizeText } from 'utils/Helper';

function smsTemplateDisplayName(c: ICampaign): string | undefined {
  const r = c as unknown as Record<string, unknown>;
  const raw =
    c.emailTemplateName ??
    (typeof r.email_template_name === 'string'
      ? r.email_template_name
      : undefined);
  if (raw === undefined || raw === null) return undefined;
  const t = String(raw).trim();
  return t === '' ? undefined : t;
}

function smsServerDisplayName(c: ICampaign): string | undefined {
  const r = c as unknown as Record<string, unknown>;
  const raw =
    c.smsServerConfigurationName ??
    (typeof r.sms_server_configuration_name === 'string'
      ? r.sms_server_configuration_name
      : undefined);
  if (raw === undefined || raw === null) return undefined;
  const t = String(raw).trim();
  return t === '' ? undefined : t;
}

function landingPageDisplayName(c: ICampaign): string | undefined {
  const r = c as unknown as Record<string, unknown>;
  const raw =
    c.landingPageName ??
    (typeof r.landing_page_name === 'string' ? r.landing_page_name : undefined);
  if (raw === undefined || raw === null) return undefined;
  const t = String(raw).trim();
  return t === '' ? undefined : t;
}

function unwrapEntity<T>(res: unknown): T | undefined {
  if (!res || typeof res !== 'object') return undefined;
  const outer = (res as { data?: unknown }).data;
  if (!outer || typeof outer !== 'object') return undefined;
  if ('templateName' in outer || 'name' in outer) {
    return outer as T;
  }
  const inner = (outer as { data?: unknown }).data;
  if (inner && typeof inner === 'object') {
    return inner as T;
  }
  return undefined;
}

interface Step9ReviewProps {
  campaign: ICampaign;
  onLaunch: () => void;
  onSaveAsDraft: () => void;
  onEditStep: (step: number) => void;
  onBack: () => void;
  isLaunching?: boolean;
  isSaving?: boolean;
}

interface ReviewCardProps {
  title: string;
  step: number;
  onEditStep: (step: number) => void;
  children: ReactNode;
}

const ReviewCard = ({ title, step, onEditStep, children }: ReviewCardProps) => (
  <div className="rounded-lg border border-card-border bg-card-background p-4">
    <div className="mb-3 flex items-center justify-between">
      <h4 className="font-medium text-foreground">{title}</h4>
      <Button type="button" variant="outline" onClick={() => onEditStep(step)}>
        <PencilIcon className="size-4" />
        Edit
      </Button>
    </div>
    {children}
  </div>
);

export const Step9Review = ({
  campaign,
  onLaunch,
  onSaveAsDraft,
  onEditStep,
  onBack,
  isLaunching,
  isSaving,
}: Step9ReviewProps) => {
  const api = useAPI();
  const [resolvedSmsTemplateName, setResolvedSmsTemplateName] = useState<
    string | null
  >(null);
  const [resolvedSmsServerName, setResolvedSmsServerName] = useState<
    string | null
  >(null);
  const [resolvedLandingPageName, setResolvedLandingPageName] = useState<
    string | null
  >(null);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      await Promise.resolve();
      if (cancelled) return;
      const fromCampaign = smsTemplateDisplayName(campaign);
      if (fromCampaign) {
        setResolvedSmsTemplateName(null);
        return;
      }
      const id = campaign.emailTemplateId;
      if (!id) {
        setResolvedSmsTemplateName(null);
        return;
      }
      try {
        const res = await api.get(API_END_POINTS.EMAIL_TEMPLATE_DETAILS(id));
        const tpl = unwrapEntity<IEmailTemplate>(res);
        const name = tpl?.templateName?.trim();
        if (!cancelled) {
          setResolvedSmsTemplateName(name ?? null);
        }
      } catch {
        if (!cancelled) setResolvedSmsTemplateName(null);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [api, campaign]);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      await Promise.resolve();
      if (cancelled) return;
      const fromCampaign = smsServerDisplayName(campaign);
      if (fromCampaign) {
        setResolvedSmsServerName(null);
        return;
      }
      const id = campaign.smsServerConfigurationId;
      if (!id) {
        setResolvedSmsServerName(null);
        return;
      }
      try {
        const res = await api.get(
          API_END_POINTS.SMS_SERVER_CONFIGURATION_DETAILS(id),
        );
        const config = unwrapEntity<ISmsServerConfiguration>(res);
        const name = config?.name?.trim();
        if (!cancelled) {
          setResolvedSmsServerName(name ?? null);
        }
      } catch {
        if (!cancelled) setResolvedSmsServerName(null);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [api, campaign]);

  useEffect(() => {
    let cancelled = false;
    void (async () => {
      await Promise.resolve();
      if (cancelled) return;
      const fromCampaign = landingPageDisplayName(campaign);
      if (fromCampaign) {
        setResolvedLandingPageName(null);
        return;
      }
      const id = campaign.landingPageId;
      if (!id) {
        setResolvedLandingPageName(null);
        return;
      }
      try {
        const res = await api.get(API_END_POINTS.LANDING_PAGE_DETAILS(id));
        const page = unwrapEntity<ILandingPage>(res);
        const name = page?.name?.trim();
        if (!cancelled) {
          setResolvedLandingPageName(name ?? null);
        }
      } catch {
        if (!cancelled) setResolvedLandingPageName(null);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [api, campaign]);

  const smsTemplateLabel =
    smsTemplateDisplayName(campaign) ??
    (campaign.emailTemplateId ? resolvedSmsTemplateName : undefined);
  const smsServerLabel =
    smsServerDisplayName(campaign) ??
    (campaign.smsServerConfigurationId ? resolvedSmsServerName : undefined);
  const landingPageNameLabel =
    landingPageDisplayName(campaign) ??
    (campaign.landingPageId ? resolvedLandingPageName : undefined);
  const landingPageReviewText =
    landingPageNameLabel ??
    (campaign.landingPageType
      ? humanizeText(campaign.landingPageType)
      : undefined) ??
    'Not selected';
  const expireSummary = formatCampaignExpireSummary(campaign.expireDate);

  return (
    <div className="mx-auto max-w-4xl">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-foreground">
          Review SMS Campaign
        </h2>
        <p className="mt-2 text-muted-foreground">
          Review your campaign settings before launching.
        </p>
      </div>

      <div className="mb-6 grid grid-cols-2 gap-4">
        <ReviewCard title="Campaign Setup" step={1} onEditStep={onEditStep}>
          <div className="space-y-2">
            <div>
              <span className="text-sm text-muted-foreground">Name: </span>
              <span className="text-sm font-medium text-primary">
                {campaign.campaignName}
              </span>
            </div>
            <div>
              <span className="text-sm text-muted-foreground">Type: </span>
              <span className="text-sm font-medium text-primary">
                {getCampaignTypeLabel(campaign.campaignType)}
              </span>
            </div>
            {expireSummary && (
              <div>
                <span className="text-sm text-muted-foreground">
                  Activity period:{' '}
                </span>
                <span className="text-sm font-medium text-primary">
                  {expireSummary}
                </span>
              </div>
            )}
          </div>
        </ReviewCard>

        <ReviewCard title="SMS Template" step={2} onEditStep={onEditStep}>
          <div className="flex items-center">
            <MessageSquare className="mr-2 size-5 text-muted-foreground" />
            <span className="text-sm font-medium text-primary">
              {smsTemplateLabel || 'Not selected'}
            </span>
          </div>
        </ReviewCard>

        <ReviewCard title="Landing Page" step={3} onEditStep={onEditStep}>
          <div className="flex items-center">
            <svg
              className="mr-2 size-5 text-muted-foreground"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M4 5a1 1 0 011-1h14a1 1 0 011 1v2a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM4 13a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H5a1 1 0 01-1-1v-6z"
              />
            </svg>
            <span className="text-sm font-medium text-primary">
              {landingPageReviewText}
            </span>
          </div>
        </ReviewCard>

        <ReviewCard title="SMS Server" step={4} onEditStep={onEditStep}>
          <div className="flex items-center">
            <Server className="mr-2 size-5 text-muted-foreground" />
            <span className="text-sm font-medium text-primary">
              {smsServerLabel || 'Not selected'}
            </span>
          </div>
        </ReviewCard>

        <ReviewCard title="Tags" step={5} onEditStep={onEditStep}>
          {campaign.campaignTags && campaign.campaignTags.length > 0 ? (
            <div className="flex flex-wrap gap-1">
              {campaign.campaignTags.map(tag => (
                <span
                  key={tag}
                  className="rounded bg-primary/10 px-2 py-0.5 text-xs text-primary"
                >
                  {tag}
                </span>
              ))}
            </div>
          ) : (
            <span className="text-sm text-muted-foreground">No tags</span>
          )}
        </ReviewCard>

        <ReviewCard title="Audience" step={6} onEditStep={onEditStep}>
          <div className="space-y-1">
            <div className="flex items-center">
              <svg
                className="mr-2 size-5 text-muted-foreground"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
              <span className="text-sm font-medium text-primary">
                {campaign.audience?.type
                  ? getAudienceTypeLabel(campaign.audience.type)
                  : 'Not selected'}
              </span>
            </div>
            <div className="ml-7 text-sm text-muted-foreground">
              {campaign.audience?.recipientCount || 0} recipients
            </div>
          </div>
        </ReviewCard>

        <ReviewCard title="Schedule" step={8} onEditStep={onEditStep}>
          <div className="space-y-1">
            <div className="text-sm font-medium text-primary">
              {campaign.schedule?.type
                ? getScheduleTypeLabel(campaign.schedule.type)
                : 'Not configured'}
            </div>
            {campaign.schedule?.startDateTime && (
              <div className="text-sm text-muted-foreground">
                {new Date(campaign.schedule.startDateTime).toLocaleString()}
              </div>
            )}
          </div>
        </ReviewCard>
      </div>

      <div className="mb-6 rounded-lg border border-card-border bg-card-background p-4">
        <div className="flex">
          <svg
            className="mr-3 mt-0.5 size-5 shrink-0 text-primary"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              fillRule="evenodd"
              d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
              clipRule="evenodd"
            />
          </svg>
          <div>
            <h4 className="text-sm font-medium text-foreground">
              Before you launch
            </h4>
            <p className="mt-1 text-sm text-muted-foreground">
              Once launched, SMS messages will be sent to{' '}
              {campaign.audience?.recipientCount || 0} recipients. Make sure all
              settings are correct.
            </p>
          </div>
        </div>
      </div>

      <div className="flex justify-between border-t border-card-border pt-6">
        <Button type="button" variant="outline" onClick={onBack}>
          <ArrowLeftIcon className="size-4" />
          Back
        </Button>
        <div className="flex space-x-3">
          <Button type="button" onClick={onSaveAsDraft} disabled={isSaving}>
            {isSaving ? 'Saving...' : 'Save as Draft'}
          </Button>
          <Button
            type="button"
            onClick={onLaunch}
            disabled={isLaunching || !campaign.canLaunch}
          >
            {isLaunching ? (
              <>
                <LoaderIcon className="-ml-1 mr-2 size-4 animate-spin" />
                Launching...
              </>
            ) : (
              <>
                Launch Campaign
                <ArrowRightIcon className="size-4" />
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};
