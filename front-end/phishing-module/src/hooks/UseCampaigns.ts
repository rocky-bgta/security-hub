import {
  CampaignStatus,
  CampaignChannel,
  IAllocateLicenceRequest,
  IAllocateLicenceResult,
  ICampaign,
  ICampaignAudienceForm,
  ICampaignCreateForm,
  ICampaignEmailTemplateForm,
  ICampaignLandingPageForm,
  ICampaignRecipient,
  ICampaignScheduleForm,
  ICampaignSenderProfileForm,
  ICampaignStats,
  ICampaignTagsForm,
  ICampaignTrainingForm,
} from 'models/Campaign';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  isSuccessResponse,
  normalizePaginatedList,
  objectToQueryString,
} from 'utils/Helper';
import { buildQueryString } from 'features/campaign/CampaignWizard/audience/helpers';
import {
  IDepartmentUserCount,
  IEndUser,
  IEndUserQueryParams,
  IRiskGroupUserCount,
} from 'features/campaign/CampaignWizard/audience/types';
import { useAPI } from './UseAPI';

interface UseCampaignsReturn {
  // State
  loading: boolean;
  saving: boolean;
  launching: boolean;
  userLoading: boolean;
  // CRUD Operations
  fetchCampaigns: (
    params: IGetListParams & {
      status?: CampaignStatus;
      channel?: CampaignChannel;
    },
  ) => Promise<IList<ICampaign>>;
  getCampaignById: (
    id: string,
    options?: { silent?: boolean },
  ) => Promise<ICampaign | null>;
  checkIsFirstCampaign: (
    productPackageId: string,
    campaignId: string,
  ) => Promise<boolean | null>;
  allocateLicence: (
    campaignId: string,
    payload: IAllocateLicenceRequest,
  ) => Promise<IResponse<IAllocateLicenceResult> | null>;
  getLicensedUserGroupCounts: (
    campaignId: string,
  ) => Promise<IRiskGroupUserCount[] | null>;
  getLicensedUserDepartmentCounts: (
    campaignId: string,
  ) => Promise<IDepartmentUserCount[] | null>;
  getLicensedUsers: (
    campaignId: string,
    query?: IEndUserQueryParams,
  ) => Promise<IList<IEndUser> | IEndUser[] | null>;
  createCampaign: (
    data: ICampaignCreateForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  deleteCampaign: (id: string) => Promise<boolean>;

  // Wizard Step Updates
  updateStep1: (
    id: string,
    data: ICampaignCreateForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep2: (
    id: string,
    data: ICampaignEmailTemplateForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep3: (
    id: string,
    data: ICampaignLandingPageForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep4: (
    id: string,
    data: ICampaignSenderProfileForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep5: (
    id: string,
    data: ICampaignTagsForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep6: (
    id: string,
    data: ICampaignAudienceForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep7: (
    id: string,
    data: ICampaignTrainingForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
  updateStep8: (
    id: string,
    data: ICampaignScheduleForm,
  ) => Promise<IResponse<ICampaign | null> | null>;

  // Lifecycle Actions
  launchCampaign: (id: string) => Promise<IResponse<ICampaign | null> | null>;
  pauseCampaign: (id: string) => Promise<IResponse<ICampaign | null> | null>;
  resumeCampaign: (id: string) => Promise<IResponse<ICampaign | null> | null>;
  cancelCampaign: (id: string) => Promise<IResponse<ICampaign | null> | null>;

  // Recipients & Stats
  fetchRecipients: (
    campaignId: string,
    params: IGetListParams,
  ) => Promise<IList<ICampaignRecipient>>;
  getCampaignStats: (id: string) => Promise<ICampaignStats | null>;
  refreshCampaignStats: (id: string) => Promise<ICampaignStats | null>;
  previewEmail: (
    campaignId: string,
    recipientId?: string,
  ) => Promise<string | null>;
}

export const useCampaigns = (): UseCampaignsReturn => {
  const { get, post, put, del } = useAPI();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [launching, setLaunching] = useState(false);
  const [userLoading, setUserLoading] = useState(true);
  // ============ CRUD Operations ============

  const fetchCampaigns = useCallback(
    async (
      params: IGetListParams & {
        status?: CampaignStatus;
        channel?: CampaignChannel;
      },
    ): Promise<IList<ICampaign>> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.CAMPAIGN_LIST}${queryString}`,
        );
        return response;
      } catch (error) {
        console.error('Error fetching campaigns:', error);
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
    ): Promise<ICampaign | null> => {
      if (!options?.silent) setLoading(true);
      try {
        const response = (await get(API_END_POINTS.CAMPAIGN_DETAILS(id))) as
          | IResponse<ICampaign>
          | undefined;
        return (response as IResponse<ICampaign>)?.data ?? null;
      } catch (error) {
        console.error('Error fetching campaign:', error);
        return null;
      } finally {
        if (!options?.silent) setLoading(false);
      }
    },
    [get],
  );

  const checkIsFirstCampaign = useCallback(
    async (
      productPackageId: string,
      campaignId: string,
    ): Promise<boolean | null> => {
      if (!productPackageId || !campaignId) return null;
      try {
        const response = (await get(
          `${API_END_POINTS.CAMPAIGN_IS_FIRST}productPackageId=${encodeURIComponent(productPackageId)}&campaignId=${encodeURIComponent(campaignId)}`,
        )) as IResponse<boolean> | undefined;
        if (!response || !isSuccessResponse(response.statusCode)) return null;
        return Boolean(response.data);
      } catch (error) {
        console.error('Error checking first campaign:', error);
        return null;
      }
    },
    [get],
  );

  const allocateLicence = useCallback(
    async (
      campaignId: string,
      payload: IAllocateLicenceRequest,
    ): Promise<IResponse<IAllocateLicenceResult> | null> => {
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_ALLOCATE_LICENCE(campaignId),
          { data: payload },
        )) as IResponse<IAllocateLicenceResult> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error allocating licences:', error);
        return null;
      }
    },
    [post],
  );

  const getLicensedUserGroupCounts = useCallback(
    async (campaignId: string): Promise<IRiskGroupUserCount[] | null> => {
      if (!campaignId) return null;
      try {
        const response = (await get(
          API_END_POINTS.CAMPAIGN_LICENSED_USER_GROUP_COUNTS(campaignId),
        )) as IResponse<IRiskGroupUserCount[] | unknown> | undefined;
        if (!response || !isSuccessResponse(response.statusCode)) return null;
        return (response.data as IRiskGroupUserCount[]) ?? null;
      } catch (error) {
        console.error('Error fetching licensed user group counts:', error);
        return null;
      }
    },
    [get],
  );

  const getLicensedUserDepartmentCounts = useCallback(
    async (campaignId: string): Promise<IDepartmentUserCount[] | null> => {
      if (!campaignId) return null;
      try {
        const response = (await get(
          API_END_POINTS.CAMPAIGN_LICENSED_USER_DEPARTMENT_COUNTS(campaignId),
        )) as IResponse<IDepartmentUserCount[] | unknown> | undefined;
        if (!response || !isSuccessResponse(response.statusCode)) return null;
        return (response.data as IDepartmentUserCount[]) ?? null;
      } catch (error) {
        console.error('Error fetching licensed user department counts:', error);
        return null;
      }
    },
    [get],
  );

  const getLicensedUsers = useCallback(
    async (
      campaignId: string,
      query?: IEndUserQueryParams,
    ): Promise<IList<IEndUser> | IEndUser[] | null> => {
      if (!campaignId) return null;
      try {
        const queryString = query ? buildQueryString(query) : '';
        const response = (await get(
          `${API_END_POINTS.CAMPAIGN_LICENSED_USERS(campaignId)}${queryString}`,
        )) as IResponse<IList<IEndUser> | IEndUser[]> | undefined;
        if (!response || !isSuccessResponse(response.statusCode)) return null;
        return response.data ?? null;
      } catch (error) {
        console.error('Error fetching licensed users:', error);
        return null;
      }
    },
    [get],
  );

  const createCampaign = useCallback(
    async (
      data: ICampaignCreateForm,
    ): Promise<IResponse<ICampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(API_END_POINTS.CAMPAIGN_CREATE, {
          data,
        })) as IResponse<ICampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error creating campaign:', error);
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
        console.error('Error deleting campaign:', error);
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  // ============ Wizard Step Updates ============

  const updateStep = useCallback(
    async <T>(
      id: string,
      step: number,
      data: T,
    ): Promise<IResponse<ICampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await put(
          API_END_POINTS.CAMPAIGN_UPDATE_STEP(id, step),
          { data },
        )) as IResponse<ICampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error(`Error updating step ${step}:`, error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const updateStep1 = useCallback(
    (id: string, data: ICampaignCreateForm) => updateStep(id, 1, data),
    [updateStep],
  );

  const updateStep2 = useCallback(
    (id: string, data: ICampaignEmailTemplateForm) => updateStep(id, 2, data),
    [updateStep],
  );

  const updateStep3 = useCallback(
    (id: string, data: ICampaignLandingPageForm) => updateStep(id, 3, data),
    [updateStep],
  );

  const updateStep4 = useCallback(
    (id: string, data: ICampaignSenderProfileForm) => updateStep(id, 4, data),
    [updateStep],
  );

  const updateStep5 = useCallback(
    (id: string, data: ICampaignTagsForm) => updateStep(id, 5, data),
    [updateStep],
  );

  const updateStep6 = useCallback(
    (id: string, data: ICampaignAudienceForm) => updateStep(id, 6, data),
    [updateStep],
  );

  const updateStep7 = useCallback(
    (id: string, data: ICampaignTrainingForm) => updateStep(id, 7, data),
    [updateStep],
  );

  const updateStep8 = useCallback(
    (id: string, data: ICampaignScheduleForm) => updateStep(id, 8, data),
    [updateStep],
  );

  // ============ Lifecycle Actions ============

  const launchCampaign = useCallback(
    async (id: string): Promise<IResponse<ICampaign | null> | null> => {
      setLaunching(true);
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_LAUNCH(id),
          {},
        )) as IResponse<ICampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error launching campaign:', error);
        return null;
      } finally {
        setLaunching(false);
      }
    },
    [post],
  );

  const pauseCampaign = useCallback(
    async (id: string): Promise<IResponse<ICampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(API_END_POINTS.CAMPAIGN_PAUSE(id), {})) as
          | IResponse<ICampaign | null>
          | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error pausing campaign:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const resumeCampaign = useCallback(
    async (id: string): Promise<IResponse<ICampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_RESUME(id),
          {},
        )) as IResponse<ICampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error resuming campaign:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const cancelCampaign = useCallback(
    async (id: string): Promise<IResponse<ICampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_CANCEL(id),
          {},
        )) as IResponse<ICampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error cancelling campaign:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  // ============ Recipients & Stats ============

  const fetchRecipients = useCallback(
    async (
      campaignId: string,
      params: IGetListParams,
    ): Promise<IList<ICampaignRecipient>> => {
      setUserLoading(true);
      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.CAMPAIGN_RECIPIENTS(campaignId)}?${queryString}`,
        );
        return normalizePaginatedList<ICampaignRecipient>(response);
      } catch (error) {
        console.error('Error fetching recipients:', error);
        return {
          offset: 0,
          pageSize: 0,
          total: 0,
          items: [],
        };
      } finally {
        setUserLoading(false);
      }
    },
    [get],
  );

  const getCampaignStats = useCallback(
    async (id: string): Promise<ICampaignStats | null> => {
      try {
        const response = (await get(API_END_POINTS.CAMPAIGN_STATS(id))) as
          | IResponse<ICampaignStats>
          | undefined;
        return (response as IResponse<ICampaignStats>)?.data ?? null;
      } catch (error) {
        console.error('Error fetching campaign stats:', error);
        return null;
      }
    },
    [get],
  );

  const refreshCampaignStats = useCallback(
    async (id: string): Promise<ICampaignStats | null> => {
      try {
        const response = (await post(
          API_END_POINTS.CAMPAIGN_STATS_REFRESH(id),
          {},
        )) as IResponse<ICampaignStats> | undefined;
        return (response as IResponse<ICampaignStats>)?.data ?? null;
      } catch (error) {
        console.error('Error refreshing campaign stats:', error);
        return null;
      }
    },
    [post],
  );

  const previewEmail = useCallback(
    async (
      campaignId: string,
      recipientId?: string,
    ): Promise<string | null> => {
      try {
        const url = recipientId
          ? `${API_END_POINTS.CAMPAIGN_PREVIEW_EMAIL(campaignId)}?recipientId=${recipientId}`
          : API_END_POINTS.CAMPAIGN_PREVIEW_EMAIL(campaignId);
        const response = (await get(url)) as IResponse<string> | undefined;
        return (response as IResponse<string>)?.data ?? null;
      } catch (error) {
        console.error('Error previewing email:', error);
        return null;
      }
    },
    [get],
  );

  return {
    loading,
    saving,
    launching,
    userLoading,
    fetchCampaigns,
    getCampaignById,
    checkIsFirstCampaign,
    allocateLicence,
    getLicensedUserGroupCounts,
    getLicensedUserDepartmentCounts,
    getLicensedUsers,
    createCampaign,
    deleteCampaign,
    updateStep1,
    updateStep2,
    updateStep3,
    updateStep4,
    updateStep5,
    updateStep6,
    updateStep7,
    updateStep8,
    launchCampaign,
    pauseCampaign,
    resumeCampaign,
    cancelCampaign,
    fetchRecipients,
    getCampaignStats,
    refreshCampaignStats,
    previewEmail,
  };
};
