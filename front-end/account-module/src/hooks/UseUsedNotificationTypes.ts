import { useAPI } from 'hooks/UseAPI';
import { useCallback, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';

export const useUsedNotificationTypes = () => {
  const apiClient = useAPI();
  const [notificationTypes, setNotificationTypes] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchNotificationTypes = useCallback(async () => {
    try {
      setIsLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_USED_NOTIFICATION_TYPES,
      );
      setNotificationTypes(response.data ?? []);
    } catch (error) {
      console.error('Failed to fetch notification types:', error);
      setNotificationTypes([]);
    } finally {
      setIsLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    fetchNotificationTypes();
  }, [fetchNotificationTypes]);

  return { notificationTypes, isLoading };
};
