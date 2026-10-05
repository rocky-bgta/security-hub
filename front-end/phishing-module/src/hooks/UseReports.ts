import { CampaignChannel } from 'models/Campaign';
import {
  ActivityType,
  IBreachSummary,
  ICampaignPerformance,
  IEmailActivity,
  IUserRiskSummary,
  RiskLevel,
} from 'models/Dashboard';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

interface IEmailActivityParams extends IGetListParams {
  activityType?: ActivityType;
  startTime?: string;
  endTime?: string;
}

interface IUserRiskParams extends IGetListParams {
  riskLevel?: RiskLevel;
  department?: string;
  sortBy?: string;
}

/**
 * Custom hook for report operations
 */
export const useReports = (channel?: CampaignChannel) => {
  const { get } = useAPI();
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  const reportsQuery = useCallback(
    (params: IGetListParams = {}) =>
      objectToQueryString({
        ...params,
        ...(channel ? { channel } : {}),
      }),
    [channel],
  );

  /**
   * Fetch campaign reports list
   */
  const fetchCampaignReports = useCallback(
    async (
      params: IGetListParams,
    ): Promise<{ data: ICampaignPerformance[]; totalCount: number }> => {
      setLoading(true);
      try {
        const queryString = reportsQuery(params);
        const response = await get(
          `${API_END_POINTS.REPORTS_CAMPAIGNS}${queryString}`,
        );

        return {
          data: response?.items || [],
          totalCount: response?.total || 0,
        };
      } catch (error) {
        console.error('Error fetching campaign reports:', error);
        return { data: [], totalCount: 0 };
      } finally {
        setLoading(false);
      }
    },
    [get, reportsQuery],
  );

  /**
   * Fetch detailed campaign report
   */
  const fetchCampaignReportById = useCallback(
    async (campaignId: string): Promise<ICampaignPerformance | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.REPORTS_CAMPAIGN_DETAIL(campaignId),
        )) as IResponse<ICampaignPerformance> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching campaign report:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Export campaign report
   */
  const exportCampaignReport = useCallback(
    async (
      campaignId: string,
      format: 'pdf' | 'excel' | 'csv' = 'csv',
    ): Promise<Blob | null> => {
      setExporting(true);
      try {
        const response = (await get(
          API_END_POINTS.REPORTS_CAMPAIGN_EXPORT(campaignId, format),
        )) as IResponse<Blob> | undefined;
        if (!response?.data) throw new Error('Export failed');
        return response.data;
      } catch (error) {
        console.error('Error exporting campaign report:', error);
        return null;
      } finally {
        setExporting(false);
      }
    },
    [],
  );

  /**
   * Fetch email activity log
   */
  const fetchEmailActivity = useCallback(
    async (
      params: IEmailActivityParams,
    ): Promise<{ data: IEmailActivity[]; totalCount: number }> => {
      setLoading(true);
      try {
        const queryString = reportsQuery(params);
        const response = await get(
          `${API_END_POINTS.REPORTS_EMAIL_ACTIVITY}${queryString}`,
        );
        return {
          data: response?.items || [],
          totalCount: response?.total || 0,
        };
      } catch (error) {
        console.error('Error fetching email activity:', error);
        return { data: [], totalCount: 0 };
      } finally {
        setLoading(false);
      }
    },
    [get, reportsQuery],
  );

  /**
   * Fetch user risk report
   */
  const fetchUserRiskReport = useCallback(
    async (
      params: IUserRiskParams,
    ): Promise<{ data: IUserRiskSummary[]; totalCount: number }> => {
      setLoading(true);
      try {
        const queryString = reportsQuery(params);
        const response = await get(
          `${API_END_POINTS.REPORTS_USER_RISK}${queryString}`,
        );
        return {
          data: response?.items || [],
          totalCount: response?.total || 0,
        };
      } catch (error) {
        console.error('Error fetching user risk report:', error);
        return { data: [], totalCount: 0 };
      } finally {
        setLoading(false);
      }
    },
    [get, reportsQuery],
  );

  /**
   * Fetch repeat offenders
   */
  const fetchRepeatOffenders = useCallback(
    async (limit: number = 20): Promise<IUserRiskSummary[]> => {
      try {
        const response = (await get(
          `${API_END_POINTS.ANALYTICS_REPEAT_OFFENDERS}${reportsQuery({ limit })}`,
        )) as IResponse<IList<IUserRiskSummary>> | undefined;
        return response?.data?.items || [];
      } catch (error) {
        console.error('Error fetching repeat offenders:', error);
        return [];
      }
    },
    [get, reportsQuery],
  );

  /**
   * Fetch user risk summary by ID
   */
  const fetchUserRiskSummary = useCallback(
    async (userId: string): Promise<IUserRiskSummary | null> => {
      try {
        const response = (await get(
          API_END_POINTS.ANALYTICS_USER_RISK(userId),
        )) as IResponse<IUserRiskSummary> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching user risk summary:', error);
        return null;
      }
    },
    [get],
  );

  /**
   * Export user risk report
   */
  const exportUserRiskReport = useCallback(
    async (format: 'pdf' | 'excel' | 'csv' = 'csv'): Promise<Blob | null> => {
      setExporting(true);
      try {
        const queryString = reportsQuery({ format });
        const response = await get(
          `${API_END_POINTS.REPORTS_USER_RISK_EXPORT}${queryString}`,
          { responseType: 'blob' },
        );
        if (!response) throw new Error('Export failed');
        return new Blob([response], { type: `application/${format}` });
      } catch (error) {
        console.error('Error exporting user risk report:', error);
        return null;
      } finally {
        setExporting(false);
      }
    },
    [get, reportsQuery],
  );

  /**
   * Fetch breach summary
   */
  const fetchBreachSummary =
    useCallback(async (): Promise<IBreachSummary | null> => {
      try {
        const response = (await get(API_END_POINTS.REPORTS_BREACH_SUMMARY)) as
          | IResponse<IBreachSummary>
          | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching breach summary:', error);
        return null;
      }
    }, [get]);

  /**
   * Download exported file
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
    loading,
    exporting,
    fetchCampaignReports,
    fetchCampaignReportById,
    exportCampaignReport,
    fetchEmailActivity,
    fetchUserRiskReport,
    fetchRepeatOffenders,
    fetchUserRiskSummary,
    exportUserRiskReport,
    fetchBreachSummary,
    downloadExport,
  };
};
