export interface IMsp {
  id: string;
  organizationName: string;
  mspAdminEmail: string;
  mspTier: string;
  status: string;
  licenseCount: number;
  usedLicenseCount: number;
  country: string;
}

export interface IMspTier {
  id: string;
  tierName: string;
}
