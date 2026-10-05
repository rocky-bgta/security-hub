import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type {
  IVishingAudienceRequest,
  IVishingCampaign,
  IVishingScenarioAttachRequest,
  IVishingScheduleRequest,
  IVishingTagsRequest,
  IVishingTelephonyRequest,
  IVishingTrainingRequest,
  IVishingVoiceSetupDraft,
} from 'models/Vishing';
import { mapCampaignToWizardDraft } from 'schemas/VishingSchema';

interface VishingWizardContextValue {
  campaignId: string | null;
  setCampaignId: (id: string | null) => void;
  draftHydrated: boolean;
  hydrateFromCampaign: (campaign: IVishingCampaign) => void;
  resetWizard: () => void;
  voiceSetup: IVishingVoiceSetupDraft;
  setVoiceSetup: (value: Partial<IVishingVoiceSetupDraft>) => void;
  selectedScenarioId: string | null;
  setSelectedScenarioId: (id: string | null) => void;
  scenarioAttach: IVishingScenarioAttachRequest;
  setScenarioAttach: (value: Partial<IVishingScenarioAttachRequest>) => void;
  telephony: IVishingTelephonyRequest;
  setTelephony: (value: Partial<IVishingTelephonyRequest>) => void;
  tags: IVishingTagsRequest;
  setTags: (value: Partial<IVishingTagsRequest>) => void;
  audience: IVishingAudienceRequest;
  setAudience: (value: Partial<IVishingAudienceRequest>) => void;
  training: IVishingTrainingRequest;
  setTraining: (value: Partial<IVishingTrainingRequest>) => void;
  schedule: IVishingScheduleRequest;
  setSchedule: (value: Partial<IVishingScheduleRequest>) => void;
}

const defaultVoiceSetup: IVishingVoiceSetupDraft = {
  consentConfirmed: true,
  voiceCloneId: '',
  voiceName: '',
  consentText:
    'I consent to my voice and likeness being used to generate synthetic media for internal security awareness training only.',
  cloningEngine: 'ELEVENLABS',
  language: 'en',
  audioFile: null,
  sampleDuration: 0,
};

const defaultScenarioAttach: IVishingScenarioAttachRequest = {
  scenarioId: '',
  enableLlmResponses: true,
  escalationLimit: 'MEDIUM',
};

const defaultTelephony: IVishingTelephonyRequest = {
  voiceServerConfigurationId: '',
};

const defaultTags: IVishingTagsRequest = { tags: [] };
const defaultAudience: IVishingAudienceRequest = { audienceType: 'INDIVIDUAL' };
const defaultTraining: IVishingTrainingRequest = {};
const defaultSchedule: IVishingScheduleRequest = {
  scheduleType: 'IMMEDIATELY',
  timezone: '',
};

const VishingWizardContext = createContext<VishingWizardContextValue | null>(
  null,
);

export const VishingWizardProvider = ({
  children,
}: {
  children: ReactNode;
}) => {
  const [campaignId, setCampaignId] = useState<string | null>(null);
  const [draftHydrated, setDraftHydrated] = useState(false);
  const [voiceSetup, setVoiceSetupState] =
    useState<IVishingVoiceSetupDraft>(defaultVoiceSetup);
  const [selectedScenarioId, setSelectedScenarioId] = useState<string | null>(
    null,
  );
  const [scenarioAttach, setScenarioAttachState] =
    useState<IVishingScenarioAttachRequest>(defaultScenarioAttach);
  const [telephony, setTelephonyState] =
    useState<IVishingTelephonyRequest>(defaultTelephony);
  const [tags, setTagsState] = useState<IVishingTagsRequest>(defaultTags);
  const [audience, setAudienceState] =
    useState<IVishingAudienceRequest>(defaultAudience);
  const [training, setTrainingState] =
    useState<IVishingTrainingRequest>(defaultTraining);
  const [schedule, setScheduleState] =
    useState<IVishingScheduleRequest>(defaultSchedule);

  const setVoiceSetup = useCallback(
    (value: Partial<IVishingVoiceSetupDraft>) => {
      setVoiceSetupState(prev => ({ ...prev, ...value }));
    },
    [],
  );

  const setScenarioAttach = useCallback(
    (value: Partial<IVishingScenarioAttachRequest>) => {
      setScenarioAttachState(prev => ({ ...prev, ...value }));
    },
    [],
  );

  const setTelephony = useCallback(
    (value: Partial<IVishingTelephonyRequest>) => {
      setTelephonyState(prev => ({ ...prev, ...value }));
    },
    [],
  );

  const setTags = useCallback((value: Partial<IVishingTagsRequest>) => {
    setTagsState(prev => ({ ...prev, ...value }));
  }, []);

  const setAudience = useCallback((value: Partial<IVishingAudienceRequest>) => {
    setAudienceState(prev => ({ ...prev, ...value }));
  }, []);

  const setTraining = useCallback((value: Partial<IVishingTrainingRequest>) => {
    setTrainingState(prev => ({ ...prev, ...value }));
  }, []);

  const setSchedule = useCallback((value: Partial<IVishingScheduleRequest>) => {
    setScheduleState(prev => ({ ...prev, ...value }));
  }, []);

  const hydrateFromCampaign = useCallback((campaign: IVishingCampaign) => {
    const draft = mapCampaignToWizardDraft(campaign);
    setCampaignId(campaign.campaignId);
    setVoiceSetupState({
      ...draft.voiceSetup,
      audioFile: null,
      sampleDuration: 0,
    });
    setSelectedScenarioId(draft.selectedScenarioId);
    setScenarioAttachState(draft.scenarioAttach);
    setTelephonyState(draft.telephony);
    setTagsState(draft.tags);
    setAudienceState(draft.audience);
    setTrainingState(draft.training);
    setScheduleState(draft.schedule);
    setDraftHydrated(true);
  }, []);

  const resetWizard = useCallback(() => {
    setCampaignId(null);
    setVoiceSetupState(defaultVoiceSetup);
    setSelectedScenarioId(null);
    setScenarioAttachState(defaultScenarioAttach);
    setTelephonyState(defaultTelephony);
    setTagsState(defaultTags);
    setAudienceState(defaultAudience);
    setTrainingState(defaultTraining);
    setScheduleState(defaultSchedule);
    setDraftHydrated(false);
  }, []);

  const value = useMemo(
    () => ({
      campaignId,
      setCampaignId,
      draftHydrated,
      hydrateFromCampaign,
      resetWizard,
      voiceSetup,
      setVoiceSetup,
      selectedScenarioId,
      setSelectedScenarioId,
      scenarioAttach,
      setScenarioAttach,
      telephony,
      setTelephony,
      tags,
      setTags,
      audience,
      setAudience,
      training,
      setTraining,
      schedule,
      setSchedule,
    }),
    [
      campaignId,
      draftHydrated,
      hydrateFromCampaign,
      resetWizard,
      voiceSetup,
      setVoiceSetup,
      selectedScenarioId,
      scenarioAttach,
      setScenarioAttach,
      telephony,
      setTelephony,
      tags,
      setTags,
      audience,
      setAudience,
      training,
      setTraining,
      schedule,
      setSchedule,
    ],
  );

  return (
    <VishingWizardContext.Provider value={value}>
      {children}
    </VishingWizardContext.Provider>
  );
};

export const useVishingWizard = (): VishingWizardContextValue => {
  const context = useContext(VishingWizardContext);
  if (!context) {
    throw new Error(
      'useVishingWizard must be used within VishingWizardProvider',
    );
  }
  return context;
};
