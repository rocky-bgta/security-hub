import { IDomain, IDomainListResponse } from 'models/Domain';
import { IGetListParams, InitGetListParams } from 'models/Global';
import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  TDomainVerificationForm,
  TGenerateVerificationForm,
} from 'schemas/DomainSchema';
import { TOAST_MESSAGES } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

interface UseDomainReturn {
  domains: IDomainListResponse;
  loading: boolean;
  queryParams: IGetListParams;
  setQueryParams: React.Dispatch<React.SetStateAction<IGetListParams>>;
  fetchDomains: () => Promise<void>;
  generateVerificationEmail: (
    data: TGenerateVerificationForm,
  ) => Promise<boolean>;
  addDomain: (data: { domain: string }) => Promise<IDomain | null>;
  verifyDomain: (data: TDomainVerificationForm) => Promise<IDomain | null>;
  lockDomain: (domainId: string) => Promise<boolean>;
  unlockDomain: (domainId: string) => Promise<boolean>;
  deleteDomain: (domainId: string) => Promise<boolean>;
  resendVerificationEmail: (
    data: TGenerateVerificationForm,
  ) => Promise<boolean>;
}

/**
 * Custom hook for domain management operations
 */
const useDomains = (): UseDomainReturn => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState(false);
  const [domains, setDomains] = useState<IDomainListResponse>({
    ...(InitGetListParams as IGetListParams),
    total: 0,
    items: [],
  } as IDomainListResponse);
  const [queryParams, setQueryParams] =
    useState<IGetListParams>(InitGetListParams);

  /**
   * Fetch domains list
   */
  const fetchDomains = useCallback(async () => {
    setLoading(true);
    try {
      const queryString = objectToQueryString(
        queryParams as Record<string, unknown>,
      );
      const response = await apiClient.get(
        API_END_POINTS.DOMAIN_LIST + queryString,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(`Failed to fetch domains: ${response.statusCode}`);
      }

      setDomains(response.data);
    } catch (error) {
      console.error('Error fetching domains:', error);
      toast.error(TOAST_MESSAGES.NETWORK_ERROR);
    } finally {
      setLoading(false);
    }
  }, [apiClient, queryParams]);

  /**
   * Generate and send verification email
   */
  const generateVerificationEmail = useCallback(
    async (data: TGenerateVerificationForm): Promise<boolean> => {
      try {
        const response = await apiClient.post(
          API_END_POINTS.DOMAIN_GENERATE_CODE,
          { data },
        );

        if (!isSuccessResponse(response.statusCode)) {
          toast.error(response.message);
          throw new Error(
            `Failed to generate verification email: ${response.statusCode}`,
          );
        }

        toast.success(TOAST_MESSAGES.DOMAIN_VERIFICATION_EMAIL_SENT);
        return true;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 429) {
          toast.error(TOAST_MESSAGES.TOO_MANY_ATTEMPTS);
        } else if (err.response?.status === 409) {
          toast.error(TOAST_MESSAGES.DOMAIN_LOCKED_BY_ANOTHER);
        }
        return false;
      }
    },
    [apiClient],
  );

  /**
   * Add domain with code
   */
  const addDomain = useCallback(
    async (data: { domain: string }): Promise<IDomain | null> => {
      try {
        const response = await apiClient.post(API_END_POINTS.DOMAIN_ADD, {
          data,
        });

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to add domain: ${response.statusCode}`);
        }

        toast.success(response.message || TOAST_MESSAGES.DOMAIN_ADDED);
        return response.data;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        toast.error(
          err.response?.data?.message || TOAST_MESSAGES.NETWORK_ERROR,
        );
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Verify domain with code
   */
  const verifyDomain = useCallback(
    async (data: TDomainVerificationForm): Promise<IDomain | null> => {
      try {
        const response = await apiClient.post(API_END_POINTS.DOMAIN_VERIFY, {
          data,
        });

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to verify domain: ${response.statusCode}`);
        }

        toast.success(TOAST_MESSAGES.DOMAIN_VERIFIED);
        return response.data;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.data?.message?.includes('expired')) {
          toast.error(TOAST_MESSAGES.EXPIRED_VERIFICATION_CODE);
        } else {
          toast.error(
            err.response?.data?.message ||
              TOAST_MESSAGES.INVALID_VERIFICATION_CODE,
          );
        }
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Lock a verified domain
   */
  const lockDomain = useCallback(
    async (domainId: string): Promise<boolean> => {
      try {
        const response = await apiClient.put(
          API_END_POINTS.DOMAIN_LOCK(domainId),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to lock domain: ${response.statusCode}`);
        }

        toast.success(TOAST_MESSAGES.DOMAIN_LOCKED);
        return true;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 403) {
          toast.error(TOAST_MESSAGES.UNAUTHORIZED);
        } else {
          toast.error(
            err.response?.data?.message || TOAST_MESSAGES.NETWORK_ERROR,
          );
        }
        return false;
      }
    },
    [apiClient],
  );

  /**
   * Unlock a locked domain
   */
  const unlockDomain = useCallback(
    async (domainId: string): Promise<boolean> => {
      try {
        const response = await apiClient.put(
          API_END_POINTS.DOMAIN_UNLOCK(domainId),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to unlock domain: ${response.statusCode}`);
        }

        toast.success(TOAST_MESSAGES.DOMAIN_UNLOCKED);
        return true;
      } catch (error: unknown) {
        const err = error as { response?: { data?: { message?: string } } };
        toast.error(
          err.response?.data?.message || TOAST_MESSAGES.NETWORK_ERROR,
        );
        return false;
      }
    },
    [apiClient],
  );

  /**
   * Delete a domain
   */
  const deleteDomain = useCallback(
    async (domainId: string): Promise<boolean> => {
      try {
        const response = await apiClient.del(
          API_END_POINTS.DOMAIN_DELETE(domainId),
        );

        if (!isSuccessResponse(response.statusCode)) {
          toast.error(response.message);
          throw new Error(`Failed to delete domain: ${response.statusCode}`);
        }

        toast.success(TOAST_MESSAGES.DOMAIN_DELETED);
        return true;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 403) {
          toast.error(TOAST_MESSAGES.UNAUTHORIZED);
        }
        return false;
      }
    },
    [apiClient],
  );

  /**
   * Resend verification email
   */
  const resendVerificationEmail = useCallback(
    async (data: TGenerateVerificationForm): Promise<boolean> => {
      try {
        const response = await apiClient.post(
          API_END_POINTS.DOMAIN_RESEND_CODE,
          { data },
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            `Failed to resend verification email: ${response.statusCode}`,
          );
        }

        toast.success(TOAST_MESSAGES.DOMAIN_VERIFICATION_EMAIL_SENT);
        return true;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 429) {
          toast.error(TOAST_MESSAGES.TOO_MANY_ATTEMPTS);
        } else {
          toast.error(
            err.response?.data?.message || TOAST_MESSAGES.NETWORK_ERROR,
          );
        }
        return false;
      }
    },
    [apiClient],
  );

  return {
    domains,
    loading,
    queryParams,
    setQueryParams,
    fetchDomains,
    generateVerificationEmail,
    addDomain,
    verifyDomain,
    lockDomain,
    unlockDomain,
    deleteDomain,
    resendVerificationEmail,
  };
};

export default useDomains;
