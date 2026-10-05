import { IList, IResponse } from 'models/Global';
import {
  ISmsServerConfiguration,
  ISmsServerConfigurationForm,
  ISmsServerConfigurationListParams,
} from 'models/SmsServerConfiguration';
import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

/**
 * Custom hook for SMS server configuration management
 */
export const useSmsServerConfigurations = () => {
  const { get, post, put, del } = useAPI();

  const [configurations, setConfigurations] = useState<
    IList<ISmsServerConfiguration>
  >({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [detailLoading, setDetailLoading] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);

  const fetchConfigurations = useCallback(
    async (params: ISmsServerConfigurationListParams): Promise<void> => {
      try {
        setLoading(true);
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.SMS_SERVER_CONFIGURATION_LIST}${queryString}`,
        );

        if (response) {
          setConfigurations({
            offset: response.offset || 0,
            pageSize: response.pageSize || 10,
            total: response.total || 0,
            items: response.items || [],
          });
        }
      } catch (error) {
        console.error('Error fetching SMS server configurations:', error);
        toast.error('Failed to load SMS server configurations');
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const getConfigurationById = useCallback(
    async (id: string): Promise<ISmsServerConfiguration | null> => {
      try {
        setDetailLoading(true);
        const response = (await get(
          API_END_POINTS.SMS_SERVER_CONFIGURATION_DETAILS(id),
        )) as IResponse<ISmsServerConfiguration> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching SMS server configuration:', error);
        toast.error('Failed to load SMS server configuration');
        return null;
      } finally {
        setDetailLoading(false);
      }
    },
    [get],
  );

  const createConfiguration = useCallback(
    async (
      data: ISmsServerConfigurationForm,
    ): Promise<ISmsServerConfiguration | null> => {
      try {
        setSaving(true);
        const response = (await post(
          API_END_POINTS.SMS_SERVER_CONFIGURATION_CREATE,
          { data },
        )) as IResponse<ISmsServerConfiguration> | undefined;

        if (response?.data) {
          toast.success('SMS server configuration created successfully');
          return response.data;
        }
        return null;
      } catch (error: unknown) {
        console.error('Error creating SMS server configuration:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to create SMS server configuration',
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
      data: ISmsServerConfigurationForm,
    ): Promise<ISmsServerConfiguration | null> => {
      try {
        setSaving(true);
        const response = (await put(
          API_END_POINTS.SMS_SERVER_CONFIGURATION_UPDATE(id),
          { data },
        )) as IResponse<ISmsServerConfiguration> | undefined;

        if (response?.data) {
          toast.success('SMS server configuration updated successfully');
          setConfigurations(prev => ({
            ...prev,
            items: prev.items.map(item =>
              item.id === id ? response.data! : item,
            ),
          }));
          return response.data;
        }
        return null;
      } catch (error: unknown) {
        console.error('Error updating SMS server configuration:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to update SMS server configuration',
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
        await del(API_END_POINTS.SMS_SERVER_CONFIGURATION_DELETE(id));
        setConfigurations(prev => ({
          ...prev,
          items: prev.items.filter(item => item.id !== id),
          total: Math.max(0, prev.total - 1),
        }));
        toast.success('SMS server configuration deleted successfully');
        return true;
      } catch (error: unknown) {
        console.error('Error deleting SMS server configuration:', error);
        const err = error as { response?: { data?: { message?: string } } };
        toast.error(
          err.response?.data?.message ||
            (error instanceof Error
              ? error.message
              : 'Failed to delete SMS server configuration'),
        );
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  return {
    configurations,
    loading,
    detailLoading,
    saving,
    fetchConfigurations,
    getConfigurationById,
    createConfiguration,
    updateConfiguration,
    deleteConfiguration,
  };
};

export default useSmsServerConfigurations;
