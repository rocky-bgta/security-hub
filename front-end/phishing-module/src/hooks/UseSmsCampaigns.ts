import {
  ICampaign,
  ICampaignAudienceForm,
  ICampaignCreateForm,
  ICampaignEmailTemplateForm,
  ICampaignLandingPageForm,
  ICampaignScheduleForm,
  ICampaignTagsForm,
  ICampaignTrainingForm,
} from 'models/Campaign';
import { ISmsCampaignServerForm } from 'models/SmsCampaign';
import { IResponse } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from './UseAPI';

interface UseSmsCampaignsReturn {
  loading: boolean;
  saving: boolean;
  launching: boolean;
  getCampaignById: (
    id: string,
    options?: { silent?: boolean },
  ) => Promise<ICampaign | null>;
  createCampaign: (
    data: ICampaignCreateForm,
  ) => Promise<IResponse<ICampaign | null> | null>;
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
    data: ISmsCampaignServerForm,
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
  launchCampaign: (id: string) => Promise<IResponse<ICampaign | null> | null>;
}

export const useSmsCampaigns = (): UseSmsCampaignsReturn => {
  const { get, post, put } = useAPI();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [launching, setLaunching] = useState(false);

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
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching SMS campaign:', error);
        return null;
      } finally {
        if (!options?.silent) setLoading(false);
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
        console.error('Error creating SMS campaign:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

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
        console.error(`Error updating SMS campaign step ${step}:`, error);
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
    async (
      id: string,
      data: ISmsCampaignServerForm,
    ): Promise<IResponse<ICampaign | null> | null> => {
      setSaving(true);
      try {
        const response = (await put(
          API_END_POINTS.CAMPAIGN_UPDATE_STEP_SMS_SERVER(id),
          { data },
        )) as IResponse<ICampaign | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error(
          'Error updating SMS campaign step 4 (sms-server):',
          error,
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
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
        console.error('Error launching SMS campaign:', error);
        return null;
      } finally {
        setLaunching(false);
      }
    },
    [post],
  );

  return {
    loading,
    saving,
    launching,
    getCampaignById,
    createCampaign,
    updateStep1,
    updateStep2,
    updateStep3,
    updateStep4,
    updateStep5,
    updateStep6,
    updateStep7,
    updateStep8,
    launchCampaign,
  };
};
