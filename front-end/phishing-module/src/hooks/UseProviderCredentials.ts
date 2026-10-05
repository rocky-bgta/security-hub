import { useCallback, useState } from 'react';

import type { IList, IResponse } from 'models/Global';
import type {
  IProviderCredential,
  IProviderCredentialCreateRequest,
  IProviderCredentialListParams,
  IProviderCredentialUpdateRequest,
  IVideoRenderProvider,
} from 'models/ProviderCredential';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { useAPI } from './UseAPI';

export interface IProviderCredentialActionResult {
  ok: boolean;
  message: string;
  data?: IProviderCredential | null;
}

export interface IProviderCredentialListResult {
  ok: boolean;
  message: string;
  data: IList<IProviderCredential>;
}

const emptyList = (
  params: IProviderCredentialListParams = {},
): IList<IProviderCredential> => ({
  items: [],
  total: 0,
  offset: params.offset ?? 0,
  pageSize: params.pageSize ?? 10,
});

const toQueryString = (
  params: Record<string, string | number | boolean | undefined>,
): string =>
  Object.entries(params)
    .filter(
      ([, value]) => value !== undefined && value !== null && value !== '',
    )
    .map(
      ([key, value]) =>
        `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`,
    )
    .join('&');

/**
 * CRUD for deepfake third-party provider credentials.
 */
const useProviderCredentials = () => {
  const { get, post, put, patch, del } = useAPI();
  const [isLoadingList, setIsLoadingList] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const getCredentialList = useCallback(
    async (
      params: IProviderCredentialListParams = {},
      options?: { silent?: boolean },
    ): Promise<IProviderCredentialListResult> => {
      if (!options?.silent) {
        setIsLoadingList(true);
      }
      try {
        const query = toQueryString({
          providerName: params.providerName,
          isActive: params.isActive,
          offset: params.offset ?? 0,
          pageSize: params.pageSize ?? 10,
          sortBy: params.sortBy ?? 'createdAt',
          sortOrder: params.sortOrder ?? 'desc',
        });
        const response = (await get(
          `${API_END_POINTS.PROVIDER_CREDENTIALS_LIST}${query}`,
        )) as IResponse<IList<IProviderCredential>> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response?.message || 'Failed to load provider credentials',
            data: emptyList(params),
          };
        }

        return {
          ok: true,
          message: response.message || 'Provider credentials loaded',
          data: response.data ?? emptyList(params),
        };
      } catch (error) {
        console.error('Error loading provider credentials:', error);
        return {
          ok: false,
          message: 'Failed to load provider credentials',
          data: emptyList(params),
        };
      } finally {
        if (!options?.silent) {
          setIsLoadingList(false);
        }
      }
    },
    [get],
  );

  const createCredential = useCallback(
    async (
      payload: IProviderCredentialCreateRequest,
    ): Promise<IProviderCredentialActionResult> => {
      setIsSubmitting(true);
      try {
        const response = (await post(API_END_POINTS.PROVIDER_CREDENTIALS, {
          data: payload,
        })) as IResponse<IProviderCredential> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message:
              response?.message || 'Failed to create provider credential',
          };
        }

        return {
          ok: true,
          message: response.message || 'Provider credential created',
          data: response.data,
        };
      } catch (error) {
        console.error('Error creating provider credential:', error);
        return {
          ok: false,
          message: 'Failed to create provider credential',
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [post],
  );

  const updateCredential = useCallback(
    async (
      id: string,
      payload: IProviderCredentialUpdateRequest,
    ): Promise<IProviderCredentialActionResult> => {
      setIsSubmitting(true);
      try {
        const response = (await put(
          API_END_POINTS.PROVIDER_CREDENTIAL_DETAILS(id),
          { data: payload },
        )) as IResponse<IProviderCredential> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message:
              response?.message || 'Failed to update provider credential',
          };
        }

        return {
          ok: true,
          message: response.message || 'Provider credential updated',
          data: response.data,
        };
      } catch (error) {
        console.error('Error updating provider credential:', error);
        return {
          ok: false,
          message: 'Failed to update provider credential',
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [put],
  );

  const deleteCredential = useCallback(
    async (id: string): Promise<IProviderCredentialActionResult> => {
      setIsSubmitting(true);
      try {
        const response = (await del(
          API_END_POINTS.PROVIDER_CREDENTIAL_DETAILS(id),
        )) as IResponse<null> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message:
              response?.message || 'Failed to delete provider credential',
          };
        }

        return {
          ok: true,
          message: response.message || 'Provider credential deleted',
        };
      } catch (error) {
        console.error('Error deleting provider credential:', error);
        return {
          ok: false,
          message: 'Failed to delete provider credential',
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [del],
  );

  const updateCredentialStatus = useCallback(
    async (
      id: string,
      active: boolean,
    ): Promise<IProviderCredentialActionResult> => {
      setIsSubmitting(true);
      try {
        const response = (await patch(
          `${API_END_POINTS.PROVIDER_CREDENTIAL_STATUS(id)}?active=${active}`,
        )) as IResponse<IProviderCredential> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message:
              response?.message || 'Failed to update provider credential status',
          };
        }

        return {
          ok: true,
          message: response.message || 'Provider credential status updated',
          data: response.data,
        };
      } catch (error) {
        console.error('Error updating provider credential status:', error);
        return {
          ok: false,
          message: 'Failed to update provider credential status',
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [patch],
  );

  const getVideoRenderProviders = useCallback(async (): Promise<{
    ok: boolean;
    message: string;
    data: IVideoRenderProvider[];
  }> => {
    try {
      const response = (await get(
        API_END_POINTS.DEEPFAKE_VIDEO_RENDER_PROVIDERS,
      )) as IResponse<IVideoRenderProvider[]> | undefined;

      if (!response || !isSuccessResponse(response.statusCode)) {
        return {
          ok: false,
          message:
            response?.message || 'Failed to load video render providers',
          data: [],
        };
      }

      return {
        ok: true,
        message: response.message || 'Video render providers loaded',
        data: response.data ?? [],
      };
    } catch (error) {
      console.error('Error loading video render providers:', error);
      return {
        ok: false,
        message: 'Failed to load video render providers',
        data: [],
      };
    }
  }, [get]);

  const setCredentialDefault = useCallback(
    async (id: string): Promise<IProviderCredentialActionResult> => {
      setIsSubmitting(true);
      try {
        const response = (await patch(
          API_END_POINTS.PROVIDER_CREDENTIAL_DEFAULT(id),
        )) as IResponse<IProviderCredential> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message:
              response?.message ||
              'Failed to set provider credential as default',
          };
        }

        return {
          ok: true,
          message: response.message || 'Provider credential set as default',
          data: response.data,
        };
      } catch (error) {
        console.error('Error setting provider credential as default:', error);
        return {
          ok: false,
          message: 'Failed to set provider credential as default',
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [patch],
  );

  return {
    isLoadingList,
    isSubmitting,
    getCredentialList,
    createCredential,
    updateCredential,
    deleteCredential,
    updateCredentialStatus,
    setCredentialDefault,
    getVideoRenderProviders,
  };
};

export default useProviderCredentials;
