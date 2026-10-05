import { IList, IResponse } from 'models/Global';
import {
  ISenderProfile,
  ISenderProfileForm,
  ISenderProfileImportData,
  ISenderProfileListParams,
  ITestResult,
} from 'models/SenderProfile';
import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

/**
 * Custom hook for sender profile management
 * Based on Task-06 Sender Profile Management
 */
export const useSenderProfiles = () => {
  const { get, put, post, del } = useAPI();

  const [profiles, setProfiles] = useState<IList<ISenderProfile>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [saving, setSaving] = useState<boolean>(false);
  const [testing, setTesting] = useState<boolean>(false);
  const [importing, setImporting] = useState<boolean>(false);
  const [totalCount, setTotalCount] = useState<number>(0);

  /**
   * Fetch sender profiles with pagination and filters
   */
  const fetchProfiles = useCallback(
    async (params: ISenderProfileListParams): Promise<void> => {
      try {
        setLoading(true);
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.SENDER_PROFILE_LIST}${queryString}`,
        );

        if (response) {
          setProfiles({
            offset: response.offset || 0,
            pageSize: response.pageSize || 10,
            total: response.total || 0,
            items: response.items || [],
          });
          setTotalCount(response.total || 0);
        }
      } catch (error) {
        console.error('Error fetching sender profiles:', error);
        toast.error('Failed to load sender profiles');
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Get sender profile by ID
   */
  const getProfileById = useCallback(
    async (profileId: string): Promise<ISenderProfile | null> => {
      try {
        setLoading(true);
        const response = (await get(
          API_END_POINTS.SENDER_PROFILE_DETAILS(profileId),
        )) as IResponse<ISenderProfile> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching sender profile:', error);
        toast.error('Failed to load sender profile');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Create a new sender profile
   */
  const createProfile = useCallback(
    async (data: ISenderProfileForm): Promise<ISenderProfile | null> => {
      try {
        setSaving(true);
        const response = (await post(API_END_POINTS.SENDER_PROFILE_CREATE, {
          data,
        })) as IResponse<ISenderProfile> | undefined;

        if (response?.data) {
          toast.success('Sender profile created successfully');
          return response.data;
        }
        return null;
      } catch (error: unknown) {
        console.error('Error creating sender profile:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to create sender profile',
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  /**
   * Update an existing sender profile
   */
  const updateProfile = useCallback(
    async (
      profileId: string,
      data: ISenderProfileForm,
    ): Promise<ISenderProfile | null> => {
      try {
        setSaving(true);
        const response = (await put(
          API_END_POINTS.SENDER_PROFILE_UPDATE(profileId),
          { data },
        )) as IResponse<ISenderProfile> | undefined;

        if (response?.data) {
          toast.success('Sender profile updated successfully');
          setProfiles(prev => ({
            ...prev,
            items: prev.items.map(p =>
              p.profileId === profileId ? response.data! : p,
            ),
          }));
          return response.data;
        }
        return null;
      } catch (error: unknown) {
        console.error('Error updating sender profile:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to update sender profile',
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  /**
   * Delete a sender profile
   */
  const deleteProfile = useCallback(
    async (profileId: string): Promise<boolean> => {
      try {
        setSaving(true);
        await del(API_END_POINTS.SENDER_PROFILE_DELETE(profileId));
        setProfiles(prev => ({
          ...prev,
          items: prev.items.filter(p => p.profileId !== profileId),
        }));
        setTotalCount(prev => Math.max(0, prev - 1));
        toast.success('Sender profile deleted successfully');
        return true;
      } catch (error: unknown) {
        console.error('Error deleting sender profile:', error);
        const err = error as { response?: { data?: { message?: string } } };
        toast.error(
          err.response?.data?.message ||
            (error instanceof Error
              ? error.message
              : 'Failed to delete sender profile'),
        );
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  /**
   * Duplicate a sender profile
   */
  const duplicateProfile = useCallback(
    async (profileId: string): Promise<ISenderProfile | null> => {
      try {
        setSaving(true);
        const response = (await post(
          API_END_POINTS.SENDER_PROFILE_DUPLICATE(profileId),
          {},
        )) as IResponse<ISenderProfile> | undefined;

        if (response?.data) {
          toast.success('Sender profile duplicated successfully');
          setProfiles(prev => ({
            ...prev,
            items: [response.data!, ...prev.items],
          }));
          setTotalCount(prev => prev + 1);
          return response.data;
        }
        return null;
      } catch (error: unknown) {
        console.error('Error duplicating sender profile:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to duplicate sender profile',
        );
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  /**
   * Test connection for an existing profile
   */
  const testProfileConnection = useCallback(
    async (profileId: string): Promise<ITestResult | null> => {
      try {
        setTesting(true);
        const response = (await post(
          API_END_POINTS.SENDER_PROFILE_TEST(profileId),
          {},
        )) as IResponse<ITestResult> | undefined;

        if (response?.data) {
          // Update profile verification status in local state
          setProfiles(prev => ({
            ...prev,
            items: prev.items.map(p =>
              p.profileId === profileId
                ? {
                    ...p,
                    verified: response.data!.success,
                    lastTestedAt: new Date().toISOString(),
                    lastTestResult: response.data!.success
                      ? 'SUCCESS'
                      : `FAILED: ${response.data!.message}`,
                  }
                : p,
            ),
          }));
          return response.data;
        }
        return null;
      } catch (error: unknown) {
        console.error('Error testing sender profile:', error);
        return {
          success: false,
          message:
            error instanceof Error ? error.message : 'Connection test failed',
          responseTimeMs: 0,
          serverResponse: null,
        };
      } finally {
        setTesting(false);
      }
    },
    [post],
  );

  /**
   * Test connection with new configuration (before saving)
   */
  const testNewConnection = useCallback(
    async (data: ISenderProfileForm): Promise<ITestResult | null> => {
      try {
        setTesting(true);
        const response = (await post(API_END_POINTS.SENDER_PROFILE_TEST_NEW, {
          data,
        })) as IResponse<ITestResult> | undefined;

        return response?.data || null;
      } catch (error: unknown) {
        console.error('Error testing new connection:', error);
        return {
          success: false,
          message:
            error instanceof Error ? error.message : 'Connection test failed',
          responseTimeMs: 0,
          serverResponse: null,
        };
      } finally {
        setTesting(false);
      }
    },
    [post],
  );

  /**
   * Check if domain is verified
   */
  const checkDomainVerification = useCallback(
    async (email: string): Promise<boolean> => {
      try {
        const response = (await get(
          `${API_END_POINTS.SENDER_PROFILE_CHECK_DOMAIN}?email=${encodeURIComponent(email)}`,
        )) as IResponse<boolean> | undefined;
        return response?.data ?? false;
      } catch (error) {
        console.error('Error checking domain verification:', error);
        return false;
      }
    },
    [get],
  );

  /**
   * Get verified profiles for campaign selection
   */
  const getVerifiedProfiles = useCallback(async (): Promise<
    ISenderProfile[]
  > => {
    try {
      const response = (await get(API_END_POINTS.SENDER_PROFILE_VERIFIED)) as
        | IResponse<ISenderProfile[]>
        | undefined;
      return response?.data || [];
    } catch (error) {
      console.error('Error fetching verified profiles:', error);
      return [];
    }
  }, [get]);

  /**
   * Bulk import sender profiles from a spreadsheet file (multipart field `file`).
   */
  const importSenderProfiles = useCallback(
    async (file: File): Promise<ISenderProfileImportData | null> => {
      try {
        setImporting(true);
        const formData = new FormData();
        formData.append('file', file);

        const response = await post(API_END_POINTS.SENDER_PROFILE_IMPORT, {
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
        });

        if (!isSuccessResponse(response.statusCode) || !response.data) {
          toast.error(response.message || 'Failed to import sender profiles');
          return null;
        }

        const {
          totalRows,
          successCount,
          failedCount,
          errors = [],
        } = response.data;

        if (successCount === 0 && failedCount === 0 && totalRows === 0) {
          toast.warning(
            `No profiles imported. Please check the file and try again.`,
          );
        } else if (totalRows > 0 && successCount === 0) {
          toast.warning(`All profiles already exist.`);
        } else {
          toast.success(`Successfully imported ${successCount} profiles.`);
        }

        if (errors.length > 0) {
          console.warn('Sender profile import row errors:', errors);
        }

        return response.data;
      } catch (error: unknown) {
        console.error('Error importing sender profiles:', error);
        toast.error(
          error instanceof Error
            ? error.message
            : 'Failed to import sender profiles',
        );
        return null;
      } finally {
        setImporting(false);
      }
    },
    [post],
  );

  return {
    profiles,
    loading,
    saving,
    testing,
    importing,
    totalCount,
    fetchProfiles,
    getProfileById,
    createProfile,
    updateProfile,
    deleteProfile,
    duplicateProfile,
    testProfileConnection,
    testNewConnection,
    checkDomainVerification,
    getVerifiedProfiles,
    importSenderProfiles,
  };
};

export default useSenderProfiles;
