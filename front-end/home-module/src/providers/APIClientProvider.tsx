import axios, { AxiosHeaders, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ReactNode, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

import useAuth from 'hooks/UseAuth';
import { APIServiceContext, IResponse } from 'models/Context';
import { ILoginResponse } from 'models/Login';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import { BASE_URL } from 'utils/Constants';
import { getDeviceInfo, isSuccessResponse } from 'utils/Helper';

type RetryableRequestConfig = AxiosRequestConfig & {
  _retry?: boolean;
};

interface IApiClient {
  get<T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<IResponse<T>>;
  post<T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<IResponse<T>>;
  put<T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<IResponse<T>>;
  patch<T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<IResponse<T>>;
  del<T = unknown>(
    url: string,
    config?: AxiosRequestConfig,
  ): Promise<IResponse<T>>;
}

const APIClientProvider = ({ children }: { children: ReactNode }) => {
  const navigate = useNavigate();
  const { accessToken, refreshToken, login, logout } = useAuth();

  // Always-current refs — written synchronously on every render before any
  // effect or event fires, so interceptors always read the latest values.
  const accessTokenRef = useRef(accessToken);
  const refreshTokenRef = useRef(refreshToken);
  const loginRef = useRef(login);
  const logoutRef = useRef(logout);
  const navigateRef = useRef(navigate);

  accessTokenRef.current = accessToken;
  refreshTokenRef.current = refreshToken;
  loginRef.current = login;
  logoutRef.current = logout;
  navigateRef.current = navigate;

  // Build the axios instance exactly once — never recreated on re-render.
  const apiClientRef = useRef<IApiClient | null>(null);

  if (!apiClientRef.current) {
    const axiosInstance = axios.create({
      baseURL: BASE_URL,
    });
    const refreshState: { promise: Promise<string | null> | null } = {
      promise: null,
    };

    const handleSessionExpired = (reason = 'session_expired') => {
      sessionStorage.setItem(
        'redirectAfterLogin',
        window.location.pathname + window.location.search,
      );
      logoutRef.current();
      navigateRef.current(`${routes.login.path}?reason=${reason}`, {
        replace: true,
      });
    };

    const refreshAccessToken = async () => {
      if (!refreshTokenRef.current) return null;

      try {
        const refreshPayload = {
          refreshToken: refreshTokenRef.current,
          deviceInfo: getDeviceInfo(),
        };

        const refreshResp: AxiosResponse<IResponse<ILoginResponse>> =
          await axiosInstance.post(API_END_POINTS.REFRESH, refreshPayload);

        if (!isSuccessResponse(refreshResp.data.statusCode)) {
          throw new Error(
            refreshResp.data.message || 'Failed to refresh access token',
          );
        }

        const newData = refreshResp.data.data;

        // Update refs immediately so every in-flight request that retries
        // in the same tick already reads the fresh tokens — before React
        // re-renders and propagates the new state values.
        accessTokenRef.current = newData.accessToken;
        refreshTokenRef.current = newData.refreshToken;

        loginRef.current({ ...newData });

        return newData.accessToken;
      } catch (error) {
        console.debug(
          'Failed to refresh access token:',
          (error as Error).message,
        );
        return null;
      }
    };

    const getFreshAccessToken = async () => {
      if (!refreshState.promise) {
        refreshState.promise = refreshAccessToken().finally(() => {
          refreshState.promise = null;
        });
      }

      return refreshState.promise;
    };

    axiosInstance.interceptors.request.use(config => {
      const headers = AxiosHeaders.from(config.headers);

      // Reads from ref — always the latest token, even immediately after a
      // refresh that hasn't propagated through React state yet.
      if (!headers.has('Authorization') && accessTokenRef.current) {
        headers.set('Authorization', `Bearer ${accessTokenRef.current}`);
      }

      config.headers = headers;

      return config;
    });

    axiosInstance.interceptors.response.use(
      response => response,
      async err => {
        const originalRequest = err.config as
          | RetryableRequestConfig
          | undefined;

        if (!originalRequest) {
          return Promise.reject(err);
        }

        const status = err?.response?.status;
        const isRefreshRequest = originalRequest.url === API_END_POINTS.REFRESH;

        console.debug(
          `API error on ${originalRequest.method} ${originalRequest.url}:`,
          status,
          err.response?.data,
        );

        if (isRefreshRequest && !isSuccessResponse(status)) {
          console.warn('Refresh token failed, logging out user');
          handleSessionExpired();
          return Promise.reject(err);
        }

        if (
          status === 401 &&
          !isRefreshRequest &&
          refreshTokenRef.current &&
          !originalRequest._retry
        ) {
          originalRequest._retry = true;

          try {
            const newAccessToken = await getFreshAccessToken();

            if (!newAccessToken) {
              console.warn('Failed to refresh access token, logging out user');
              handleSessionExpired();
              return Promise.reject(err);
            }

            originalRequest.headers = {
              ...originalRequest.headers,
              Authorization: `Bearer ${newAccessToken}`,
            };

            return axiosInstance.request(originalRequest);
          } catch (refreshError) {
            if (
              axios.isAxiosError(refreshError) &&
              refreshError.response?.status === 401
            ) {
              console.warn('Failed to refresh access token, logging out user');
              handleSessionExpired();
            }
            return Promise.reject(refreshError);
          }
        }

        if (status === 403) {
          console.warn('Access forbidden, logging out user');
          handleSessionExpired('session_invalid');
          return Promise.reject(err);
        }

        return Promise.reject(err);
      },
    );

    async function request<T = unknown>(
      config: AxiosRequestConfig,
    ): Promise<IResponse<T>> {
      try {
        const response: AxiosResponse<IResponse<T>> =
          await axiosInstance.request<IResponse<T>>(config);
        return response.data;
      } catch (err: unknown) {
        console.error('API request error:', err);

        if (axios.isAxiosError(err)) {
          return {
            message: err.response?.data?.message ?? 'Request failed',
            statusCode: err.response?.data?.statusCode ?? 500,
            data: err.response?.data?.data as T,
          };
        }

        return {
          message: 'An unexpected error occurred',
          statusCode: 500,
          data: {} as T,
        };
      }
    }

    apiClientRef.current = {
      get<T = unknown>(url: string, config: AxiosRequestConfig = {}) {
        return request<T>({ ...config, method: 'GET', url });
      },
      post<T = unknown>(url: string, config: AxiosRequestConfig = {}) {
        return request<T>({ ...config, method: 'POST', url });
      },
      put<T = unknown>(url: string, config: AxiosRequestConfig = {}) {
        return request<T>({ ...config, method: 'PUT', url });
      },
      patch<T = unknown>(url: string, config: AxiosRequestConfig = {}) {
        return request<T>({ ...config, method: 'PATCH', url });
      },
      del<T = unknown>(url: string, config: AxiosRequestConfig = {}) {
        return request<T>({ ...config, method: 'DELETE', url });
      },
    };
  }

  return (
    <APIServiceContext.Provider value={apiClientRef.current}>
      {children}
    </APIServiceContext.Provider>
  );
};

export default APIClientProvider;
