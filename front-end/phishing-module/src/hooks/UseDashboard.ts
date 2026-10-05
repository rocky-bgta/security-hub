import {
  IBreachSummary,
  ICampaignPerformance,
  IDashboardKpi,
  IDashboardOverview,
  IDashboardRiskImpact,
  IEmailStats,
  IPhishProneDataPoint,
  IInsecureWebFindingsSummary,
  ITrendData,
  ITrendDataPoint,
  IUserRiskDistribution,
} from 'models/Dashboard';
import { CampaignChannel } from 'models/Campaign';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from './UseAPI';
import { objectToQueryString } from 'utils/Helper';

/**
 * Custom hook for dashboard operations
 */
export const useDashboard = (channel?: CampaignChannel) => {
  const { get, post } = useAPI();
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const dashboardQuery = useCallback(
    (params: IGetListParams = {}) =>
      objectToQueryString({
        ...params,
        ...(channel ? { channel } : {}),
      }),
    [channel],
  );

  /**
   * Get dashboard phish-prone data
   */
  const fetchPhishProneData = useCallback(
    async (days: string = '30'): Promise<IPhishProneDataPoint | null> => {
      setLoading(true);
      try {
        const response = (await get(
          `${API_END_POINTS.DASHBOARD_PHISH_PRONE_DATA}${dashboardQuery({ days })}`,
        )) as IResponse<IPhishProneDataPoint>;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching dashboard phish-prone data:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get, dashboardQuery],
  );

  /**
   * Get dashboard overview with all key metrics
   */
  const fetchOverview =
    useCallback(async (): Promise<IDashboardOverview | null> => {
      setLoading(true);
      try {
        const queryString = dashboardQuery();
        const response = (await get(
          queryString
            ? `${API_END_POINTS.DASHBOARD_OVERVIEW}?${queryString}`
            : API_END_POINTS.DASHBOARD_OVERVIEW,
        )) as IResponse<IDashboardOverview> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching dashboard overview:', error);
        return null;
      } finally {
        setLoading(false);
      }
    }, [get, dashboardQuery]);

  /**
   * Get campaign risk impact with all key metrics
   */
  const fetchRiskImpact = useCallback(
    async (
      params: IGetListParams,
    ): Promise<IList<IDashboardRiskImpact> | null> => {
      setLoading(true);

      try {
        const queryString = dashboardQuery(params);
        const response = (await get(
          `${API_END_POINTS.DASHBOARD_RISK_IMPACT}?${queryString}`,
        )) as IList<IDashboardRiskImpact> | undefined;
        return response || null;
      } catch (error) {
        console.error('Error fetching campaign risk impact:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get, dashboardQuery],
  );

  /**
   * Get 7 core KPI metrics
   */
  const fetchKpiMetrics = useCallback(
    async (days: string = '30'): Promise<IDashboardKpi | null> => {
      try {
        const response = (await get(
          `${API_END_POINTS.DASHBOARD_KPI}${dashboardQuery({ days })}`,
        )) as IResponse<IDashboardKpi> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching KPI metrics:', error);
        return null;
      }
    },
    [get, dashboardQuery],
  );

  /**
   * Get campaign performance summary
   */
  const fetchCampaignPerformance = useCallback(
    async (limit: number = 10): Promise<ICampaignPerformance[]> => {
      try {
        const response = (await get(
          `${API_END_POINTS.DASHBOARD_CAMPAIGNS}${dashboardQuery({ limit })}`,
        )) as IResponse<ICampaignPerformance[]> | undefined;
        return response?.data || [];
      } catch (error) {
        console.error('Error fetching campaign performance:', error);
        return [];
      }
    },
    [get, dashboardQuery],
  );

  /**
   * Get email statistics
   */
  const fetchEmailStats = useCallback(async (): Promise<IEmailStats | null> => {
    try {
      const queryString = dashboardQuery();
      const response = (await get(
        queryString
          ? `${API_END_POINTS.DASHBOARD_EMAIL_STATS}?${queryString}`
          : API_END_POINTS.DASHBOARD_EMAIL_STATS,
      )) as IResponse<IEmailStats> | undefined;
      return response?.data || null;
    } catch (error) {
      console.error('Error fetching email stats:', error);
      return null;
    }
  }, [get, dashboardQuery]);

  /**
   * Get user risk distribution
   */
  const fetchUserRiskDistribution =
    useCallback(async (): Promise<IUserRiskDistribution | null> => {
      try {
        const queryString = dashboardQuery();
        const response = (await get(
          queryString
            ? `${API_END_POINTS.DASHBOARD_USER_RISK}?${queryString}`
            : API_END_POINTS.DASHBOARD_USER_RISK,
        )) as IResponse<IUserRiskDistribution> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching user risk distribution:', error);
        return null;
      }
    }, [get, dashboardQuery]);

  /**
   * Get historical trends
   */
  const fetchTrends = useCallback(
    async (
      metricType: string = 'openRate',
      days: number = 30,
    ): Promise<ITrendDataPoint[]> => {
      try {
        const response = (await get(
          `${API_END_POINTS.DASHBOARD_TRENDS}metricType=${metricType}&days=${days}`,
        )) as IResponse<ITrendDataPoint[]> | undefined;
        return response?.data || [];
      } catch (error) {
        console.error('Error fetching trends:', error);
        return [];
      }
    },
    [get],
  );

  /**
   * Get historical trends
   */
  const fetchAllTrends = useCallback(
    async (days: string = '30'): Promise<ITrendData | null> => {
      try {
        const response = (await get(
          `${API_END_POINTS.DASHBOARD_ALL_TRENDS}${dashboardQuery({ days })}`,
        )) as IResponse<ITrendData> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching trends:', error);
        return null;
      }
    },
    [get, dashboardQuery],
  );

  /**
   * Get breach summary
   */
  const fetchBreachSummary =
    useCallback(async (): Promise<IBreachSummary | null> => {
      try {
        const response = (await get(
          API_END_POINTS.DASHBOARD_BREACH_SUMMARY,
        )) as IResponse<IBreachSummary> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching breach summary:', error);
        return null;
      }
    }, [get]);

  /**
   * Get insecure web findings summary
   */
  const fetchInsecureWebFindingsSummary =
    useCallback(async (): Promise<IInsecureWebFindingsSummary | null> => {
      try {
        const response = (await get(
          API_END_POINTS.INSECURE_WEB_FINDINGS_SUMMARY,
        )) as IResponse<IInsecureWebFindingsSummary> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching insecure web findings summary:', error);
        return null;
      }
    }, [get]);

  /**
   * Refresh dashboard statistics
   */
  const refreshStats = useCallback(async (): Promise<boolean> => {
    setRefreshing(true);
    try {
      await post(API_END_POINTS.DASHBOARD_REFRESH, {});
      return true;
    } catch (error) {
      console.error('Error refreshing stats:', error);
      return false;
    } finally {
      setRefreshing(false);
    }
  }, [post]);

  /**
   * Get phish-prone percentage
   */
  const fetchPhishPronePercentage = useCallback(async (): Promise<number> => {
    try {
      const response = (await get(API_END_POINTS.ANALYTICS_PHISH_PRONE)) as
        | IResponse<number>
        | undefined;
      return response?.data || 0;
    } catch (error) {
      console.error('Error fetching phish-prone percentage:', error);
      return 0;
    }
  }, [get]);

  /**
   * Get delivery rate
   */
  const fetchDeliveryRate = useCallback(async (): Promise<number> => {
    try {
      const response = (await get(API_END_POINTS.ANALYTICS_DELIVERY_RATE)) as
        | IResponse<number>
        | undefined;
      return response?.data || 0;
    } catch (error) {
      console.error('Error fetching delivery rate:', error);
      return 0;
    }
  }, [get]);

  /**
   * Get compromise rate
   */
  const fetchCompromiseRate = useCallback(async (): Promise<number> => {
    try {
      const response = (await get(API_END_POINTS.ANALYTICS_COMPROMISE_RATE)) as
        | IResponse<number>
        | undefined;
      return response?.data || 0;
    } catch (error) {
      console.error('Error fetching compromise rate:', error);
      return 0;
    }
  }, [get]);

  /**
   * Get report rate
   */
  const fetchReportRate = useCallback(async (): Promise<number> => {
    try {
      const response = (await get(API_END_POINTS.ANALYTICS_REPORT_RATE)) as
        | IResponse<number>
        | undefined;
      return response?.data || 0;
    } catch (error) {
      console.error('Error fetching report rate:', error);
      return 0;
    }
  }, [get]);

  return {
    loading,
    refreshing,
    fetchPhishProneData,
    fetchOverview,
    fetchRiskImpact,
    fetchKpiMetrics,
    fetchCampaignPerformance,
    fetchEmailStats,
    fetchUserRiskDistribution,
    fetchTrends,
    fetchBreachSummary,
    fetchInsecureWebFindingsSummary,
    refreshStats,
    fetchPhishPronePercentage,
    fetchDeliveryRate,
    fetchCompromiseRate,
    fetchReportRate,
    fetchAllTrends,
  };
};
