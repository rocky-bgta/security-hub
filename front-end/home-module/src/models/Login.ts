export interface ILoginCredentials {
  username: string;
  password: string;
  deviceInfo: {
    platformType: string;
    platformInfo: string;
    platformVersion: string;
    deviceIdentifier: string;
    appLanguage: string;
    appVersion: string;
  };
}

export interface IUserMfaMethodItem {
  method: string;
  isDefault: boolean;
  createdAt: string;
}

export interface IUserMfaMethodsResponse {
  methods: Array<IUserMfaMethodItem>;
}

export interface IMfaMethodsResponse {
  methods: Array<string>;
}

export interface ILoginResponse {
  accessToken: string;
  credentialChangeNeeded: boolean;
  deviceBindingNeeded: boolean;
  lastLoginTime: string;
  refreshToken: string;
  roleNames: Array<string>;
  message?: string;
  mfaSetupRequired: boolean;
  mfaVerificationRequired: boolean;
  methods: Array<IUserMfaMethodItem>;
  tempToken: string;
  trialDaysLeft: string;
  isBuyNow: boolean;
}
