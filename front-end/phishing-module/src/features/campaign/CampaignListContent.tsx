import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import ConfirmDialog from 'components/ConfirmDialog';
import { CampaignCardSkeleton } from 'components/LoadingSkeleton';
import ViewToggle from 'components/ViewToggle';
import { EmailCampaignCard } from './EmailCampaignCard';
import { SmsCampaignCard } from './SmsCampaignCard';
import { VishingCampaignCard } from './VishingCampaignCard';
import { CampaignTable } from './CampaignTable';
import { useCampaigns } from 'hooks/UseCampaigns';
import { Mail, MessageSquare, Phone, PlusIcon, Search } from 'lucide-react';
import {
  ACTION_BUTTON_LABELS_ENUM,
  ACTION_TEXT,
  CampaignActionType,
  CampaignChannel,
  CampaignStatus,
  ICampaign,
  ICampaignActionConfirm,
  STATUS_FILTERS,
} from 'models/Campaign';
import { IGetListParams, IList } from 'models/Global';
import { FormEvent, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';

const PAGE_CONFIG = [
  {
    channel: CampaignChannel.EMAIL,
    label: 'Email Campaigns',
    icon: Mail,
    description: 'Manage phishing email simulation campaigns.',
    cardDescription: 'Create and manage phishing email simulation campaigns.',
    createLabel: 'Create Email Campaign',
    createPath: routes.phishingCampaignCreate.path,
  },
  {
    channel: CampaignChannel.SMS,
    label: 'SMS Campaigns',
    icon: MessageSquare,
    description: 'Manage smishing simulation campaigns.',
    cardDescription: 'Create and manage smishing simulation campaigns.',
    createLabel: 'Create SMS Campaign',
    createPath: routes.smishingSimulationCreate.path,
  },
  {
    channel: CampaignChannel.VOICE,
    label: 'Vishing Campaigns',
    icon: Phone,
    description: 'Manage vishing simulation campaigns.',
    cardDescription: 'Create and manage vishing simulation campaigns.',
    createLabel: 'Create Vishing Simulation',
    createPath: routes.vishingSimulationCreate.path,
  },
] as const;

function getViewModeKey(channel: CampaignChannel): string {
  return `campaignListViewMode_${channel}`;
}

interface CampaignListContentProps {
  channel: CampaignChannel;
}

/**
 * Shared campaign list UI for a single channel (email, SMS, or voice).
 */
const CampaignListContent = ({ channel }: CampaignListContentProps) => {
  const navigate = useNavigate();
  const {
    fetchCampaigns,
    deleteCampaign,
    launchCampaign,
    pauseCampaign,
    resumeCampaign,
    cancelCampaign,
    loading,
  } = useCampaigns();

  const pageConfig =
    PAGE_CONFIG.find(config => config.channel === channel) ?? PAGE_CONFIG[0];
  const PageIcon = pageConfig.icon;
  const ChannelCampaignCard =
    channel === CampaignChannel.VOICE
      ? VishingCampaignCard
      : channel === CampaignChannel.SMS
        ? SmsCampaignCard
        : EmailCampaignCard;

  const [campaigns, setCampaigns] = useState<IList<ICampaign>>({
    offset: 0,
    pageSize: 0,
    total: 0,
    items: [],
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<CampaignStatus | ''>('');
  const [viewMode, setViewMode] = useState<'grid' | 'table'>(() => {
    const saved = localStorage.getItem(getViewModeKey(channel));
    return saved === 'table' ? 'table' : 'grid';
  });
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [deleteConfirm, setDeleteConfirm] = useState<ICampaign | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [actionConfirm, setActionConfirm] =
    useState<ICampaignActionConfirm | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [queryParams, setQueryParams] = useState<IGetListParams>(() => ({
    offset: 0,
    pageSize: 12,
    search: '',
    sortBy: 'createdAt',
    sortDirection: 'desc',
    status: '',
    channel,
  }));

  useEffect(() => {
    localStorage.setItem(getViewModeKey(channel), viewMode);
  }, [viewMode, channel]);

  useEffect(() => {
    setQueryParams(prev => ({
      ...prev,
      offset: 0,
      pageSize: 12,
      searchParam: searchTerm,
      sortBy,
      sortDirection: sortOrder,
      status: statusFilter || undefined,
      channel,
    }));
  }, [searchTerm, statusFilter, channel, sortBy, sortOrder]);

  const loadCampaigns = useCallback(async () => {
    const result = await fetchCampaigns(
      queryParams as IGetListParams & {
        status?: CampaignStatus;
        channel?: CampaignChannel;
      },
    );

    setCampaigns({
      offset: result.offset,
      pageSize: result.pageSize,
      total: result.total,
      items: result.items,
    });
  }, [fetchCampaigns, queryParams]);

  useEffect(() => {
    loadCampaigns();
  }, [loadCampaigns]);

  const handleSearch = (e: FormEvent) => {
    e.preventDefault();
    loadCampaigns();
  };

  const handleSort = (field: string) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('asc');
    }
  };

  const handleView = (campaign: ICampaign) => {
    navigate(
      `${
        campaign.channel === CampaignChannel.VOICE
          ? routes.vishingCampaignDetails.path
          : campaign.channel === CampaignChannel.SMS
            ? routes.smishingCampaignDetails.path
            : routes.phishingCampaignDetails.path
      }`.replace(':id', campaign.campaignId),
    );
  };

  const handleEdit = (campaign: ICampaign) => {
    const editRoute =
      campaign.channel === CampaignChannel.VOICE
        ? routes.vishingSimulationEdit
        : campaign.channel === CampaignChannel.SMS
          ? routes.smishingSimulationEdit
          : routes.phishingCampaignEdit;
    navigate(`${editRoute.path.replace(':id', campaign.campaignId)}`);
  };

  const handleDelete = async (campaign: ICampaign) => {
    setDeleting(true);
    const success = await deleteCampaign(campaign.campaignId);
    if (success) {
      toast.success('Campaign deleted successfully');
      loadCampaigns();
      setDeleting(false);
    } else {
      toast.error('Failed to delete campaign');
      setDeleting(false);
    }
    setDeleteConfirm(null);
  };

  const handleLaunch = async (campaign: ICampaign) => {
    setActionConfirm({ type: ACTION_BUTTON_LABELS_ENUM.LAUNCH, campaign });
  };

  const handlePause = async (campaign: ICampaign) => {
    setActionConfirm({ type: ACTION_BUTTON_LABELS_ENUM.PAUSE, campaign });
  };

  const handleResume = async (campaign: ICampaign) => {
    setActionConfirm({ type: ACTION_BUTTON_LABELS_ENUM.RESUME, campaign });
  };

  const handleCancel = async (campaign: ICampaign) => {
    setActionConfirm({ type: ACTION_BUTTON_LABELS_ENUM.CANCEL, campaign });
  };

  const getActionDialogContent = (
    type: CampaignActionType,
    campaignName: string,
  ) => {
    const actionText = ACTION_TEXT[type];

    return {
      message: `Are you sure you want to ${actionText.present} "${campaignName}" campaign?`,
      buttonText: actionText.label,
      loadingText: `${actionText.progressive}...`,
      successMessage: `Campaign ${actionText.past} successfully`,
      errorMessage: `Failed to ${actionText.present} campaign`,
    };
  };

  const handleConfirmAction = async () => {
    if (!actionConfirm) {
      return;
    }

    const { campaign, type } = actionConfirm;
    const content = getActionDialogContent(type, campaign.campaignName);

    setActionLoading(true);

    let result;
    if (type === ACTION_BUTTON_LABELS_ENUM.LAUNCH) {
      result = await launchCampaign(campaign.campaignId);
    } else if (type === ACTION_BUTTON_LABELS_ENUM.PAUSE) {
      result = await pauseCampaign(campaign.campaignId);
    } else if (type === ACTION_BUTTON_LABELS_ENUM.RESUME) {
      result = await resumeCampaign(campaign.campaignId);
    } else {
      result = await cancelCampaign(campaign.campaignId);
    }

    if (result && isSuccessResponse(result.statusCode)) {
      toast.success(content.successMessage);
      await loadCampaigns();
    } else {
      toast.error(result?.message || content.errorMessage);
    }

    setActionLoading(false);
    setActionConfirm(null);
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const currentPageIndex = (queryParams.offset || 0) + 1;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-foreground">
            {pageConfig.label}
          </h2>
          <p className="text-muted-foreground">{pageConfig.description}</p>
        </div>
        <Button onClick={() => navigate(pageConfig.createPath)}>
          <PlusIcon className="size-5" />
          {pageConfig.createLabel}
        </Button>
      </div>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-lg">
                <PageIcon className="size-5" />
                {pageConfig.label} ({campaigns?.total})
              </CardTitle>
              <CardDescription>{pageConfig.cardDescription}</CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="space-y-6">
            <div className="mb-6 space-y-4">
              <div className="flex flex-wrap items-center gap-4">
                <form onSubmit={handleSearch} className="min-w-64 flex-1">
                  <div className="relative flex-1">
                    <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
                    <Input
                      placeholder="Search campaigns..."
                      value={searchTerm}
                      onChange={e => setSearchTerm(e.target.value)}
                      className="pl-9"
                    />
                  </div>
                </form>

                <Select
                  value={statusFilter}
                  onValueChange={value => {
                    setStatusFilter(
                      value === 'all' ? '' : (value as CampaignStatus),
                    );
                  }}
                >
                  <SelectTrigger className="w-40">
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    {STATUS_FILTERS.map(filter => (
                      <SelectItem key={filter.value} value={filter.value}>
                        {filter.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <ViewToggle value={viewMode} onChange={setViewMode} />
              </div>
            </div>

            {viewMode === 'grid' ? (
              <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                {loading ? (
                  <CampaignCardSkeleton />
                ) : (campaigns?.items || [])?.length === 0 ? (
                  <div className="col-span-3 flex flex-col items-center justify-center p-6">
                    <PageIcon className="size-10 text-muted-foreground" />
                    <h3 className="mt-4 text-lg font-medium text-foreground">
                      No campaigns found
                    </h3>
                    <p className="mt-2 text-muted-foreground">
                      Get started by creating your first campaign.
                    </p>
                  </div>
                ) : (
                  campaigns?.items?.map(campaign => (
                    <ChannelCampaignCard
                      key={campaign.campaignId}
                      campaign={campaign}
                      onView={handleView}
                      onEdit={handleEdit}
                      onDelete={() => setDeleteConfirm(campaign)}
                      onLaunch={handleLaunch}
                      onPause={handlePause}
                      onResume={handleResume}
                      onCancel={handleCancel}
                    />
                  ))
                )}
              </div>
            ) : (
              <CampaignTable
                campaigns={campaigns?.items || []}
                loading={loading}
                channel={channel}
                onView={handleView}
                onEdit={handleEdit}
                onDelete={c => setDeleteConfirm(c)}
                onLaunch={handleLaunch}
                sortBy={sortBy}
                sortOrder={sortOrder}
                onSort={handleSort}
                onPause={handlePause}
                onResume={handleResume}
                onCancel={handleCancel}
              />
            )}

            {campaigns?.total > (queryParams.pageSize || 0) && (
              <div className="mt-6 flex justify-end">
                <Pagination
                  total={campaigns?.total}
                  perPage={queryParams.pageSize || 0}
                  currentPage={currentPageIndex}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </div>
        </CardContent>
      </Card>
      <ConfirmDialog
        isOpen={!!deleteConfirm}
        onClose={() => setDeleteConfirm(null)}
        onConfirm={() => deleteConfirm && handleDelete(deleteConfirm)}
        message={`Are you sure you want to delete "${deleteConfirm?.campaignName || ''}"? This action cannot be undone.`}
        loading={deleting}
        loadingText="Deleting..."
        buttonText="Delete"
      />

      <ConfirmDialog
        isOpen={!!actionConfirm}
        onClose={() => {
          if (!actionLoading) setActionConfirm(null);
        }}
        onConfirm={handleConfirmAction}
        message={
          actionConfirm
            ? getActionDialogContent(
                actionConfirm.type,
                actionConfirm.campaign.campaignName,
              ).message
            : 'Are you sure?'
        }
        loading={actionLoading}
        loadingText={
          actionConfirm
            ? getActionDialogContent(
                actionConfirm.type,
                actionConfirm.campaign.campaignName,
              ).loadingText
            : 'Updating...'
        }
        buttonText={
          actionConfirm
            ? getActionDialogContent(
                actionConfirm.type,
                actionConfirm.campaign.campaignName,
              ).buttonText
            : 'Confirm'
        }
      />
    </div>
  );
};

export default CampaignListContent;
