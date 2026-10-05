import { Button } from 'common/Button';
import { ICampaign, getCampaignTypeLabel } from 'models/Campaign';
import {
  EditIcon,
  EyeIcon,
  PauseIcon,
  PlayIcon,
  TrashIcon,
  XIcon,
} from 'lucide-react';
import { ReactNode } from 'react';
import { CampaignStatusBadge } from './CampaignStatusBadge';

export interface CampaignCardProps {
  campaign: ICampaign;
  onView: (campaign: ICampaign) => void;
  onEdit?: (campaign: ICampaign) => void;
  onDelete?: (campaign: ICampaign) => void;
  onLaunch?: (campaign: ICampaign) => void;
  onPause?: (campaign: ICampaign) => void;
  onResume?: (campaign: ICampaign) => void;
  onCancel?: (campaign: ICampaign) => void;
}

export function formatCampaignCardDate(dateString?: string) {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function CampaignCardStat({
  value,
  label,
  valueClassName = 'text-foreground',
}: {
  value: number;
  label: string;
  valueClassName?: string;
}) {
  return (
    <div>
      <div className={`text-lg font-semibold ${valueClassName}`}>{value}</div>
      <div className="text-xs text-muted-foreground">{label}</div>
    </div>
  );
}

export function CampaignCardProgress({
  label,
  percent,
}: {
  label: string;
  percent: number;
}) {
  return (
    <div className="mt-3">
      <div className="mb-1 flex justify-between text-xs text-muted-foreground">
        <span>{label}</span>
        <span>{percent}%</span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-white/20">
        <div
          className="h-full rounded-full bg-primary transition-all duration-300"
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
}

export function CampaignCardShell({
  campaign,
  onView,
  children,
  ...actions
}: CampaignCardProps & { children: ReactNode }) {
  return (
    <div className="rounded-lg border border-card-border shadow-sm transition-shadow hover:shadow-md">
      <div className="border-b border-card-border p-4">
        <div className="flex items-start justify-between">
          <div className="min-w-0 flex-1">
            <h3
              className="cursor-pointer truncate text-lg font-semibold text-foreground hover:text-primary"
              onClick={() => onView(campaign)}
            >
              {campaign.campaignName}
            </h3>
            <p className="mt-1 text-sm text-muted-foreground">
              {getCampaignTypeLabel(campaign.campaignType)}
            </p>
          </div>
          <CampaignStatusBadge status={campaign.status} />
        </div>
      </div>

      <div className="p-4">{children}</div>

      <div className="flex items-center justify-between p-4">
        <div className="text-xs text-muted-foreground">
          Created: {formatCampaignCardDate(campaign.createdAt)}
        </div>
        <CampaignCardActions
          campaign={campaign}
          onView={onView}
          {...actions}
        />
      </div>
    </div>
  );
}

function CampaignCardActions({
  campaign,
  onView,
  onEdit,
  onDelete,
  onLaunch,
  onPause,
  onResume,
  onCancel,
}: CampaignCardProps) {
  return (
    <div className="flex items-center space-x-2">
      {campaign.canEdit && onEdit && (
        <Button
          variant="ghost"
          onClick={() => onEdit(campaign)}
          className="h-max rounded p-1.5 text-muted-foreground hover:bg-blue-50 hover:text-blue-600"
          title="Edit"
        >
          <EditIcon className="size-4" />
        </Button>
      )}

      {campaign.canLaunch && onLaunch && (
        <Button
          variant="ghost"
          onClick={() => onLaunch(campaign)}
          className="h-max rounded p-1.5 text-muted-foreground hover:bg-green-50 hover:text-green-600"
          title="Launch"
        >
          <PlayIcon className="size-4" />
        </Button>
      )}

      {campaign.canPause && onPause && (
        <Button
          variant="ghost"
          onClick={() => onPause(campaign)}
          className="h-max rounded p-1.5 text-muted-foreground hover:bg-yellow-50 hover:text-yellow-600"
          title="Pause"
        >
          <PauseIcon className="size-4" />
        </Button>
      )}

      {campaign.canResume && onResume && (
        <Button
          variant="ghost"
          onClick={() => onResume(campaign)}
          className="h-max rounded p-1.5 text-muted-foreground hover:bg-green-50 hover:text-green-600"
          title="Resume"
        >
          <PlayIcon className="size-4" />
        </Button>
      )}

      {campaign.canCancel && onCancel && (
        <Button
          variant="ghost"
          onClick={() => onCancel(campaign)}
          className="h-max rounded p-1.5 text-muted-foreground hover:bg-red-50 hover:text-red-600"
          title="Cancel"
        >
          <XIcon className="size-4" />
        </Button>
      )}

      {campaign.canDelete && onDelete && (
        <Button
          variant="ghost"
          onClick={() => onDelete(campaign)}
          className="h-max rounded p-1.5 text-muted-foreground hover:bg-red-50 hover:text-red-600"
          title="Delete"
        >
          <TrashIcon className="size-4" />
        </Button>
      )}

      <Button
        variant="ghost"
        onClick={() => onView(campaign)}
        className="h-max rounded p-1.5 text-muted-foreground hover:bg-blue-50 hover:text-primary"
        title="View Details"
      >
        <EyeIcon className="size-4" />
      </Button>
    </div>
  );
}
