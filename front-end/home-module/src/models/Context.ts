import { AxiosRequestConfig } from 'axios';
import { createContext, Dispatch, SetStateAction } from 'react';

import { IBrandingData } from 'models/Branding';
import { IUserMfaMethodItem } from 'models/Login';
import { INavigationItem } from 'models/SideMenu';

export interface IToken {
  accessToken: string;
  refreshToken: string;
  roleNames: Array<string>;
  lastLoginTime: string;
  deviceBindingNeeded: boolean;
  credentialChangeNeeded: boolean;
}

interface IAuthContext {
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  isAuthenticating: boolean;
  role: string;
  roleNames: Array<string>;
  isAuthenticated: boolean;
  setAuthenticationInProgress: (inProgress: boolean) => void;
  login: (tokens: IToken) => void;
  logout: () => void;
}

export const AuthContext = createContext<IAuthContext | undefined>(undefined);

export interface IResponse<T> {
  message: string;
  statusCode: number;
  data: T;
}

interface IAPIServiceContext {
  get: <T = any>(
    url: string,
    config?: AxiosRequestConfig,
  ) => Promise<IResponse<T>>;
  post: <T = any>(
    url: string,
    config?: AxiosRequestConfig,
  ) => Promise<IResponse<T>>;
  put: <T = any>(
    url: string,
    config?: AxiosRequestConfig,
  ) => Promise<IResponse<T>>;
  patch: <T = any>(
    url: string,
    config?: AxiosRequestConfig,
  ) => Promise<IResponse<T>>;
  del: <T = any>(
    url: string,
    config?: AxiosRequestConfig,
  ) => Promise<IResponse<T>>;
}

export const APIServiceContext = createContext<IAPIServiceContext | null>(null);

export interface IUserInfo {
  userId: string;
  clientAdminId: string;
  email: string;
  username: string;
  phoneNumber: string;
  userStatus: 'ACTIVE' | 'INACTIVE' | 'PENDING';
  fullName: string;
  riskGroup: string;
  passwordExpiryDate: string;
  profilePicture: string;
  selfOnboardingUser: boolean;
  methods?: Array<IUserMfaMethodItem>;
  onboardBy: string;
  pendingPayment: boolean;
  clientProductTags: Array<'Phishing' | 'Security'>;
}

export interface IStoreContext {
  userInfo: IUserInfo;
  setUserInfo: Dispatch<SetStateAction<IUserInfo>>;
  menus: Array<INavigationItem>;
  isStoreReady: boolean;
  brandingInfo: IBrandingData;
  setBrandingInfo: Dispatch<SetStateAction<IBrandingData>>;
  clearStore: () => void;
  notificationChangeKey: number;
  markNotificationAsRead: (id: string) => Promise<boolean>;
  markAllNotificationsAsRead: () => Promise<boolean>;
}

export const StoreContext = createContext<IStoreContext>({} as IStoreContext);
