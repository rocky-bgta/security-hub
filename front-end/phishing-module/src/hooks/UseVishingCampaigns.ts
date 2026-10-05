import { useCallback, useState } from 'react';
import { useAPI } from 'hooks/UseAPI';
import type { IGetListParams, IList, IResponse } from 'models/Global';
import type {
  IVishingAudienceRequest,
  IVishingCampaign,
  IVishingCampaignCreateRequest,
  IVishingCampaignListParams,
  IVishingCampaignStats,
  IVishingDataCapture,
  IVishingEndUser,
  IVishingLiveMetrics,
  IVishingRecipient,
  IVishingRemediation,
  IVishingReport,
  IVishingScheduleRequest,
  IVishingScenarioAttachRequest,
  IVishingTagsRequest,
  IVishingTeachableMomentRequest,
  IVishingTelephonyRequest,
  IVishingTestCallRequest,
  IVishingTrainingRequest,
  IVishingVoiceSetupDraft,
} from 'models/Vishing';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  normalizePaginatedList,
  objectToQueryString,
  unwrapResponseData,
} from 'utils/Helper';

interface UseVishingCampaignsReturn {
  loading: boolean;
  saving: boolean;
  launching: boolean;
  fetchCampaigns: (
    params: IVishingCampaignListParams,
  ) => Promise<IList<IVishingCampaign>>;
  getCampaignById: (
    id: string,
    options?: { silent?: boolean },
  ) => Promise<IVishingCampaign | null>;
  createCampaign: (
    data: IVishingCampaignCreateRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  deleteCampaign: (id: string) => Promise<boolean>;
  updateSetup: (
    id: string,
    data: IVishingCampaignCreateRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateVoiceSetup: (
    id: string,
    data: IVishingVoiceSetupDraft,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateScenario: (
    id: string,
    data: IVishingScenarioAttachRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateTelephony: (
    id: string,
    data: IVishingTelephonyRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateTags: (
    id: string,
    data: IVishingTagsRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateAudience: (
    id: string,
    data: IVishingAudienceRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateTraining: (
    id: string,
    data: IVishingTrainingRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  updateSchedule: (
    id: string,
    data: IVishingScheduleRequest,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  launchCampaign: (
    id: string,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  pauseCampaign: (
    id: string,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  resumeCampaign: (
    id: string,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  cancelCampaign: (
    id: string,
  ) => Promise<IResponse<IVishingCampaign | null> | null>;
  fetchRecipients: (
    campaignId: string,
    params: IGetListParams,
  ) => Promise<IList<IVishingRecipient>>;
  getCampaignStats: (id: string) => Promise<IVishingCampaignStats | null>;
  refreshCampaignStats: (id: string) => Promise<IVishingCampaignStats | null>;
  getAvailableTags: () => Promise<string[]>;
  fetchEndUsers: (params?: IGetListParams) => Promise<IList<IVishingEndUser>>;
  getDataCapture: (
    campaignId: string,
    params?: IGetListParams,
  ) => Promise<IVishingDataCapture | null>;
  getSuccessKeywords: (campaignId: string) => Promise<string[]>;
  updateSuccessKeywords: (
    campaignId: string,
    keywords: string[],
  ) => Promise<string[] | null>;
  getRemediation: (campaignId: string) => Promise<IVishingRemediation | null>;
  sendTeachableMoment: (
    campaignId: string,
    data: IVishingTeachableMomentRequest,
  ) => Promise<boolean>;
  getReport: (campaignId: string) => Promise<IVishingReport | null>;
  exportReport: (
    campaignId: string,
    format?: 'json' | 'csv',
    anonymize?: boolean,
  ) => Promise<Blob | null>;
  getLiveMetrics: (campaignId: string) => Promise<IVishingLiveMetrics | null>;
  sendTestCall: (
    campaignId: string,
    data: IVishingTestCallRequest,
  ) => Promise<boolean>;
}

export const useVishingCampaigns = (): UseVishingCampaignsReturn => {
  const { get, post, put, del } = useAPI();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [launching, setLaunching] = useState(false);

  const fetchCampaigns = useCallback(
    async (
      params: IVishingCampaignListParams,
    ): Promise<IList<IVishingCampaign>> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString({
          ...params,
          channel: params.channel ?? 'VOICE',
        } as Record<string, unknown>);
        const response = await get(
          `${API_END_POINTS.CAMPAIGN_LIST}${queryString}`,
        );
        return normalizePaginatedList<IVishingCampaign>(response);
      } catch (error) {
        console.error('Error fetching vishing campaigns:', error);
        return { offset: 0, pageSize: 0, total: 0, items: [] };
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getCampaignById = useCallback(
    async (
      id: string,
      options?: { silent?: boolean },
    ): Promise<IVishingCampaign | null> => {
      if (!options?.silent) setLoading(true);
      try {
        const response = (await get(API_END_POINTS.CAMPAIGN_DETAILS(id))) as
          | IResponse<IVishingCampaign>
          | undefined;
        return unwrapResponseData<IVishingCampaign>(response) ?? null;
      } catch (error) {
        console.error('Error fetching vishing campaign:', error);
        return null;
      } finally {
        if (!options?.silent) setLoading(false);
      }
    },
    [get],
  );

  const createCampaign = useCallback(
    async (
      data: IVishingCampaignCreateRequest,
    ): Promise<IResponse<IVishingCampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(API_END_POINTS.CAMPAIGN_CREATE, {
          data,
        })) as IResponse<IVishingCampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error creating vishing campaign:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const deleteCampaign = useCallback(
    async (id: string): Promise<boolean> => {
      setSaving(true);
      try {
        await del(API_END_POINTS.CAMPAIGN_DELETE(id));
        return true;
      } catch (error) {
        console.error('Error deleting vishing campaign:', error);
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  const putCampaignStep = useCallback(
    async <T>(
      url: string,
      data: T,
    ): Promise<IResponse<IVishingCampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await put(url, { data })) as
          | IResponse<IVishingCampaign | null>
          | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error saving vishing campaign step:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const updateSetup = useCallback(
    (id: string, data: IVishingCampaignCreateRequest) =>
      putCampaignStep(API_END_POINTS.CAMPAIGN_UPDATE_STEP(id, 1), data),
    [putCampaignStep],
  );

  const updateVoiceSetup = useCallback(
    async (
      id: string,
      data: IVishingVoiceSetupDraft,
    ): Promise<IResponse<IVishingCampaign | null> | null> => {
      setSaving(true);
      try {
        const formData = new FormData();
        formData.append(
          'consentConfirmed',
          String(Boolean(data.consentConfirmed)),
        );
        if (data.consentText) {
          formData.append('consentText', data.consentText);
        }

        // Case 1 — upload new audio (create clone)
        if (data.audioFile) {
          const provider = data.cloningEngine ?? 'ELEVENLABS';
          const language = data.language ?? 'en';
          const voiceName = data.voiceName?.trim();
          const url =
            `${API_END_POINTS.VISHING_CAMPAIGN_STEP_VOICE_SETUP(id)}` +
            `?provider=${encodeURIComponent(provider)}` +
            `&language=${encodeURIComponent(language)}` +
            (voiceName ? `&voiceName=${encodeURIComponent(voiceName)}` : '');

          formData.append('file', data.audioFile, data.audioFile.name);

          const response = (await put(url, {
            data: formData,
            headers: { 'Content-Type': 'multipart/form-data' },
          })) as IResponse<IVishingCampaign | null> | undefined;

          return response ?? null;
        }

        // Case 2 — reuse existing clone
        if (!data.voiceCloneId) {
          return null;
        }

        const voiceName = data.voiceName?.trim();
        const url =
          `${API_END_POINTS.VISHING_CAMPAIGN_STEP_VOICE_SETUP(id)}` +
          `?voiceCloneId=${encodeURIComponent(data.voiceCloneId)}` +
          (voiceName ? `&voiceName=${encodeURIComponent(voiceName)}` : '');

        const response = (await put(url, {
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
        })) as IResponse<IVishingCampaign | null> | undefined;

        return response ?? null;
      } catch (error) {
        console.error('Error saving vishing voice setup:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const updateScenario = useCallback(
    (id: string, data: IVishingScenarioAttachRequest) =>
      putCampaignStep(API_END_POINTS.VISHING_CAMPAIGN_STEP_SCENARIO(id), data),
    [putCampaignStep],
  );

  const updateTelephony = useCallback(
    (id: string, data: IVishingTelephonyRequest) =>
      putCampaignStep(API_END_POINTS.VISHING_CAMPAIGN_STEP_TELEPHONY(id), data),
    [putCampaignStep],
  );

  const updateTags = useCallback(
    (id: string, data: IVishingTagsRequest) =>
      putCampaignStep(API_END_POINTS.CAMPAIGN_UPDATE_STEP(id, 5), data),
    [putCampaignStep],
  );

  const updateAudience = useCallback(
    (id: string, data: IVishingAudienceRequest) =>
      putCampaignStep(API_END_POINTS.CAMPAIGN_UPDATE_STEP(id, 6), data),
    [putCampaignStep],
  );

  const updateTraining = useCallback(
    (id: string, data: IVishingTrainingRequest) =>
      putCampaignStep(API_END_POINTS.CAMPAIGN_UPDATE_STEP(id, 7), data),
    [putCampaignStep],
  );

  const updateSchedule = useCallback(
    (id: string, data: IVishingScheduleRequest) =>
      putCampaignStep(API_END_POINTS.CAMPAIGN_UPDATE_STEP(id, 8), data),
    [putCampaignStep],
  );

  const postCampaignAction = useCallback(
    async (url: string): Promise<IResponse<IVishingCampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(url, {})) as
          | IResponse<IVishingCampaign | null>
          | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error performing vishing campaign action:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const launchCampaign = useCallback(
    async (id: string): Promise<IResponse<IVishingCampaign | null> | null> => {
      setLaunching(true);
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_LAUNCH(id),
          {},
        )) as IResponse<IVishingCampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error launching vishing campaign:', error);
        return null;
      } finally {
        setLaunching(false);
      }
    },
    [post],
  );

  const pauseCampaign = useCallback(
    (id: string) => postCampaignAction(API_END_POINTS.CAMPAIGN_PAUSE(id)),
    [postCampaignAction],
  );

  const resumeCampaign = useCallback(
    (id: string) => postCampaignAction(API_END_POINTS.CAMPAIGN_RESUME(id)),
    [postCampaignAction],
  );

  const cancelCampaign = useCallback(
    (id: string) => postCampaignAction(API_END_POINTS.CAMPAIGN_CANCEL(id)),
    [postCampaignAction],
  );

  const fetchRecipients = useCallback(
    async (
      campaignId: string,
      params: IGetListParams,
    ): Promise<IList<IVishingRecipient>> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.CAMPAIGN_RECIPIENTS(campaignId)}?${queryString}`,
        );
        return normalizePaginatedList<IVishingRecipient>(response);
      } catch (error) {
        console.error('Error fetching vishing recipients:', error);
        return { offset: 0, pageSize: 0, total: 0, items: [] };
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getCampaignStats = useCallback(
    async (id: string): Promise<IVishingCampaignStats | null> => {
      setLoading(true);
      try {
        const response = (await get(API_END_POINTS.CAMPAIGN_STATS(id))) as
          | IResponse<IVishingCampaignStats>
          | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching vishing campaign stats:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const refreshCampaignStats = useCallback(
    async (id: string): Promise<IVishingCampaignStats | null> => {
      setSaving(true);
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_STATS_REFRESH(id),
          {},
        )) as IResponse<IVishingCampaignStats> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error refreshing vishing campaign stats:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const getAvailableTags = useCallback(async (): Promise<string[]> => {
    setLoading(true);
    try {
      const response = (await get(API_END_POINTS.EMAIL_TEMPLATE_TAGS)) as
        | IResponse<string[]>
        | undefined;
      return response?.data ?? [];
    } catch (error) {
      console.error('Error fetching campaign tags:', error);
      return [];
    } finally {
      setLoading(false);
    }
  }, [get]);

  const fetchEndUsers = useCallback(
    async (params: IGetListParams = {}): Promise<IList<IVishingEndUser>> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.END_USER_LIST}${queryString}`,
        );
        return {
          ...response.data,
        };
      } catch (error) {
        console.error('Error fetching end users:', error);
        return { offset: 0, pageSize: 0, total: 0, items: [] };
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getDataCapture = useCallback(
    async (
      campaignId: string,
      params: IGetListParams = {},
    ): Promise<IVishingDataCapture | null> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = (await get(
          `${API_END_POINTS.VISHING_DATA_CAPTURE(campaignId)}?${queryString}`,
        )) as IResponse<IVishingDataCapture> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching vishing data capture:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getSuccessKeywords = useCallback(
    async (campaignId: string): Promise<string[]> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.VISHING_SUCCESS_KEYWORDS(campaignId),
        )) as IResponse<string[]> | undefined;
        const payload = unwrapResponseData<string[] | { keywords?: string[] }>(
          response,
        );
        if (Array.isArray(payload)) return payload;
        return payload?.keywords ?? [];
      } catch (error) {
        console.error('Error fetching success keywords:', error);
        return [];
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const updateSuccessKeywords = useCallback(
    async (
      campaignId: string,
      keywords: string[],
    ): Promise<string[] | null> => {
      setSaving(true);
      try {
        const response = (await put(
          API_END_POINTS.VISHING_SUCCESS_KEYWORDS(campaignId),
          { data: { keywords } },
        )) as IResponse<string[]> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error updating success keywords:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const getRemediation = useCallback(
    async (campaignId: string): Promise<IVishingRemediation | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.VISHING_REMEDIATION(campaignId),
        )) as IResponse<IVishingRemediation> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching vishing remediation:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const sendTeachableMoment = useCallback(
    async (
      campaignId: string,
      data: IVishingTeachableMomentRequest,
    ): Promise<boolean> => {
      setSaving(true);
      try {
        await post(API_END_POINTS.VISHING_TEACHABLE_MOMENT(campaignId), {
          data,
        });
        return true;
      } catch (error) {
        console.error('Error sending teachable moment:', error);
        return false;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const getReport = useCallback(
    async (campaignId: string): Promise<IVishingReport | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.VISHING_REPORT(campaignId),
        )) as IResponse<IVishingReport> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching vishing report:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const exportReport = useCallback(
    async (
      campaignId: string,
      format: 'json' | 'csv' = 'csv',
      anonymize = false,
    ): Promise<Blob | null> => {
      setSaving(true);
      try {
        const query = objectToQueryString({
          format,
          anonymize,
        } as Record<string, unknown>);
        const response = (await get(
          `${API_END_POINTS.VISHING_REPORT_EXPORT(campaignId)}?${query}`,
          { responseType: 'blob' },
        )) as Blob | IResponse<Blob> | undefined;
        return (
          (response as IResponse<Blob>)?.data || (response as Blob) || null
        );
      } catch (error) {
        console.error('Error exporting vishing report:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [get],
  );

  const getLiveMetrics = useCallback(
    async (campaignId: string): Promise<IVishingLiveMetrics | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.VISHING_LIVE_METRICS(campaignId),
        )) as IResponse<IVishingLiveMetrics> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching vishing live metrics:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const sendTestCall = useCallback(
    async (
      campaignId: string,
      data: IVishingTestCallRequest,
    ): Promise<boolean> => {
      setSaving(true);
      try {
        await post(API_END_POINTS.VISHING_TEST_CALL(campaignId), {
          data,
        });
        return true;
      } catch (error) {
        console.error('Error sending vishing test call:', error);
        return false;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  return {
    loading,
    saving,
    launching,
    fetchCampaigns,
    getCampaignById,
    createCampaign,
    deleteCampaign,
    updateSetup,
    updateVoiceSetup,
    updateScenario,
    updateTelephony,
    updateTags,
    updateAudience,
    updateTraining,
    updateSchedule,
    launchCampaign,
    pauseCampaign,
    resumeCampaign,
    cancelCampaign,
    fetchRecipients,
    getCampaignStats,
    refreshCampaignStats,
    getAvailableTags,
    fetchEndUsers,
    getDataCapture,
    getSuccessKeywords,
    updateSuccessKeywords,
    getRemediation,
    sendTeachableMoment,
    getReport,
    exportReport,
    getLiveMetrics,
    sendTestCall,
  };
};
