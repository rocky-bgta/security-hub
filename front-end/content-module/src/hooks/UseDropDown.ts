import { IList, IResponse } from 'models/Global';
import { useCallback } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from './UseAPI';
import { IDropdownOption } from 'models/DropDown';

export interface IIndustryItem {
  id: string;
  code: string;
  name: string;
  active: boolean;
}

const useDropDown = () => {
  const apiClient = useAPI();

  const fetchConfigurationDropdown = useCallback(
    async (endpoint: string): Promise<IList<IDropdownOption>> => {
      try {
        const response = await apiClient.get(
          endpoint + '?isActive=true&sortOrder=asc&sortBy=displayOrder',
        );
        return (
          response?.data || { offset: 0, pageSize: 10, total: 0, items: [] }
        );
      } catch (error) {
        console.error(`Error fetching dropdown data from ${endpoint}:`, error);
        return { offset: 0, pageSize: 10, total: 0, items: [] };
      }
    },
    [apiClient],
  );

  const fetchPayloadTypes = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_PAYLOAD_TYPES),
    [fetchConfigurationDropdown],
  );

  const fetchDifficulty = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_DIFFICULTY),
    [fetchConfigurationDropdown],
  );

  const fetchTones = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_TONES),
    [fetchConfigurationDropdown],
  );

  const fetchAttackerPersonas = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_ATTACKER_PERSONAS),
    [fetchConfigurationDropdown],
  );

  const fetchSocialEngineeringStrategies = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_SOCIAL_ENGINEERING_STRATEGIES),
    [fetchConfigurationDropdown],
  );

  const fetchCampaignObjectives = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_CAMPAIGN_OBJECTIVES),
    [fetchConfigurationDropdown],
  );

  const fetchTriggerEvents = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_TRIGGER_EVENTS),
    [fetchConfigurationDropdown],
  );

  const fetchAttackTechniques = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_ATTACK_TECHNIQUES),
    [fetchConfigurationDropdown],
  );

  const fetchEmotionalTriggers = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_EMOTIONAL_TRIGGERS),
    [fetchConfigurationDropdown],
  );

  const fetchUrgencyLevels = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_URGENCY_LEVELS),
    [fetchConfigurationDropdown],
  );

  const fetchBrands = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_BRANDS),
    [fetchConfigurationDropdown],
  );

  const fetchCallToActions = useCallback(
    () => fetchConfigurationDropdown(API_END_POINTS.CONFIGURATION_CALL_TO_ACTIONS),
    [fetchConfigurationDropdown],
  );

  const fetchIndustries = useCallback(
    async (): Promise<IResponse<IIndustryItem[]>> =>
      (await apiClient.get(
        API_END_POINTS.CONFIGURATION_INDUSTRIES,
      )) as IResponse<IIndustryItem[]>,
    [apiClient],
  );

  const fetchSubIndustries = useCallback(
    async (): Promise<IResponse<IIndustryItem[]>> =>
      (await apiClient.get(
        API_END_POINTS.CONFIGURATION_SUB_INDUSTRIES,
      )) as IResponse<IIndustryItem[]>,
    [apiClient],
  );

  return {
    fetchPayloadTypes,
    fetchDifficulty,
    fetchTones,
    fetchAttackerPersonas,
    fetchSocialEngineeringStrategies,
    fetchCampaignObjectives,
    fetchTriggerEvents,
    fetchAttackTechniques,
    fetchEmotionalTriggers,
    fetchUrgencyLevels,
    fetchBrands,
    fetchCallToActions,
    fetchIndustries,
    fetchSubIndustries,
  };
};

export default useDropDown;
