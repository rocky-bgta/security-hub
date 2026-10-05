import { CampaignChannel } from 'models/Campaign';
import { IDropdownItem } from 'models/DropDown';
import { IList, IResponse } from 'models/Global';
import { useCallback } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

/**
 * Custom hook for fetching dropdown configuration data
 */
const useDropDown = () => {
  const { get } = useAPI();

  const fetchConfigurationDropdown = useCallback(
    async (
      endpoint: string,
      extraParams?: Record<string, string>,
    ): Promise<IList<IDropdownItem>> => {
      try {
        const queryString = objectToQueryString({
          isActive: true,
          sortOrder: 'asc',
          sortBy: 'displayOrder',
          ...extraParams,
        });
        const response = (await get(
          `${endpoint}?${queryString}`,
        )) as IResponse<IList<IDropdownItem>> | undefined;
        return (
          response?.data || { offset: 0, pageSize: 10, total: 0, items: [] }
        );
      } catch (error) {
        console.error(`Error fetching dropdown data from ${endpoint}:`, error);
        return { offset: 0, pageSize: 10, total: 0, items: [] };
      }
    },
    [get],
  );

  const fetchPayloadTypes = useCallback(
    async (channel?: CampaignChannel): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_PAYLOAD_TYPES,
        channel ? { channel } : undefined,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchLandingPageCategories = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_LANDING_PAGE_CATEGORIES,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchDataCaptureTypes = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_DATA_CAPTURE_TYPES,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchCampaignObjectives = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_CAMPAIGN_OBJECTIVES,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchPersonalizationLevels = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_PERSONALIZATION_LEVELS,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchBrands = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_BRANDS),
    [fetchConfigurationDropdown],
  );

  const fetchDeceptionLevels = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_DECEPTION_LEVELS),
    [fetchConfigurationDropdown],
  );

  const fetchTones = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_TONES),
    [fetchConfigurationDropdown],
  );

  const fetchAttackerPersonas = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_ATTACKER_PERSONAS,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchCallToActions = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_CALL_TO_ACTIONS),
    [fetchConfigurationDropdown],
  );

  const fetchSocialEngineeringStrategies = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_SOCIAL_ENGINEERING_STRATEGIES,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchEmotionalTriggers = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_EMOTIONAL_TRIGGERS,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchAttackTechniques = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_ATTACK_TECHNIQUES,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchTriggerEvents = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_TRIGGER_EVENTS),
    [fetchConfigurationDropdown],
  );

  const fetchExpectedUserActions = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.CONFIGURATION_EXPECTED_USER_ACTIONS,
      ),
    [fetchConfigurationDropdown],
  );

  const fetchConstraints = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_CONSTRAINTS_DATA),
    [fetchConfigurationDropdown],
  );

  const fetchDifficulty = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_DIFFICULTY),
    [fetchConfigurationDropdown],
  );

  const fetchUrgencyLevels = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_URGENCY_LEVELS),
    [fetchConfigurationDropdown],
  );

  const fetchIndustries = useCallback(
    async (): Promise<IResponse<Array<{ id: string; name: string }>>> =>
      (await get(API_END_POINTS.GET_ACTIVE_INDUSTRIES_LIST)) as IResponse<
        Array<{ id: string; name: string }>
      >,
    [get],
  );

  const fetchLanguages = useCallback(
    async (): Promise<IResponse<Array<{ id: string; displayName: string }>>> =>
      (await get(API_END_POINTS.GET_ACTIVE_LANGUAGE_LIST)) as IResponse<
        Array<{ id: string; displayName: string }>
      >,
    [get],
  );

  const fetchDepartments = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(
        API_END_POINTS.DEPARTMENT_LIST + '?isActive=true',
      ),
    [fetchConfigurationDropdown],
  );

  const fetchDifficulties = useCallback(
    async (): Promise<IList<IDropdownItem>> =>
      fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_DIFFICULTIES),
    [fetchConfigurationDropdown],
  );

  return {
    fetchPayloadTypes,
    fetchLandingPageCategories,
    fetchDataCaptureTypes,
    fetchCampaignObjectives,
    fetchPersonalizationLevels,
    fetchBrands,
    fetchDeceptionLevels,
    fetchTones,
    fetchAttackerPersonas,
    fetchCallToActions,
    fetchSocialEngineeringStrategies,
    fetchEmotionalTriggers,
    fetchAttackTechniques,
    fetchTriggerEvents,
    fetchExpectedUserActions,
    fetchConstraints,
    fetchDifficulty,
    fetchUrgencyLevels,
    fetchIndustries,
    fetchLanguages,
    fetchDepartments,
    fetchDifficulties,
  };
};

export default useDropDown;
