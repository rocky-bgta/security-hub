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
import { TableSkeleton } from 'components/LoadingSkeleton';
import { CampaignStatusBadge } from 'features/campaign/CampaignStatusBadge';
import { useCampaigns } from 'hooks/UseCampaigns';
import {
  CampaignChannel,
  CampaignType,
  ICampaign,
  ICampaignRecipient,
  getCampaignTypeLabel,
  getRecipientStatusColor,
  getRecipientStatusLabel,
} from 'models/Campaign';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';
import { getCampaignListUrl } from 'utils/ListNavigation';
import { formatDate, humanizeText } from 'utils/Helper';

const RECIPIENT_PAGE_SIZE = 10;

const CampaignDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const {
    getCampaignById,
    fetchRecipients,
    refreshCampaignStats,
    pauseCampaign,
    resumeCampaign,
    cancelCampaign,
    loading,
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

  const loadCampaign = useCallback(async () => {
    if (!id) return;
    const data = await getCampaignById(id);
    if (data) {
      const isVishing =
        data.channel === CampaignChannel.VOICE ||
        data.campaignType === CampaignType.VISHING_SIMULATION ||
        data.campaignType === CampaignType.VISHING_WITH_TRAINING;
      const isSms =
        data.channel === CampaignChannel.SMS ||
        data.campaignType === CampaignType.SMISHING_SIMULATION ||
        data.campaignType === CampaignType.SMISHING_WITH_TRAINING;
      if (isVishing) {
        navigate(
          routes.vishingCampaignDetails.path.replace(':id', data.campaignId),
          { replace: true },
        );
        return;
      }
      if (isSms) {
        navigate(
          routes.smishingCampaignDetails.path.replace(':id', data.campaignId),
          { replace: true },
        );
        return;
      }
      setCampaign(data);
    } else {
      toast.error('Campaign not found');
      navigate(getCampaignListUrl());
    }
  }, [id, getCampaignById, navigate]);

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
    setTimeout(() => {
      loadCampaign();
    }, 0);
  }, [loadCampaign]);

  useEffect(() => {
    if (activeTab === 'recipients') {
      setTimeout(() => {
        loadRecipients();
      }, 0);
    }
  }, [activeTab, recipientPage, loadRecipients]);

  const handlePause = async () => {
    if (!campaign) return;
    const result = await pauseCampaign(campaign.campaignId);
    if (result) {
      toast.success('Campaign paused');
      setCampaign(result.data);
    }
  };

  const handleResume = async () => {
    if (!campaign) return;
    const result = await resumeCampaign(campaign.campaignId);
    if (result) {
      toast.success('Campaign resumed');
      setCampaign(result.data);
    }
  };

  const handleCancel = async () => {
    if (!campaign) return;
    if (window.confirm('Are you sure you want to cancel this campaign?')) {
      const result = await cancelCampaign(campaign.campaignId);
      if (result) {
        toast.success('Campaign cancelled');
        setCampaign(result.data);
      }
    }
  };

  const handleRefreshStats = async () => {
    if (!campaign) return;
    await loadRecipients();
    const stats = await refreshCampaignStats(campaign.campaignId);
    if (stats) {
      setCampaign(prev => (prev ? { ...prev, stats } : prev));
      toast.success('Statistics refreshed');
    }
  };

  if (loading || !campaign) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader mode="screen" />
      </div>
    );
  }

  const stats = campaign.stats;
  const isVishingCampaign =
    campaign.channel === CampaignChannel.VOICE ||
    campaign.campaignType === CampaignType.VISHING_SIMULATION ||
    campaign.campaignType === CampaignType.VISHING_WITH_TRAINING;
  const editPath = isVishingCampaign
    ? routes.vishingSimulationEdit.path
    : routes.phishingCampaignEdit.path;

  return (
    <div>
      <div>
        {/* Header */}
        <div>
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center space-x-3">
                <h1 className="text-2xl font-bold text-foreground">
                  {campaign.campaignName}
                </h1>
                <CampaignStatusBadge status={campaign.status} size="lg" />
              </div>
              <p className="mt-1 text-muted-foreground">
                {getCampaignTypeLabel(campaign.campaignType)} • Created{' '}
                {new Date(campaign.createdAt).toLocaleDateString()}
              </p>
            </div>
            <div className="flex space-x-2">
              {campaign.canEdit && (
                <Button
                  variant="outline"
                  onClick={() =>
                    navigate(editPath.replace(':id', campaign.campaignId))
                  }
                >
                  Edit
                </Button>
              )}
              {campaign.canPause && (
                <Button
                  variant="outline"
                  onClick={handlePause}
                  disabled={saving}
                >
                  Pause
                </Button>
              )}
              {campaign.canResume && (
                <Button
                  variant="outline"
                  onClick={handleResume}
                  disabled={saving}
                >
                  Resume
                </Button>
              )}
              {campaign.canCancel && (
                <Button
                  variant="outline"
                  onClick={handleCancel}
                  disabled={saving}
                >
                  Cancel
                </Button>
              )}
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        {stats && (
              <div className="my-6 grid grid-cols-5 gap-4">
                <Card className="p-6">
                  <div className="text-2xl font-bold text-foreground">
                    {stats.totalRecipients}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Recipients
                  </div>
                </Card>
                <Card className="p-6">
                  <div className="text-2xl font-bold text-primary">
                    {stats.emailsOpened}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Opened ({stats.openRate?.toFixed(1) || 0}%)
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
                <Card className="p-6">
                  <div className="text-2xl font-bold text-green-600">
                    {stats.emailsReported}
                  </div>
                  <div className="text-sm text-muted-foreground">
                    Reported ({stats.reportRate?.toFixed(1) || 0}%)
                  </div>
                </Card>
              </div>
            )}

            {/* Tabs */}
            <Card>
              <div>
                <nav className="flex space-x-8 px-6">
                  <button
                    onClick={() => setActiveTab('overview')}
                    className={`border-b-2 border-primary px-1 py-4 text-sm font-medium ${
                      activeTab === 'overview'
                        ? 'border-b-2 border-primary text-primary'
                        : 'border-transparent text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    Overview
                  </button>
                  <button
                    onClick={() => setActiveTab('recipients')}
                    className={`border-b-2 px-1 py-4 text-sm font-medium ${
                      activeTab === 'recipients'
                        ? 'border-b-2 border-primary text-primary'
                        : 'border-transparent text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    Recipients ({stats?.totalRecipients || 0})
                  </button>
                </nav>
              </div>

              <div className="p-6">
                {activeTab === 'overview' && (
                  <div className="grid grid-cols-2 gap-6">
                    <div>
                      <h3 className="mb-4 font-medium text-foreground">
                        Campaign Configuration
                      </h3>
                      <dl className="space-y-3">
                        <div>
                          <dt className="text-sm text-muted-foreground">
                            Email Template
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
                            Sender Profile
                          </dt>
                          <dd className="text-sm font-medium">
                            {campaign.senderProfileName || '-'}
                          </dd>
                        </div>
                        <div>
                          <dt className="text-sm text-muted-foreground">
                            Tags
                          </dt>
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
                              <span className="text-muted-foreground">
                                No tags
                              </span>
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
                      </dl>
                    </div>
                  </div>
                )}

                {activeTab === 'recipients' && (
                  <div>
                    <div className="mb-4 flex items-center justify-between">
                      <h3 className="font-medium text-foreground">
                        Recipient List
                      </h3>
                      <Button
                        variant="outline"
                        onClick={handleRefreshStats}
                        disabled={saving}
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
                            <TableHead>Name</TableHead>
                            <TableHead>Phone Number</TableHead>
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
                                <TableCell>{recipient.email}</TableCell>
                                <TableCell>{recipient.fullName}</TableCell>
                                <TableCell>{recipient.phoneNumber || '-'}</TableCell>
                                <TableCell>
                                  {recipient.department || '-'}
                                </TableCell>
                                <TableCell>
                                  <span
                                    className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${getRecipientStatusColor(recipient.status)}`}
                                  >
                                    {getRecipientStatusLabel(recipient.status)}
                                  </span>
                                </TableCell>
                                <TableCell>
                                  {recipient.openCount > 0 && (
                                    <span className="mr-2">
                                      Opens: {recipient.openCount}
                                    </span>
                                  )}
                                  {recipient.clickCount > 0 && (
                                    <span>Clicks: {recipient.clickCount}</span>
                                  )}
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
                          Showing {(recipientPage - 1) * RECIPIENT_PAGE_SIZE + 1}{' '}
                          to{' '}
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
      </div>
    </div>
  );
};

export default CampaignDetails;
