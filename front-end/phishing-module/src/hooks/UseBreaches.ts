import {
  BreachSeverity,
  BreachStatus,
  IBreachConfig,
  IBreachConfigRequest,
  IBreachRecord,
  IBreachStatusRequest,
  IBreachSyncResult,
  IRecipientActionRequest,
  IRecipientBreach,
  RecipientBreachStatus,
} from 'models/Breach';
import { IList, IResponse } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

// Filter parameters for breach list
export interface IBreachListParams {
  offset?: number;
  pageSize?: number;
  keyword?: string;
  domain?: string;
  status?: BreachStatus;
  severity?: BreachSeverity;
  startDate?: string;
  endDate?: string;
}

// Filter parameters for recipient breach list
export interface IRecipientBreachListParams {
  offset?: number;
  pageSize?: number;
  breachRecordId?: string;
  keyword?: string;
  status?: RecipientBreachStatus;
}

/**
 * Custom hook for Breach Detection API operations
 */
export const useBreaches = () => {
  const { get, post, put, delete: del } = useAPI();
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);

  // ==================== Breach Records ====================

  /**
   * Fetch list of breach records
   */
  const fetchBreaches = useCallback(
    async (
      params: IBreachListParams = {},
    ): Promise<IList<IBreachRecord> | null> => {
      setLoading(true);
      try {
        const queryParams = {
          offset: params.offset || 0,
          pageSize: params.pageSize || 10,
          keyword: params.keyword,
          domain: params.domain,
          status: params.status,
          severity: params.severity,
          startDate: params.startDate,
          endDate: params.endDate,
        };
        const queryString = objectToQueryString(queryParams);
        const response = (await get(
          `${API_END_POINTS.BREACH_LIST}${queryString ? `&${queryString}` : ''}`,
        )) as IResponse<IList<IBreachRecord>> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching breaches:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Fetch single breach record details
   */
  const fetchBreachById = useCallback(
    async (id: string): Promise<IBreachRecord | null> => {
      setLoading(true);
      try {
        const response = (await get(API_END_POINTS.BREACH_DETAILS(id))) as
          | IResponse<IBreachRecord>
          | undefined;
        return (response as IResponse<IBreachRecord>)?.data || null;
      } catch (error) {
        console.error('Error fetching breach details:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Update breach status
   */
  const updateBreachStatus = useCallback(
    async (
      id: string,
      request: IBreachStatusRequest,
    ): Promise<IBreachRecord | null> => {
      setActionLoading(true);
      try {
        const response = (await put(API_END_POINTS.BREACH_UPDATE_STATUS(id), {
          data: request,
        })) as IResponse<IBreachRecord> | undefined;
        return (response as IResponse<IBreachRecord>)?.data || null;
      } catch (error) {
        console.error('Error updating breach status:', error);
        return null;
      } finally {
        setActionLoading(false);
      }
    },
    [put],
  );

  /**
   * Delete breach record
   */
  const deleteBreach = useCallback(
    async (id: string): Promise<boolean> => {
      setActionLoading(true);
      try {
        await del(API_END_POINTS.BREACH_DELETE(id));
        return true;
      } catch (error) {
        console.error('Error deleting breach:', error);
        return false;
      } finally {
        setActionLoading(false);
      }
    },
    [del],
  );

  /**
   * Export breaches
   */
  const exportBreaches = useCallback(
    async (
      format: string = 'csv',
      domain?: string,
      status?: BreachStatus,
    ): Promise<Blob | null> => {
      setActionLoading(true);
      try {
        const queryString = objectToQueryString({ format, domain, status });
        const response = (await get(
          `${API_END_POINTS.BREACH_EXPORT}?${queryString}`,
          { responseType: 'blob' },
        )) as Blob | IResponse<Blob> | undefined;
        return (
          (response as IResponse<Blob>)?.data || (response as Blob) || null
        );
      } catch (error) {
        console.error('Error exporting breaches:', error);
        return null;
      } finally {
        setActionLoading(false);
      }
    },
    [get],
  );

  // ==================== Recipient Breaches ====================

  /**
   * Fetch list of recipient breaches
   */
  const fetchRecipientBreaches = useCallback(
    async (
      params: IRecipientBreachListParams = {},
    ): Promise<IList<IRecipientBreach> | null> => {
      setLoading(true);
      try {
        const queryParams = {
          offset: params.offset || 0,
          pageSize: params.pageSize || 20,
          breachRecordId: params.breachRecordId,
          keyword: params.keyword,
          status: params.status,
        };
        const queryString = objectToQueryString(queryParams);
        const response = (await get(
          `${API_END_POINTS.RECIPIENT_BREACH_LIST}${queryString ? `&${queryString}` : ''}`,
        )) as IResponse<IList<IRecipientBreach>> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching recipient breaches:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Fetch single recipient breach details
   */
  const fetchRecipientBreachById = useCallback(
    async (id: string): Promise<IRecipientBreach | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.RECIPIENT_BREACH_DETAILS(id),
        )) as IResponse<IRecipientBreach> | undefined;
        return (response as IResponse<IRecipientBreach>)?.data || null;
      } catch (error) {
        console.error('Error fetching recipient breach:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Notify recipient about breach
   */
  const notifyRecipient = useCallback(
    async (
      id: string,
      request?: IRecipientActionRequest,
    ): Promise<IRecipientBreach | null> => {
      setActionLoading(true);
      try {
        const response = (await post(
          API_END_POINTS.RECIPIENT_BREACH_NOTIFY(id),
          { data: request || {} },
        )) as IResponse<IRecipientBreach> | undefined;
        return (response as IResponse<IRecipientBreach>)?.data || null;
      } catch (error) {
        console.error('Error notifying recipient:', error);
        return null;
      } finally {
        setActionLoading(false);
      }
    },
    [post],
  );

  /**
   * Trigger password reset for recipient
   */
  const resetRecipientPassword = useCallback(
    async (
      id: string,
      request?: IRecipientActionRequest,
    ): Promise<IRecipientBreach | null> => {
      setActionLoading(true);
      try {
        const response = (await post(
          API_END_POINTS.RECIPIENT_BREACH_RESET(id),
          { data: request || {} },
        )) as IResponse<IRecipientBreach> | undefined;
        return (response as IResponse<IRecipientBreach>)?.data || null;
      } catch (error) {
        console.error('Error triggering password reset:', error);
        return null;
      } finally {
        setActionLoading(false);
      }
    },
    [post],
  );

  /**
   * Resolve recipient breach
   */
  const resolveRecipientBreach = useCallback(
    async (
      id: string,
      request?: IRecipientActionRequest,
    ): Promise<IRecipientBreach | null> => {
      setActionLoading(true);
      try {
        const response = (await put(
          API_END_POINTS.RECIPIENT_BREACH_RESOLVE(id),
          { data: request || {} },
        )) as IResponse<IRecipientBreach> | undefined;
        return (response as IResponse<IRecipientBreach>)?.data || null;
      } catch (error) {
        console.error('Error resolving recipient breach:', error);
        return null;
      } finally {
        setActionLoading(false);
      }
    },
    [put],
  );

  // ==================== Configuration ====================

  /**
   * Fetch breach detection configuration
   */
  const fetchConfig = useCallback(async (): Promise<IBreachConfig | null> => {
    setLoading(true);
    try {
      const response = (await get(API_END_POINTS.BREACH_CONFIG_GET)) as
        | IResponse<IBreachConfig>
        | undefined;
      return (response as IResponse<IBreachConfig>)?.data || null;
    } catch (error) {
      console.error('Error fetching breach config:', error);
      return null;
    } finally {
      setLoading(false);
    }
  }, [get]);

  /**
   * Update breach detection configuration
   */
  const updateConfig = useCallback(
    async (request: IBreachConfigRequest): Promise<IBreachConfig | null> => {
      setActionLoading(true);
      try {
        const response = (await post(API_END_POINTS.BREACH_CONFIG_UPDATE, {
          data: request,
        })) as IResponse<IBreachConfig> | undefined;
        return (response as IResponse<IBreachConfig>)?.data || null;
      } catch (error) {
        console.error('Error updating breach config:', error);
        return null;
      } finally {
        setActionLoading(false);
      }
    },
    [post],
  );

  /**
   * Trigger manual breach sync
   */
  const triggerSync =
    useCallback(async (): Promise<IBreachSyncResult | null> => {
      setSyncing(true);
      try {
        const response = await post(API_END_POINTS.BREACH_SYNC, {});
        const typedResponse = response as
          | IResponse<IBreachSyncResult>
          | undefined;
        return typedResponse?.data || null;
      } catch (error) {
        console.error('Error triggering breach sync:', error);
        return null;
      } finally {
        setSyncing(false);
      }
    }, [post]);

  // ==================== Utility Functions ====================

  /**
   * Download export file
   */
  const downloadExport = useCallback((blob: Blob, filename: string) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }, []);

  return {
    // State
    loading,
    actionLoading,
    syncing,
    // Breach Records
    fetchBreaches,
    fetchBreachById,
    updateBreachStatus,
    deleteBreach,
    exportBreaches,
    // Recipient Breaches
    fetchRecipientBreaches,
    fetchRecipientBreachById,
    notifyRecipient,
    resetRecipientPassword,
    resolveRecipientBreach,
    // Configuration
    fetchConfig,
    updateConfig,
    triggerSync,
    // Utilities
    downloadExport,
  };
};

export default useBreaches;
