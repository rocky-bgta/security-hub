import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Activity, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from 'components/common/Button';
import {
  getVishingCampaignId,
  toVishingCampaignCreateRequest,
  mapCampaignToWizardDraft,
  parseStepKey,
  stepKeyToIndex,
} from 'schemas/VishingSchema';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';
import {
  useVishingWizard,
  VishingWizardProvider,
} from 'features/vishing/context/VishingWizardContext';
import { WizardStepper } from 'features/campaign/CampaignWizard/WizardStepper';
import { isSuccessResponse } from 'utils/Helper';
import { CampaignChannel, CampaignValidityUnit } from 'models/Campaign';
import { getCampaignListUrl } from 'utils/ListNavigation';
import { toast } from 'react-toastify';
import type { CampaignSetup, StepKey } from 'models/Vishing';
import { routes } from 'routes/Routes';
import { TrainingBundleTab } from 'features/vishing/steps/TrainingBundleTab';
import {
  ReviewTab,
  SchedulePlaceholder,
  SetupTab,
  TagsTab,
} from 'features/vishing/steps/WizardSteps';
import {
  AudienceTab,
  LiveSimulationTab,
} from 'features/vishing/steps/AudienceLiveSteps';
import { TelephonyTab } from 'features/vishing/steps/TelephonyTab';
import IdentityVoiceTab from 'features/vishing/steps/VoiceStep';
import { ScenarioScriptTab } from 'features/vishing/steps/ScenarioStep';

const BASE_STEPS: { key: StepKey; title: string }[] = [
  { key: 'setup', title: 'Setup' },
  { key: 'voice', title: 'Voice Setup' },
  { key: 'scenario', title: 'Scenario & Script' },
  { key: 'telephony', title: 'Telephony' },
  { key: 'tags', title: 'Campaign Tags' },
  { key: 'audience', title: 'Select Audience' },
  { key: 'schedule', title: 'Schedule' },
  { key: 'review', title: 'Review' },
  { key: 'live', title: 'Live Simulation' },
];

const TRAINING_STEP = {
  key: 'training' as StepKey,
  title: 'Training & Content',
};

const VishingPageContent = () => {
  const { id: routeCampaignId } = useParams<{ id: string }>();
  const isEditMode = !!routeCampaignId;
  const {
    createCampaign,
    updateSetup,
    updateVoiceSetup,
    updateScenario,
    updateTelephony,
    updateTags,
    updateAudience,
    updateSchedule,
    getCampaignById,
    saving,
  } = useVishingCampaigns();
  const {
    campaignId,
    setCampaignId,
    draftHydrated,
    hydrateFromCampaign,
    resetWizard,
    voiceSetup,
    scenarioAttach,
    telephony,
    tags,
    audience,
    schedule,
  } = useVishingWizard();
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const topRef = useRef<HTMLDivElement | null>(null);
  const draftLoadRef = useRef<string | null>(null);
  const [loadingDraft, setLoadingDraft] = useState(false);
  const [pendingStepKey, setPendingStepKey] = useState<StepKey | null>(null);
  const [step, setStep] = useState(1);
  const [completedStep, setCompletedStep] = useState(0);
  const [setup, setSetup] = useState<CampaignSetup>({
    name: '',
    type: 'VISHING_SIMULATION',
    stages: ['COMPROMISED'],
    learningMode: 'MICRO_CONTENT',
    productPackageId: '',
    expireDate: {
      validityUnit: CampaignValidityUnit.DAYS,
      validityPeriod: 1,
    },
  });

  const steps = useMemo(() => {
    const list = [...BASE_STEPS];
    if (setup.type === 'VISHING_WITH_TRAINING') {
      const audienceIdx = list.findIndex(s => s.key === 'audience');
      list.splice(audienceIdx + 1, 0, TRAINING_STEP);
    }
    return list.map((s, i) => ({ ...s, id: i + 1 }));
  }, [setup.type]);

  const stepperSteps = useMemo(
    () => steps.map(s => ({ step: s.id, name: s.title })),
    [steps],
  );

  const totalSteps = steps.length;
  const stepIdOf = (key: StepKey) => steps.find(s => s.key === key)?.id ?? 0;
  const currentKey = steps[step - 1]?.key;
  const queryCampaignId = searchParams.get('campaignId');
  const urlCampaignId = routeCampaignId || queryCampaignId;
  const urlStep = searchParams.get('step');

  const goToStep = (nextStep: number) => {
    setStep(nextStep);
    setCompletedStep(prev => Math.max(prev, nextStep - 1));
  };

  const openCampaignDetails = (id?: string | null) => {
    const targetId = id || campaignId;
    if (!targetId) {
      navigate(routes.vishingCampaigns.path);
      return;
    }
    navigate(routes.vishingCampaignDetails.path.replace(':id', targetId));
  };

  useEffect(() => {
    if (!urlCampaignId) return;
    if (draftLoadRef.current === urlCampaignId) return;

    const loadDraft = async () => {
      setLoadingDraft(true);
      try {
        const campaign = await getCampaignById(urlCampaignId);
        if (!campaign) {
          toast.error('Campaign draft not found.');
          draftLoadRef.current = null;
          resetWizard();
          navigate(
            isEditMode
              ? routes.vishingCampaigns.path
              : routes.vishingSimulationCreate.path,
            { replace: true },
          );
          return;
        }

        // Launched campaigns are no longer editable, so show their results.
        if (
          campaign.status === 'RUNNING' ||
          campaign.status === 'COMPLETED' ||
          campaign.status === 'PAUSED'
        ) {
          navigate(
            routes.vishingCampaignDetails.path.replace(':id', urlCampaignId),
            {
              replace: true,
            },
          );
          return;
        }

        hydrateFromCampaign(campaign);
        const draft = mapCampaignToWizardDraft(campaign);
        setSetup(draft.setup);
        setPendingStepKey(parseStepKey(urlStep) ?? draft.resumeStepKey);
        draftLoadRef.current = urlCampaignId;
      } finally {
        setLoadingDraft(false);
      }
    };

    void loadDraft();
  }, [
    urlCampaignId,
    urlStep,
    isEditMode,
    getCampaignById,
    hydrateFromCampaign,
    resetWizard,
    navigate,
  ]);

  useEffect(() => {
    if (!pendingStepKey) return;
    const stepId = stepKeyToIndex(steps, pendingStepKey);
    if (stepId) {
      setStep(stepId);
      setCompletedStep(prev => Math.max(prev, stepId - 1));
      setPendingStepKey(null);
    }
  }, [pendingStepKey, steps]);

  useEffect(() => {
    if (!campaignId) {
      if (urlCampaignId && draftLoadRef.current !== urlCampaignId) return;
      if (queryCampaignId || urlStep) {
        setSearchParams(
          prev => {
            const next = new URLSearchParams(prev);
            next.delete('campaignId');
            next.delete('step');
            return next;
          },
          { replace: true },
        );
      }
      return;
    }

    const next = new URLSearchParams();
    if (!isEditMode) {
      next.set('campaignId', campaignId);
    }
    if (currentKey) next.set('step', currentKey);

    if (next.toString() !== searchParams.toString()) {
      setSearchParams(next, { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isEditMode, campaignId, step, currentKey, draftHydrated, urlCampaignId]);

  useEffect(() => {
    const scrollToTop = () => {
      let current: HTMLElement | null = topRef.current;

      while (current) {
        current.scrollTop = 0;
        current = current.parentElement;
      }

      document.documentElement.scrollTop = 0;
      document.body.scrollTop = 0;
      window.scrollTo({ top: 0, behavior: 'auto' });
    };

    requestAnimationFrame(() => {
      scrollToTop();
      requestAnimationFrame(scrollToTop);
    });
  }, [step]);

  const next = () => goToStep(Math.min(totalSteps, step + 1));
  const back = () => goToStep(Math.max(1, step - 1));

  const saveSetupStep = async (): Promise<boolean> => {
    const payload = toVishingCampaignCreateRequest(setup);

    if (!campaignId) {
      const response = await createCampaign(payload);
      const createdId = getVishingCampaignId(response?.data ?? null);

      if (!createdId || !isSuccessResponse(response?.statusCode ?? 0)) {
        toast.error(response?.message ?? 'Failed to create campaign');
        return false;
      }

      setCampaignId(createdId);
      // Skip draft reload so we stay on create and advance to step 2.
      draftLoadRef.current = createdId;
      return true;
    }

    const response = await updateSetup(campaignId, payload);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save campaign setup');
      return false;
    }

    return true;
  };

  const saveVoiceSetupStep = async (): Promise<boolean> => {
    if (!campaignId) {
      toast.error('Campaign not found. Please complete setup first.');
      return false;
    }

    if (!voiceSetup.consentConfirmed) {
      toast.error('Please confirm consent before continuing.');
      return false;
    }

    if (!voiceSetup.audioFile && !voiceSetup.voiceCloneId) {
      toast.error('Please record, upload, or select a voice profile.');
      return false;
    }

    const response = await updateVoiceSetup(campaignId, voiceSetup);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save voice setup');
      return false;
    }

    return true;
  };

  const saveScenarioStep = async (): Promise<boolean> => {
    if (!campaignId) {
      toast.error('Campaign not found. Please complete setup first.');
      return false;
    }

    if (!scenarioAttach.scenarioId) {
      toast.error('Please publish a scenario before continuing.');
      return false;
    }

    const response = await updateScenario(campaignId, scenarioAttach);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(
        response?.message ??
          'Failed to attach scenario. Ensure it is published.',
      );
      return false;
    }

    return true;
  };

  const requireCampaignId = (): string | null => {
    if (!campaignId) {
      toast.error('Campaign not found. Please complete setup first.');
      return null;
    }
    return campaignId;
  };

  const saveTelephonyStep = async (): Promise<boolean> => {
    const id = requireCampaignId();
    if (!id) return false;

    if (!telephony.voiceServerConfigurationId) {
      toast.error('Please select a telephony provider.');
      return false;
    }

    const response = await updateTelephony(id, telephony);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(
        response?.message ?? 'Failed to save telephony configuration',
      );
      return false;
    }

    return true;
  };

  const saveTagsStep = async (): Promise<boolean> => {
    const id = requireCampaignId();
    if (!id) return false;

    const response = await updateTags(id, tags);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save campaign tags');
      return false;
    }

    return true;
  };

  const saveAudienceStep = async (): Promise<boolean> => {
    const id = requireCampaignId();
    if (!id) return false;

    if (
      audience.audienceType === 'INDIVIDUAL' &&
      (!audience.userIds || audience.userIds.length === 0)
    ) {
      toast.error('Please select at least one audience member.');
      return false;
    }

    const response = await updateAudience(id, audience);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save audience');
      return false;
    }

    return true;
  };

  const saveScheduleStep = async (): Promise<boolean> => {
    const id = requireCampaignId();
    if (!id) return false;

    const response = await updateSchedule(id, schedule);
    if (!isSuccessResponse(response?.statusCode ?? 0)) {
      toast.error(response?.message ?? 'Failed to save schedule');
      return false;
    }

    return true;
  };

  const handleSaveAndContinue = async () => {
    if (currentKey === 'setup') {
      const saved = await saveSetupStep();
      if (!saved) return;
    }

    if (currentKey === 'voice') {
      const saved = await saveVoiceSetupStep();
      if (!saved) return;
    }

    if (currentKey === 'scenario') {
      const saved = await saveScenarioStep();
      if (!saved) return;
    }

    if (currentKey === 'telephony') {
      const saved = await saveTelephonyStep();
      if (!saved) return;
    }

    if (currentKey === 'tags') {
      const saved = await saveTagsStep();
      if (!saved) return;
    }

    if (currentKey === 'audience') {
      const saved = await saveAudienceStep();
      if (!saved) return;
    }

    if (currentKey === 'schedule') {
      const saved = await saveScheduleStep();
      if (!saved) return;
    }

    next();
  };

  const finishWizard = () => {
    toast.success('Simulation launched successfully!');
    openCampaignDetails(campaignId);
  };

  const validityPeriodMax =
    setup.expireDate.validityUnit === CampaignValidityUnit.MONTHS ? 12 : 365;
  const isValidityPeriodValid =
    Number.isFinite(setup.expireDate.validityPeriod) &&
    setup.expireDate.validityPeriod > 0 &&
    setup.expireDate.validityPeriod <= validityPeriodMax;

  const canContinue =
    step === 1
      ? setup.name.trim().length > 0 &&
        setup.stages.length > 0 &&
        setup.productPackageId.trim().length > 0 &&
        isValidityPeriodValid
      : true;

  if (loadingDraft) {
    return (
      <div className="py-12 text-center text-sm text-white/50">
        Loading campaign draft…
      </div>
    );
  }

  return (
    <div ref={topRef}>
      <header className="mb-8 flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold tracking-tight text-white">
          {isEditMode ? 'Edit vishing simulation' : 'Create vishing simulation'}
        </h1>
        {campaignId && (
          <Button variant="outline" onClick={() => openCampaignDetails()}>
            <Activity className="mr-1 size-4" /> View Campaign
          </Button>
        )}
      </header>

      <WizardStepper
        currentStep={step}
        completedStep={completedStep}
        steps={stepperSteps}
        onStepClick={goToStep}
      />

      <div
        className="space-y-6"
        key={`${campaignId ?? 'new'}-${draftHydrated}`}
      >
        <div>
          {currentKey === 'setup' && (
            <SetupTab setup={setup} onChange={setSetup} />
          )}
          {currentKey === 'voice' && <IdentityVoiceTab />}
          {currentKey === 'scenario' && <ScenarioScriptTab />}
          {currentKey === 'telephony' && <TelephonyTab />}
          {currentKey === 'tags' && <TagsTab />}
          {currentKey === 'audience' && (
            <AudienceTab
              productPackageId={setup.productPackageId}
              onBack={back}
              onContinue={next}
              isLoading={saving}
            />
          )}
          {currentKey === 'training' && (
            <TrainingBundleTab
              productPackageId={setup.productPackageId}
              onBack={back}
              onContinue={next}
              isLoading={saving}
            />
          )}
          {currentKey === 'schedule' && <SchedulePlaceholder />}
          {currentKey === 'review' && (
            <ReviewTab
              setup={setup}
              onEdit={key => {
                const id = stepIdOf(key);
                if (id) goToStep(id);
              }}
            />
          )}
          {currentKey === 'live' && (
            <LiveSimulationTab onLaunched={finishWizard} />
          )}
        </div>

        {currentKey !== 'training' && currentKey !== 'audience' && (
          <div className="flex flex-col-reverse items-stretch justify-between gap-3 border-t border-white/10 pt-5 sm:flex-row sm:items-center">
            <Button
              variant="outline"
              onClick={
                step === 1
                  ? () => navigate(getCampaignListUrl(CampaignChannel.VOICE))
                  : back
              }
              className="border-white/15 bg-transparent text-white/80 hover:bg-white/[0.05]"
            >
              {step === 1 ? (
                'Cancel'
              ) : (
                <>
                  <ChevronLeft className="mr-1 size-4" /> Back
                </>
              )}
            </Button>
            <div className="hidden text-xs text-white/40 sm:block">
              Step {step} of {totalSteps}
            </div>
            {step < totalSteps ? (
              <Button
                onClick={handleSaveAndContinue}
                disabled={!canContinue || saving}
                className="bg-[#00FFA3] font-medium text-black shadow-[0_0_24px_rgba(0,255,163,0.35)] transition-all hover:-translate-y-0.5 hover:bg-[#00FFA3]/90 hover:shadow-[0_0_32px_rgba(0,255,163,0.55)] disabled:opacity-40 disabled:shadow-none disabled:hover:translate-y-0"
              >
                Save &amp; Continue <ChevronRight className="ml-1 size-4" />
              </Button>
            ) : null}
          </div>
        )}
      </div>
    </div>
  );
};

const VishingPage = () => (
  <VishingWizardProvider>
    <VishingPageContent />
  </VishingWizardProvider>
);

export default VishingPage;
