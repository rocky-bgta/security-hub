import { Button } from 'common/Button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import {
  EditIcon,
  EyeIcon,
  Mail,
  PauseIcon,
  PlayIcon,
  TrashIcon,
  XIcon,
} from 'lucide-react';
import { ICampaign, getCampaignTypeLabel, CampaignChannel } from 'models/Campaign';
import { ReactNode } from 'react';
import { CampaignStatusBadge } from './CampaignStatusBadge';
import { CampaignChannelBadge } from './CampaignChannelBadge';
import {
  fromCampaignChannel,
  getSimulationCopy,
} from 'utils/SimulationChannel';

interface CampaignTableProps {
  campaigns: ICampaign[];
  loading: boolean;
  onView: (campaign: ICampaign) => void;
  onEdit?: (campaign: ICampaign) => void;
  onDelete?: (campaign: ICampaign) => void;
  onLaunch?: (campaign: ICampaign) => void;
  onPause?: (campaign: ICampaign) => void;
  onResume?: (campaign: ICampaign) => void;
  onCancel?: (campaign: ICampaign) => void;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
  onSort: (field: string) => void;
  channel?: CampaignChannel;
}

const SortableHeader = ({
  field,
  children,
  sortBy,
  sortOrder,
  onSort,
}: {
  field: string;
  children: ReactNode;
  sortBy: string;
  sortOrder: 'asc' | 'desc';
  onSort: (field: string) => void;
}) => (
  <TableHead
    className="cursor-pointer hover:bg-white/10"
    onClick={() => onSort(field)}
  >
    <div className="flex items-center space-x-1">
      <span>{children}</span>
      {sortBy === field && (
        <svg className="size-4" fill="currentColor" viewBox="0 0 20 20">
          {sortOrder === 'asc' ? (
            <path
              fillRule="evenodd"
              d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"
              clipRule="evenodd"
            />
          ) : (
            <path
              fillRule="evenodd"
              d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
              clipRule="evenodd"
            />
          )}
        </svg>
      )}
    </div>
  </TableHead>
);

export const CampaignTable = ({
  campaigns,
  loading,
  onView,
  onEdit,
  onDelete,
  onLaunch,
  onPause,
  onResume,
  onCancel,
  sortBy,
  sortOrder,
  onSort,
  channel,
}: CampaignTableProps) => {
  const copy = getSimulationCopy(
    fromCampaignChannel(
      channel ?? campaigns[0]?.channel ?? CampaignChannel.EMAIL,
    ),
  );
  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>
              <div className="h-4 w-24 animate-pulse rounded bg-gray-200" />
            </TableHead>
            <TableHead>
              <div className="h-4 w-16 animate-pulse rounded bg-gray-200" />
            </TableHead>
            <TableHead>
              <div className="h-4 w-20 animate-pulse rounded bg-gray-200" />
            </TableHead>
            <TableHead>
              <div className="h-4 w-16 animate-pulse rounded bg-gray-200" />
            </TableHead>
            <TableHead>
              <div className="h-4 w-20 animate-pulse rounded bg-gray-200" />
            </TableHead>
            <TableHead>
              <div className="h-4 w-16 animate-pulse rounded bg-gray-200" />
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {[1, 2, 3, 4, 5].map(i => (
            <TableRow key={i}>
              <TableCell>
                <div className="h-4 w-40 animate-pulse rounded bg-gray-100" />
              </TableCell>
              <TableCell>
                <div className="h-6 w-16 animate-pulse rounded-full bg-gray-100" />
              </TableCell>
              <TableCell>
                <div className="h-4 w-28 animate-pulse rounded bg-gray-100" />
              </TableCell>
              <TableCell>
                <div className="h-6 w-20 animate-pulse rounded-full bg-gray-100" />
              </TableCell>
              <TableCell>
                <div className="h-4 w-24 animate-pulse rounded bg-gray-100" />
              </TableCell>
              <TableCell>
                <div className="h-4 w-20 animate-pulse rounded bg-gray-100" />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    );
  }

  if (campaigns?.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-6">
        <Mail className="size-10 text-muted-foreground" />
        <h3 className="mt-4 text-lg font-medium text-foreground">
          No campaigns found
        </h3>
        <p className="mt-2 text-muted-foreground">
          Get started by creating your first campaign.
        </p>
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <SortableHeader
            field="campaignName"
            sortBy={sortBy}
            sortOrder={sortOrder}
            onSort={onSort}
          >
            Campaign
          </SortableHeader>
          <TableHead>Channel</TableHead>
          <TableHead>Type</TableHead>
          <TableHead>Status</TableHead>
          <TableHead>Recipients</TableHead>
          <TableHead>{copy.openRateLabel}</TableHead>
          <TableHead>{copy.clickRateLabel}</TableHead>
          <SortableHeader
            field="createdAt"
            sortBy={sortBy}
            sortOrder={sortOrder}
            onSort={onSort}
          >
            Created
          </SortableHeader>
          <TableHead className="text-center">Actions</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {campaigns?.map(campaign => (
          <TableRow key={campaign.campaignId}>
            <TableCell>
              <Button
                variant="link"
                onClick={() => onView(campaign)}
                className="text-sm font-medium"
              >
                {campaign.campaignName}
              </Button>
            </TableCell>
            <TableCell>
              <CampaignChannelBadge channel={campaign.channel} size="sm" />
            </TableCell>
            <TableCell>{getCampaignTypeLabel(campaign.campaignType)}</TableCell>
            <TableCell>
              <CampaignStatusBadge status={campaign.status} size="sm" />
            </TableCell>
            <TableCell>{campaign.stats?.totalRecipients || 0}</TableCell>
            <TableCell>{campaign.stats?.openRate?.toFixed(1) || 0}%</TableCell>
            <TableCell>{campaign.stats?.clickRate?.toFixed(1) || 0}%</TableCell>
            <TableCell>{formatDate(campaign.createdAt)}</TableCell>
            <TableCell className="text-center">
              <div className="flex items-center justify-center">
                {campaign.canEdit && onEdit && (
                  <Button
                    variant="ghost"
                    onClick={() => onEdit(campaign)}
                    className="text-muted-foreground"
                    title="Edit"
                  >
                    <EditIcon className="size-4" />
                  </Button>
                )}
                {campaign.canLaunch && onLaunch && (
                  <Button
                    variant="ghost"
                    onClick={() => onLaunch(campaign)}
                    className="text-muted-foreground hover:text-green-600"
                    title="Launch"
                  >
                    <PlayIcon className="size-4" />
                  </Button>
                )}
                {campaign.canDelete && onDelete && (
                  <Button
                    variant="ghost"
                    onClick={() => onDelete(campaign)}
                    className="text-muted-foreground hover:text-red-600"
                    title="Delete"
                  >
                    <TrashIcon className="size-4" />
                  </Button>
                )}
                <Button
                  variant="ghost"
                  onClick={() => onView(campaign)}
                  className="text-muted-foreground"
                  title="View"
                >
                  <EyeIcon className="size-4" />
                </Button>
                {campaign.canPause && onPause && (
                  <Button
                    variant="ghost"
                    onClick={() => onPause(campaign)}
                    className="text-muted-foreground hover:text-yellow-600"
                    title="Pause"
                  >
                    <PauseIcon className="size-4" />
                  </Button>
                )}
                {campaign.canResume && onResume && (
                  <Button
                    variant="ghost"
                    onClick={() => onResume(campaign)}
                    className="text-muted-foreground hover:text-green-600"
                    title="Resume"
                  >
                    <PlayIcon className="size-4" />
                  </Button>
                )}
                {campaign.canCancel && onCancel && (
                  <Button
                    variant="ghost"
                    onClick={() => onCancel(campaign)}
                    className="text-muted-foreground hover:text-red-600"
                    title="Cancel"
                  >
                    <XIcon className="size-4" />
                  </Button>
                )}
              </div>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};
