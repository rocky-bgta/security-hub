import { useEffect, useState } from 'react';
import { Activity, Play, Radio } from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Badge } from 'components/common/Badge';
import { Progress } from 'components/common/Progress';
import { Separator } from 'components/common/Separator';
import { toast } from 'react-toastify';
import { AudienceType, ICampaign } from 'models/Campaign';
import type { IVishingAudienceRequest } from 'models/Vishing';
import { TCampaignAudienceForm } from 'schemas/CampaignSchema';
import { isSuccessResponse } from 'utils/Helper';
import { Step6Audience } from 'features/campaign/CampaignWizard/Step6Audience';
import { useVishingWizard } from '../context/VishingWizardContext';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';

const VISHING_RISK_GROUPS = 'RISK_GROUPS';

const toAudienceType = (audienceType?: string): AudienceType => {
  if (audienceType === VISHING_RISK_GROUPS) return AudienceType.GROUPS;
  if (
    audienceType === AudienceType.ALL_USERS ||
    audienceType === AudienceType.DEPARTMENTS ||
    audienceType === AudienceType.GROUPS ||
    audienceType === AudienceType.INDIVIDUAL
  ) {
    return audienceType;
  }
  return AudienceType.ALL_USERS;
};

const hasStoredVishingAudience = (
  audience?: IVishingAudienceRequest,
): boolean => {
  if (!audience?.audienceType) return false;
  if (audience.audienceType === AudienceType.ALL_USERS) return true;
  if (audience.audienceType === AudienceType.INDIVIDUAL) {
    return Boolean(audience.userIds?.length);
  }
  if (audience.audienceType === AudienceType.DEPARTMENTS) {
    return Boolean(audience.departmentIds?.length);
  }
  if (
    audience.audienceType === VISHING_RISK_GROUPS ||
    audience.audienceType === AudienceType.GROUPS
  ) {
    return Boolean(audience.groupIds?.length || audience.riskGroups?.length);
  }
  return false;
};

const toStep6Form = (
  audience: IVishingAudienceRequest,
): TCampaignAudienceForm => ({
  audienceType: toAudienceType(audience.audienceType),
  departmentIds: audience.departmentIds ?? [],
  groupIds: audience.groupIds ?? audience.riskGroups ?? [],
  userIds: audience.userIds ?? [],
});

const toVishingAudiencePayload = (
  data: TCampaignAudienceForm,
): IVishingAudienceRequest => {
  const isGroups = data.audienceType === AudienceType.GROUPS;
  return {
    audienceType: isGroups ? VISHING_RISK_GROUPS : data.audienceType,
    departmentIds: data.departmentIds,
    groupIds: data.groupIds,
    riskGroups: isGroups ? data.groupIds : undefined,
    userIds: data.userIds,
  };
};

interface AudienceTabProps {
  productPackageId?: string;
  onBack: () => void;
  onContinue: () => void;
  isLoading?: boolean;
}

export function AudienceTab({
  productPackageId,
  onBack,
  onContinue,
  isLoading,
}: AudienceTabProps) {
  const { campaignId, audience, setAudience, hydrateFromCampaign } =
    useVishingWizard();
  const { updateAudience, getCampaignById, saving } = useVishingCampaigns();

  const handleSubmit = async (data: TCampaignAudienceForm) => {
    if (!campaignId) {
      toast.error('Campaign not found');
      return;
    }

    const payload = toVishingAudiencePayload(data);
    setAudience(payload);
    const response = await updateAudience(campaignId, payload);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save audience');
      return;
    }
    onContinue();
  };

  const handleAudienceAllocated = async (): Promise<ICampaign | null> => {
    if (!campaignId) return null;
    const data = await getCampaignById(campaignId, { silent: true });
    if (!data) return null;
    hydrateFromCampaign(data);
    const form = toStep6Form(data.audience ?? {});
    return {
      audience: {
        type: form.audienceType,
        departmentIds: form.departmentIds,
        groupIds: form.groupIds,
        userIds: form.userIds,
        recipientCount: 0,
      },
    } as ICampaign;
  };

  return (
    <Step6Audience
      campaignId={campaignId ?? undefined}
      productPackageId={productPackageId}
      initialData={
        hasStoredVishingAudience(audience) ? toStep6Form(audience) : undefined
      }
      onSubmit={handleSubmit}
      onBack={onBack}
      onAudienceAllocated={handleAudienceAllocated}
      isLoading={isLoading || saving}
    />
  );
}

export function LiveSimulationTab({
  onLaunched,
}: { onLaunched?: () => void } = {}) {
  const { campaignId } = useVishingWizard();
  const { launchCampaign, getLiveMetrics } = useVishingCampaigns();
  const [running, setRunning] = useState(false);
  const [calls, setCalls] = useState({ active: 0, answered: 0, completed: 0 });

  useEffect(() => {
    if (!running || !campaignId) return;

    const poll = async () => {
      const metrics = await getLiveMetrics(campaignId);
      if (!metrics) return;
      setCalls({
        active: metrics.activeCalls,
        answered: metrics.answeredCalls,
        completed: metrics.completedCalls,
      });
    };

    void poll();
    const timer = window.setInterval(() => void poll(), 5000);
    return () => window.clearInterval(timer);
  }, [running, campaignId, getLiveMetrics]);

  const launch = async () => {
    if (!campaignId) {
      toast.error('Campaign not found. Complete the wizard first.');
      return;
    }

    const response = await launchCampaign(campaignId);
    if (!response?.data) {
      toast.error(response?.message ?? 'Failed to launch campaign.');
      return;
    }

    setRunning(true);
    toast.success('Live simulation started — dialling targets');
    onLaunched?.();
  };

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Radio className="size-4 text-primary" /> Launch Live Simulation
          </CardTitle>
          <CardDescription>
            Begin AI-driven outbound calls using the configured voice, script
            and telephony provider.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-3 gap-3">
            <div className="rounded-lg border border-card-border/60 p-4">
              <div className="text-xs text-muted-foreground">Active calls</div>
              <div className="mt-1 text-2xl font-semibold">{calls.active}</div>
            </div>
            <div className="rounded-lg border border-card-border/60 p-4">
              <div className="text-xs text-muted-foreground">Answered</div>
              <div className="mt-1 text-2xl font-semibold text-emerald-500">
                {calls.answered}
              </div>
            </div>
            <div className="rounded-lg border border-card-border/60 p-4">
              <div className="text-xs text-muted-foreground">Completed</div>
              <div className="mt-1 text-2xl font-semibold">
                {calls.completed}
              </div>
            </div>
          </div>
          <div className="flex gap-2">
            <Button
              onClick={launch}
              disabled={running}
              className="text-primary-foreground"
            >
              <Play className="mr-1 size-4" />{' '}
              {running ? 'Running…' : 'Start simulation'}
            </Button>
          </div>
          {running && <Progress value={(calls.completed / 12) * 100} />}
        </CardContent>
      </Card>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="size-4 text-primary" /> Real-time Pipeline
          </CardTitle>
          <CardDescription>STT → LLM → TTS latency</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3 text-sm">
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Speech-to-Text</span>
            <Badge variant="outline">~180 ms</Badge>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">LLM response</span>
            <Badge variant="outline">~640 ms</Badge>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground">Text-to-Speech</span>
            <Badge variant="outline">~220 ms</Badge>
          </div>
          <Separator />
          <div className="flex items-center justify-between font-medium">
            <span>End-to-end</span>
            <span className="text-emerald-500">~1.04 s</span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
