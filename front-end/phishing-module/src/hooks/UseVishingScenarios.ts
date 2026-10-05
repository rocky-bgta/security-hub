import { useCallback, useState } from 'react';
import { useAPI } from 'hooks/UseAPI';
import type { IGetListParams, IList, IResponse } from 'models/Global';
import type {
  IVishingScenario,
  IVishingScenarioCreateRequest,
} from 'models/Vishing';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  normalizePaginatedList,
  objectToQueryString,
  unwrapResponseData,
} from 'utils/Helper';

interface UseVishingScenariosReturn {
  loading: boolean;
  saving: boolean;
  fetchScenarios: (params?: IGetListParams) => Promise<IList<IVishingScenario>>;
  getScenarioById: (id: string) => Promise<IVishingScenario | null>;
  createScenario: (
    data: IVishingScenarioCreateRequest,
  ) => Promise<IVishingScenario | null>;
  updateScenario: (
    id: string,
    data: IVishingScenarioCreateRequest,
  ) => Promise<IVishingScenario | null>;
  deleteScenario: (id: string) => Promise<boolean>;
  publishScenario: (id: string) => Promise<IVishingScenario | null>;
}

export const useVishingScenarios = (): UseVishingScenariosReturn => {
  const { get, post, put, del } = useAPI();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const fetchScenarios = useCallback(
    async (params: IGetListParams = {}): Promise<IList<IVishingScenario>> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString(params as Record<string, unknown>);
        const response = (await get(
          `${API_END_POINTS.VISHING_SCENARIO_LIST}${queryString}`,
        )) as IResponse<IList<IVishingScenario>> | undefined;
        return normalizePaginatedList<IVishingScenario>(
          unwrapResponseData<IList<IVishingScenario>>(response),
        );
      } catch (error) {
        console.error('Error fetching vishing scenarios:', error);
        return { offset: 0, pageSize: 0, total: 0, items: [] };
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getScenarioById = useCallback(
    async (id: string): Promise<IVishingScenario | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.VISHING_SCENARIO_DETAILS(id),
        )) as IResponse<IVishingScenario> | undefined;
        return unwrapResponseData<IVishingScenario>(response) ?? null;
      } catch (error) {
        console.error('Error fetching vishing scenario:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const createScenario = useCallback(
    async (
      data: IVishingScenarioCreateRequest,
    ): Promise<IVishingScenario | null> => {
      setSaving(true);
      try {
        const response = (await post(API_END_POINTS.VISHING_SCENARIO_CREATE, {
          data,
        })) as IResponse<IVishingScenario> | undefined;
        return unwrapResponseData<IVishingScenario>(response) ?? null;
      } catch (error) {
        console.error('Error creating vishing scenario:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const updateScenario = useCallback(
    async (
      id: string,
      data: IVishingScenarioCreateRequest,
    ): Promise<IVishingScenario | null> => {
      setSaving(true);
      try {
        const response = (await put(
          API_END_POINTS.VISHING_SCENARIO_DETAILS(id),
          { data },
        )) as IResponse<IVishingScenario> | undefined;
        return unwrapResponseData<IVishingScenario>(response) ?? null;
      } catch (error) {
        console.error('Error updating vishing scenario:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const deleteScenario = useCallback(
    async (id: string): Promise<boolean> => {
      setSaving(true);
      try {
        await del(API_END_POINTS.VISHING_SCENARIO_DETAILS(id));
        return true;
      } catch (error) {
        console.error('Error deleting vishing scenario:', error);
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  const publishScenario = useCallback(
    async (id: string): Promise<IVishingScenario | null> => {
      setSaving(true);
      try {
        const response = (await post(
          API_END_POINTS.VISHING_SCENARIO_PUBLISH(id),
          {},
        )) as IResponse<IVishingScenario> | undefined;
        return unwrapResponseData<IVishingScenario>(response) ?? null;
      } catch (error) {
        console.error('Error publishing vishing scenario:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  return {
    loading,
    saving,
    fetchScenarios,
    getScenarioById,
    createScenario,
    updateScenario,
    deleteScenario,
    publishScenario,
  };
};
