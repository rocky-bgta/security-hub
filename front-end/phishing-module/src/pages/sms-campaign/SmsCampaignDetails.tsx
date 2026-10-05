import { Button } from 'common/Button';
import { Card } from 'common/Card';
import Pagination from 'common/Pagination';
import Loader from 'common/loader/Loader';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import ConfirmDialog from 'components/ConfirmDialog';
import IconBackButton from 'components/IconBackButton';
import { TableSkeleton } from 'components/LoadingSkeleton';
import { CampaignStatusBadge } from 'features/campaign/CampaignStatusBadge';
import { useCampaigns } from 'hooks/UseCampaigns';
import { RefreshCw } from 'lucide-react';
import {
  ACTION_BUTTON_LABELS_ENUM,
  ACTION_TEXT,
  CampaignChannel,
  CampaignType,
  ICampaign,
  ICampaignRecipient,
  getCampaignTypeLabel,
  getRecipientStatusColor,
  getRecipientStatusLabel,
  type CampaignActionType,
} from 'models/Campaign';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';
import { formatDate, humanizeText, isSuccessResponse } from 'utils/Helper';
import { getCampaignListUrl } from 'utils/ListNavigation';

const RECIPIENT_PAGE_SIZE = 10;

const isSmsCampaign = (campaign: ICampaign) =>
  campaign.channel === CampaignChannel.SMS ||
  campaign.campaignType === CampaignType.SMISHING_SIMULATION ||
  campaign.campaignType === CampaignType.SMISHING_WITH_TRAINING;

const isVishingCampaign = (campaign: ICampaign) =>
  campaign.channel === CampaignChannel.VOICE ||
  campaign.campaignType === CampaignType.VISHING_SIMULATION ||
  campaign.campaignType === CampaignType.VISHING_WITH_TRAINING;

const SmsCampaignDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const listUrl = getCampaignListUrl(CampaignChannel.SMS);

  const {
    getCampaignById,
    fetchRecipients,
    refreshCampaignStats,
    pauseCampaign,
    resumeCampaign,
    cancelCampaign,
    userLoading,
    saving,
  } = useCampaigns();

  const [campaign, setCampaign] = useState<ICampaign | null>(null);
  const [recipients, setRecipients] = useState<ICampaignRecipient[]>([]);
  const [recipientPage, setRecipientPage] = useState(1);
  const [totalRecipients, setTotalRecipients] = useState(0);
  const [activeTab, setActiveTab] = useState<'overview' | 'recipients'>(
    'overview',
  );
  const [actionType, setActionType] = useState<CampaignActionType | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const loadCampaign = useCallback(async () => {
    if (!id) return;
    const data = await getCampaignById(id);
    if (!data) {
      toast.error('Campaign not found');
      navigate(listUrl);
      return;
    }

    if (isVishingCampaign(data)) {
      navigate(
        routes.vishingCampaignDetails.path.replace(':id', data.campaignId),
        { replace: true },
      );
      return;
    }

    if (!isSmsCampaign(data)) {
      navigate(
        routes.phishingCampaignDetails.path.replace(':id', data.campaignId),
        { replace: true },
      );
      return;
    }

    setCampaign(data);
  }, [id, getCampaignById, navigate, listUrl]);

  const loadRecipients = useCallback(async () => {
    if (!id) return;
    const result = await fetchRecipients(id, {
      offset: recipientPage - 1,
      pageSize: RECIPIENT_PAGE_SIZE,
      sortBy: 'email',
      sortDirection: 'asc',
    });
    setRecipients(result.items || []);
    setTotalRecipients(result.total || 0);
  }, [id, recipientPage, fetchRecipients]);

  useEffect(() => {
    void loadCampaign();
  }, [loadCampaign]);

  useEffect(() => {
    if (activeTab === 'recipients') {
      void loadRecipients();
    }
  }, [activeTab, recipientPage, loadRecipients]);

  const applyUpdatedCampaign = (updated?: ICampaign | null) => {
    if (updated) {
      setCampaign(updated);
      return;
    }
    void loadCampaign();
  };

  const handleConfirmAction = async () => {
    if (!campaign || !actionType) return;

    const action = ACTION_TEXT[actionType];
    let result = null;

    if (actionType === ACTION_BUTTON_LABELS_ENUM.PAUSE) {
      result = await pauseCampaign(campaign.campaignId);
    } else if (actionType === ACTION_BUTTON_LABELS_ENUM.RESUME) {
      result = await resumeCampaign(campaign.campaignId);
    } else if (actionType === ACTION_BUTTON_LABELS_ENUM.CANCEL) {
      result = await cancelCampaign(campaign.campaignId);
    }

    if (result && isSuccessResponse(result.statusCode)) {
      toast.success(`Campaign ${action.past} successfully`);
      applyUpdatedCampaign(result.data);
    } else {
      toast.error(result?.message || `Failed to ${action.present} campaign`);
    }

    setActionType(null);
  };

  const handleRefresh = async () => {
    if (!campaign || refreshing) return;
    setRefreshing(true);
    try {
      await loadRecipients();
      const stats = await refreshCampaignStats(campaign.campaignId);
      if (stats) {
        setCampaign(prev => (prev ? { ...prev, stats } : prev));
      } else {
        await loadCampaign();
      }
      toast.success('Statistics refreshed');
    } finally {
      setRefreshing(false);
    }
  };

  if (!campaign) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader mode="screen" />
      </div>
    );
  }

  const stats = campaign.stats;
  const actionCopy = actionType ? ACTION_TEXT[actionType] : null;
  const tabClass = (tab: 'overview' | 'recipients') =>
    `border-b-2 px-1 py-4 text-sm font-medium ${
      activeTab === tab
        ? 'border-primary text-primary'
        : 'border-transparent text-muted-foreground hover:text-foreground'
    }`;

  return (
    <div className="space-y-6">
      <IconBackButton
        onClick={() => navigate(listUrl)}
        label="Back to Campaigns"
      />

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-foreground">
              {campaign.campaignName}
            </h1>
            <CampaignStatusBadge status={campaign.status} size="lg" />
          </div>
          <p className="mt-1 text-muted-foreground">
            {getCampaignTypeLabel(campaign.campaignType)}
            {campaign.createdAt
              ? ` • Created ${formatDate(campaign.createdAt)}`
              : ''}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="outline"
            onClick={() => void handleRefresh()}
            disabled={refreshing || saving}
          >
            <RefreshCw
              className={`mr-2 size-4 ${refreshing ? 'animate-spin' : ''}`}
            />
            {refreshing ? 'Refreshing' : 'Refresh'}
          </Button>
          {campaign.canEdit && (
            <Button
              variant="outline"
              onClick={() =>
                navigate(
                  routes.smishingSimulationEdit.path.replace(
                    ':id',
                    campaign.campaignId,
                  ),
                )
              }
            >
              Edit
            </Button>
          )}
          {campaign.canPause && (
            <Button
              variant="outline"
              onClick={() => setActionType(ACTION_BUTTON_LABELS_ENUM.PAUSE)}
              disabled={saving}
            >
              Pause
            </Button>
          )}
          {campaign.canResume && (
            <Button
              variant="outline"
              onClick={() => setActionType(ACTION_BUTTON_LABELS_ENUM.RESUME)}
              disabled={saving}
            >
              Resume
            </Button>
          )}
          {campaign.canCancel && (
            <Button
              variant="outline"
              onClick={() => setActionType(ACTION_BUTTON_LABELS_ENUM.CANCEL)}
              disabled={saving}
            >
              Cancel
            </Button>
          )}
        </div>
      </div>

      {stats && (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-5">
          <Card className="p-6">
            <div className="text-2xl font-bold text-foreground">
              {stats.totalRecipients}
            </div>
            <div className="text-sm text-muted-foreground">Recipients</div>
          </Card>
          <Card className="p-6">
            <div className="text-2xl font-bold text-primary">
              {stats.smsSent}
            </div>
            <div className="text-sm text-muted-foreground">SMS sent</div>
          </Card>
          <Card className="p-6">
            <div className="text-2xl font-bold text-cyan-600">
              {stats.smsDelivered}
            </div>
            <div className="text-sm text-muted-foreground">
              Delivered ({stats.deliveryRate?.toFixed(1) || 0}%)
            </div>
          </Card>
          <Card className="p-6">
            <div className="text-2xl font-bold text-orange-600">
              {stats.linksClicked}
            </div>
            <div className="text-sm text-muted-foreground">
              Clicked ({stats.clickRate?.toFixed(1) || 0}%)
            </div>
          </Card>
          <Card className="p-6">
            <div className="text-2xl font-bold text-vibrant-red">
              {stats.dataSubmitted}
            </div>
            <div className="text-sm text-muted-foreground">
              Compromised ({stats.submissionRate?.toFixed(1) || 0}%)
            </div>
          </Card>
        </div>
      )}

      <Card>
        <nav className="flex space-x-8 px-6">
          <button
            type="button"
            onClick={() => setActiveTab('overview')}
            className={tabClass('overview')}
          >
            Overview
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('recipients')}
            className={tabClass('recipients')}
          >
            Recipients ({stats?.totalRecipients || 0})
          </button>
        </nav>

        <div className="p-6">
          {activeTab === 'overview' && (
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
              <div>
                <h3 className="mb-4 font-medium text-foreground">
                  Campaign Configuration
                </h3>
                <dl className="space-y-3">
                  <div>
                    <dt className="text-sm text-muted-foreground">
                      SMS Template
                    </dt>
                    <dd className="text-sm font-medium">
                      {campaign.emailTemplateName || '-'}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-sm text-muted-foreground">
                      Landing Page
                    </dt>
                    <dd className="text-sm font-medium">
                      {campaign.landingPageName || '-'}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-sm text-muted-foreground">
                      SMS Server
                    </dt>
                    <dd className="text-sm font-medium">
                      {campaign.smsServerConfigurationName || '-'}
                    </dd>
                  </div>
                  <div>
                    <dt className="text-sm text-muted-foreground">Tags</dt>
                    <dd className="text-sm">
                      {campaign.campaignTags?.length > 0 ? (
                        <div className="mt-1 flex flex-wrap gap-1">
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
                        <span className="text-muted-foreground">No tags</span>
                      )}
                    </dd>
                  </div>
                </dl>
              </div>
              <div>
                <h3 className="mb-4 font-medium text-foreground">
                  Schedule & Timing
                </h3>
                <dl className="space-y-3">
                  <div>
                    <dt className="text-sm text-muted-foreground">
                      Schedule Type
                    </dt>
                    <dd className="text-sm font-medium">
                      {humanizeText(campaign.schedule?.type) || '-'}
                    </dd>
                  </div>
                  {campaign.schedule?.startDateTime && (
                    <div>
                      <dt className="text-sm text-muted-foreground">
                        Start Time
                      </dt>
                      <dd className="text-sm font-medium">
                        {new Date(
                          campaign.schedule.startDateTime,
                        ).toLocaleString()}
                      </dd>
                    </div>
                  )}
                  {campaign.launchedAt && (
                    <div>
                      <dt className="text-sm text-muted-foreground">
                        Launched At
                      </dt>
                      <dd className="text-sm font-medium">
                        {formatDate(campaign.launchedAt)}
                      </dd>
                    </div>
                  )}
                  {campaign.completedAt && (
                    <div>
                      <dt className="text-sm text-muted-foreground">
                        Completed At
                      </dt>
                      <dd className="text-sm font-medium">
                        {formatDate(campaign.completedAt)}
                      </dd>
                    </div>
                  )}
                  {stats?.smsFailed != null && (
                    <div>
                      <dt className="text-sm text-muted-foreground">
                        Failed SMS
                      </dt>
                      <dd className="text-sm font-medium">{stats.smsFailed}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </div>
          )}

          {activeTab === 'recipients' && (
            <div>
              <div className="mb-4 flex items-center justify-between">
                <h3 className="font-medium text-foreground">Recipient List</h3>
                <Button
                  variant="outline"
                  onClick={() => void handleRefresh()}
                  disabled={refreshing || saving}
                >
                  Refresh Stats
                </Button>
              </div>
              {userLoading ? (
                <TableSkeleton count={10} />
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Sr. No.</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Phone Number</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Department</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Activity</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {recipients.length === 0 ? (
                      <TableRow>
                        <TableCell
                          colSpan={7}
                          className="text-center text-muted-foreground"
                        >
                          No recipients found
                        </TableCell>
                      </TableRow>
                    ) : (
                      recipients.map((recipient, index) => (
                        <TableRow key={recipient.recipientId}>
                          <TableCell>
                            {(recipientPage - 1) * RECIPIENT_PAGE_SIZE +
                              index +
                              1}
                          </TableCell>
                          <TableCell>{recipient.email || '-'}</TableCell>
                          <TableCell>{recipient.phoneNumber || '—'}</TableCell>
                          <TableCell>{recipient.fullName || '-'}</TableCell>
                          <TableCell>{recipient.department || '-'}</TableCell>
                          <TableCell>
                            <span
                              className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${getRecipientStatusColor(recipient.status)}`}
                            >
                              {getRecipientStatusLabel(recipient.status)}
                            </span>
                          </TableCell>
                          <TableCell>
                            {recipient.clickCount > 0 && (
                              <span>Clicks: {recipient.clickCount}</span>
                            )}
                            {recipient.hasSubmittedData && (
                              <span className="ml-2 text-vibrant-red">
                                Compromised
                              </span>
                            )}
                            {recipient.clickCount === 0 &&
                              !recipient.hasSubmittedData &&
                              '-'}
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              )}
              {totalRecipients > 0 && (
                <div className="mt-4 flex items-center justify-between border-t border-card-border pt-4">
                  <span className="text-sm text-muted-foreground">
                    Showing {(recipientPage - 1) * RECIPIENT_PAGE_SIZE + 1} to{' '}
                    {Math.min(
                      recipientPage * RECIPIENT_PAGE_SIZE,
                      totalRecipients,
                    )}{' '}
                    of {totalRecipients}
                  </span>
                  <Pagination
                    total={totalRecipients}
                    perPage={RECIPIENT_PAGE_SIZE}
                    currentPage={recipientPage}
                    onPageChange={setRecipientPage}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      </Card>

      <ConfirmDialog
        isOpen={!!actionType}
        onClose={() => {
          if (!saving) setActionType(null);
        }}
        onConfirm={handleConfirmAction}
        message={
          actionCopy
            ? `Are you sure you want to ${actionCopy.present.toLowerCase()} "${campaign.campaignName}"?`
            : 'Are you sure?'
        }
        loading={saving}
        loadingText={
          actionCopy ? `${actionCopy.progressive}...` : 'Updating...'
        }
        buttonText={actionCopy?.label ?? 'Confirm'}
      />
    </div>
  );
};

export default SmsCampaignDetails;
