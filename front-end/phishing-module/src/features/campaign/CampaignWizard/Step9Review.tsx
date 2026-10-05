import { Button } from 'common/Button';
import {
  ArrowLeftIcon,
  ArrowRightIcon,
  LoaderIcon,
  PencilIcon,
} from 'lucide-react';
import { useAPI } from 'hooks/UseAPI';
import {
  ICampaign,
  formatCampaignExpireSummary,
  getAudienceTypeLabel,
  getCampaignTypeLabel,
  getScheduleTypeLabel,
} from 'models/Campaign';
import { IEmailTemplate } from 'models/EmailTemplate';
import { ILandingPage } from 'models/LandingPage';
import { ISenderProfile } from 'models/SenderProfile';
import { ReactNode, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { humanizeText } from 'utils/Helper';

/** Campaign GET payloads sometimes omit camelCase names or use snake_case. */
function emailTemplateDisplayName(c: ICampaign): string | undefined {
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

function senderProfileDisplayName(c: ICampaign): string | undefined {
  const r = c as unknown as Record<string, unknown>;
  const raw =
    c.senderProfileName ??
    (typeof r.sender_profile_name === 'string'
      ? r.sender_profile_name
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
  if ('templateName' in outer || 'profileName' in outer || 'name' in outer) {
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
  const [resolvedEmailTemplateName, setResolvedEmailTemplateName] = useState<
    string | null
  >(null);
  const [resolvedSenderProfileName, setResolvedSenderProfileName] = useState<
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
      const fromCampaign = emailTemplateDisplayName(campaign);
      if (fromCampaign) {
        setResolvedEmailTemplateName(null);
        return;
      }
      const id = campaign.emailTemplateId;
      if (!id) {
        setResolvedEmailTemplateName(null);
        return;
      }
      try {
        const res = await api.get(API_END_POINTS.EMAIL_TEMPLATE_DETAILS(id));
        const tpl = unwrapEntity<IEmailTemplate>(res);
        const name = tpl?.templateName?.trim();
        if (!cancelled) {
          setResolvedEmailTemplateName(name ?? null);
        }
      } catch {
        if (!cancelled) setResolvedEmailTemplateName(null);
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
      const fromCampaign = senderProfileDisplayName(campaign);
      if (fromCampaign) {
        setResolvedSenderProfileName(null);
        return;
      }
      const id = campaign.senderProfileId;
      if (!id) {
        setResolvedSenderProfileName(null);
        return;
      }
      try {
        const res = await api.get(API_END_POINTS.SENDER_PROFILE_DETAILS(id));
        const p = unwrapEntity<ISenderProfile>(res);
        const name = p?.profileName?.trim();
        if (!cancelled) {
          setResolvedSenderProfileName(name ?? null);
        }
      } catch {
        if (!cancelled) setResolvedSenderProfileName(null);
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

  const emailTemplateLabel =
    emailTemplateDisplayName(campaign) ??
    (campaign.emailTemplateId ? resolvedEmailTemplateName : undefined);
  const senderProfileLabel =
    senderProfileDisplayName(campaign) ??
    (campaign.senderProfileId ? resolvedSenderProfileName : undefined);
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
        <h2 className="text-2xl font-bold text-foreground">Review Campaign</h2>
        <p className="mt-2 text-muted-foreground">
          Review your campaign settings before launching.
        </p>
      </div>

      {/* Campaign Summary */}
      <div className="mb-6 grid grid-cols-2 gap-4">
        {/* Step 1: Setup */}
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

        {/* Step 2: Email Template */}
        <ReviewCard title="Email Template" step={2} onEditStep={onEditStep}>
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
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
            <span className="text-sm font-medium text-primary">
              {emailTemplateLabel || 'Not selected'}
            </span>
          </div>
        </ReviewCard>

        {/* Step 3: Landing Page */}
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

        {/* Step 4: Mail Server */}
        <ReviewCard title="Mail Server" step={4} onEditStep={onEditStep}>
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
                d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2"
              />
            </svg>
            <span className="text-sm font-medium text-primary">
              {senderProfileLabel || 'Not selected'}
            </span>
          </div>
        </ReviewCard>

        {/* Step 5: Tags */}
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

        {/* Step 6: Audience */}
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

        {/* Step 8: Schedule */}
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

      {/* Launch Warning */}
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
              Once launched, emails will be sent to{' '}
              {campaign.audience?.recipientCount || 0} recipients. Make sure all
              settings are correct.
            </p>
          </div>
        </div>
      </div>

      {/* Actions */}
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
