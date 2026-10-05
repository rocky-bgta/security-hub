import { Button } from 'common/Button';
import { Card, CardContent } from 'common/Card';
import Loader from 'common/loader/Loader';
import ConfirmDialog from 'components/ConfirmDialog';
import IconBackButton from 'components/IconBackButton';
import { CampaignStatusBadge } from 'features/campaign/CampaignStatusBadge';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';
import { RefreshCw } from 'lucide-react';
import {
  ACTION_BUTTON_LABELS_ENUM,
  ACTION_TEXT,
  CampaignChannel,
  CampaignStatus,
  CampaignType,
  getCampaignTypeLabel,
  type CampaignActionType,
} from 'models/Campaign';
import type { IVishingCampaign, VishingCampaignStatus } from 'models/Vishing';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { routes } from 'routes/Routes';
import { formatDate, isSuccessResponse } from 'utils/Helper';
import { getCampaignListUrl } from 'utils/ListNavigation';
import VishingCampaignAnalytics from './VishingCampaignAnalytics';

const EDITABLE_STATUSES: VishingCampaignStatus[] = ['DRAFT', 'SCHEDULED'];
const ANALYTICS_HIDDEN_STATUSES: VishingCampaignStatus[] = ['DRAFT'];

const VishingCampaignDetails = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const listUrl = getCampaignListUrl(CampaignChannel.VOICE);

  const {
    getCampaignById,
    pauseCampaign,
    resumeCampaign,
    cancelCampaign,
    saving,
  } = useVishingCampaigns();

  const [campaign, setCampaign] = useState<IVishingCampaign | null>(null);
  const [actionType, setActionType] = useState<CampaignActionType | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const loadCampaign = useCallback(async () => {
    if (!id) return;
    const data = await getCampaignById(id);
    if (data) {
      setCampaign(data);
      return;
    }
    toast.error('Campaign not found');
    navigate(listUrl);
  }, [id, getCampaignById, navigate, listUrl]);

  useEffect(() => {
    void loadCampaign();
  }, [loadCampaign]);

  const canEdit =
    campaign?.canEdit ??
    (campaign ? EDITABLE_STATUSES.includes(campaign.status) : false);
  const canPause = campaign?.canPause ?? campaign?.status === 'RUNNING';
  const canResume = campaign?.canResume ?? campaign?.status === 'PAUSED';
  const canCancel =
    campaign?.canCancel ??
    (campaign
      ? campaign.status === 'RUNNING' || campaign.status === 'PAUSED'
      : false);

  const applyUpdatedCampaign = (updated?: IVishingCampaign | null) => {
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
    if (!id || refreshing) return;
    setRefreshing(true);
    try {
      const data = await getCampaignById(id);
      if (data) setCampaign(data);
      setRefreshKey(key => key + 1);
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

  const showAnalytics = !ANALYTICS_HIDDEN_STATUSES.includes(campaign.status);
  const actionCopy = actionType ? ACTION_TEXT[actionType] : null;

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
            <CampaignStatusBadge
              status={campaign.status as CampaignStatus}
              size="lg"
            />
          </div>
          <p className="mt-1 text-muted-foreground">
            {getCampaignTypeLabel(campaign.campaignType as CampaignType)}
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
          {canEdit && (
            <Button
              variant="outline"
              onClick={() =>
                navigate(
                  routes.vishingSimulationEdit.path.replace(
                    ':id',
                    campaign.campaignId,
                  ),
                )
              }
            >
              Edit
            </Button>
          )}
          {canPause && (
            <Button
              variant="outline"
              onClick={() => setActionType(ACTION_BUTTON_LABELS_ENUM.PAUSE)}
              disabled={saving}
            >
              Pause
            </Button>
          )}
          {canResume && (
            <Button
              variant="outline"
              onClick={() => setActionType(ACTION_BUTTON_LABELS_ENUM.RESUME)}
              disabled={saving}
            >
              Resume
            </Button>
          )}
          {canCancel && (
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

      {showAnalytics ? (
        <VishingCampaignAnalytics
          campaignId={campaign.campaignId}
          scenario={campaign.voiceScenario}
          refreshKey={refreshKey}
        />
      ) : (
        <Card>
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            Launch this campaign to view live call analytics, data capture, and
            remediation.
          </CardContent>
        </Card>
      )}

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

export default VishingCampaignDetails;
