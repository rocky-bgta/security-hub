import {
  IPhishingCourseDetails,
  IPhishingCourseStatistics,
} from 'models/Dashboard';
import { CampaignChannel } from 'models/Campaign';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from './UseAPI';
import { objectToQueryString } from 'utils/Helper';

/**
 * Custom hook for course statistics operations
 */
export const useCourseStatistics = (channel?: CampaignChannel) => {
  const { get } = useAPI();
  const [loading, setLoading] = useState(false);

  /**
   * Get phishing course statistics
   */
  const fetchPhishingCourseStatistics =
    useCallback(async (): Promise<IPhishingCourseStatistics | null> => {
      try {
        const queryString = objectToQueryString({
          ...(channel ? { channel } : {}),
        });
        const response = (await get(
          queryString
            ? `${API_END_POINTS.GET_PHISHING_COURSE_STATISTICS}?${queryString}`
            : API_END_POINTS.GET_PHISHING_COURSE_STATISTICS,
        )) as IResponse<IPhishingCourseStatistics> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching phishing course statistics:', error);
        return null;
      }
    }, [get, channel]);

  /**
   * Get phishing course details with all key metrics
   */
  const fetchPhishingCourseDetails = useCallback(
    async (
      params: IGetListParams,
    ): Promise<IList<IPhishingCourseDetails> | null> => {
      setLoading(true);

      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = (await get(
          `${API_END_POINTS.GET_PHISHING_COURSE_DETAILS}${queryString}`,
        )) as IResponse<IList<IPhishingCourseDetails>> | undefined;

        return response?.data || null;
      } catch (error) {
        console.error('Error fetching phishing course details:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  return {
    loading,
    fetchPhishingCourseStatistics,
    fetchPhishingCourseDetails,
  };
};
