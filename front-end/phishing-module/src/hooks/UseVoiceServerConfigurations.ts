import { IList, IResponse } from 'models/Global';
import {
  IVoiceServerConfiguration,
  IVoiceServerConfigurationForm,
  IVoiceServerConfigurationListParams,
  IVoiceServerTestRequest,
} from 'models/VoiceServerConfiguration';
import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { normalizePaginatedList, objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

interface ITestResult {
  success: boolean;
  message: string;
}

const emptyList = (
  params: IVoiceServerConfigurationListParams = {},
): IList<IVoiceServerConfiguration> => ({
  offset: params.offset ?? 0,
  pageSize: params.pageSize ?? 10,
  total: 0,
  items: [],
});

/**
 * Custom hook for voice server configuration management
 */
export const useVoiceServerConfigurations = () => {
  const { get, post, put, del } = useAPI();

  const [configurations, setConfigurations] =
    useState<IList<IVoiceServerConfiguration>>(emptyList());
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const fetchConfigurations = useCallback(
    async (
      params: IVoiceServerConfigurationListParams = {},
    ): Promise<IList<IVoiceServerConfiguration>> => {
      try {
        setLoading(true);
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = (await get(
          `${API_END_POINTS.VOICE_SERVER_LIST}${queryString}`,
        )) as
          | IResponse<IList<IVoiceServerConfiguration>>
          | IList<IVoiceServerConfiguration>
          | undefined;

        let payload: IList<IVoiceServerConfiguration> | undefined;
        if (response && 'items' in response && Array.isArray(response.items)) {
          payload = response;
        } else if (response && 'data' in response) {
          payload = response.data;
        }

        const list = normalizePaginatedList<IVoiceServerConfiguration>(
          payload,
          emptyList(params),
        );
        setConfigurations(list);
        return list;
      } catch (error) {
        console.error('Error fetching voice server configurations:', error);
        toast.error('Failed to load voice server configurations');
        const fallback = emptyList(params);
        setConfigurations(fallback);
        return fallback;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getDefaultConfiguration = useCallback(async () => {
    try {
      setDetailLoading(true);
      const response = (await get(API_END_POINTS.VOICE_SERVER_DEFAULT)) as
        | IResponse<IVoiceServerConfiguration>
        | undefined;
      return response?.data ?? null;
    } catch (error) {
      console.error('Error fetching default voice server:', error);
      return null;
    } finally {
      setDetailLoading(false);
    }
  }, [get]);

  const getConfigurationById = useCallback(
    async (id: string): Promise<IVoiceServerConfiguration | null> => {
      try {
        setDetailLoading(true);
        const response = (await get(
          API_END_POINTS.VOICE_SERVER_DETAILS(id),
        )) as IResponse<IVoiceServerConfiguration> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching voice server configuration:', error);
        toast.error('Failed to load voice server configuration');
        return null;
      } finally {
        setDetailLoading(false);
      }
    },
    [get],
  );

  const createConfiguration = useCallback(
    async (
      data: IVoiceServerConfigurationForm,
    ): Promise<IVoiceServerConfiguration | null> => {
      try {
        setSaving(true);
        const response = (await post(API_END_POINTS.VOICE_SERVER_CREATE, {
          data,
        })) as IResponse<IVoiceServerConfiguration> | undefined;

        if (response?.data) {
          toast.success('Voice server configuration created successfully');
          return response.data;
        }
        toast.error(
          response?.message || 'Failed to create voice server configuration',
        );
        return null;
      } catch (error: unknown) {
        console.error('Error creating voice server configuration:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to create voice server configuration',
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const updateConfiguration = useCallback(
    async (
      id: string,
      data: IVoiceServerConfigurationForm,
    ): Promise<IVoiceServerConfiguration | null> => {
      try {
        setSaving(true);
        const response = (await put(API_END_POINTS.VOICE_SERVER_DETAILS(id), {
          data,
        })) as IResponse<IVoiceServerConfiguration> | undefined;

        if (response?.data) {
          toast.success('Voice server configuration updated successfully');
          setConfigurations(prev => ({
            ...prev,
            items: prev.items.map(item =>
              item.id === id ? response.data! : item,
            ),
          }));
          return response.data;
        }
        toast.error(
          response?.message || 'Failed to update voice server configuration',
        );
        return null;
      } catch (error: unknown) {
        console.error('Error updating voice server configuration:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to update voice server configuration',
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const deleteConfiguration = useCallback(
    async (id: string): Promise<boolean> => {
      try {
        setSaving(true);
        await del(API_END_POINTS.VOICE_SERVER_DETAILS(id));
        setConfigurations(prev => ({
          ...prev,
          items: prev.items.filter(item => item.id !== id),
          total: Math.max(0, prev.total - 1),
        }));
        toast.success('Voice server configuration deleted successfully');
        return true;
      } catch (error: unknown) {
        console.error('Error deleting voice server configuration:', error);
        const err = error as { response?: { data?: { message?: string } } };
        toast.error(
          err.response?.data?.message ||
            (error instanceof Error
              ? error.message
              : 'Failed to delete voice server configuration'),
        );
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  const setDefaultConfiguration = useCallback(
    async (id: string): Promise<IVoiceServerConfiguration | null> => {
      try {
        setSaving(true);
        const response = (await post(
          API_END_POINTS.VOICE_SERVER_SET_DEFAULT(id),
          {},
        )) as IResponse<IVoiceServerConfiguration> | undefined;

        if (response?.data) {
          toast.success('Default voice server updated');
          setConfigurations(prev => ({
            ...prev,
            items: prev.items.map(item => ({
              ...item,
              default: item.id === id,
            })),
          }));
          return response.data;
        }
        toast.error(response?.message || 'Failed to set default voice server');
        return null;
      } catch (error: unknown) {
        console.error('Error setting default voice server:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to set default voice server',
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const testConfiguration = useCallback(
    async (
      id: string,
      data: IVoiceServerTestRequest,
    ): Promise<ITestResult | null> => {
      try {
        setSaving(true);
        const response = (await post(API_END_POINTS.VOICE_SERVER_TEST(id), {
          data,
        })) as IResponse<ITestResult> | undefined;

        return (
          response?.data ?? {
            success: false,
            message:
              response?.message || 'Failed to test voice server configuration',
          }
        );
      } catch (error) {
        console.error('Error testing voice server configuration:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  return {
    configurations,
    loading,
    detailLoading,
    saving,
    fetchConfigurations,
    getDefaultConfiguration,
    getConfigurationById,
    createConfiguration,
    updateConfiguration,
    deleteConfiguration,
    setDefaultConfiguration,
    testConfiguration,
  };
};

export default useVoiceServerConfigurations;
