export interface IClientAdminDashboardSummary {
  totalProduct: number;
  totalLicense: number;
  totalTopic: number;
  totalCertificate: number;
}

export interface IUserDashboardSummary {
  id: string;
  organizationAdminId: string;
  totalProduct: number;
  totalPackage: number;
  totalLicense: number;
  totalClient: number;
  totalMsp: number;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

export interface IDashboardCardTooltip {
  id: string;
  packageName: string;
  price: number;
  thumbnailUrl: string;
}

export interface IDashboardCardTooltipGrouped {
  groupedPackages: {
    IN_PROGRESS: Array<IDashboardCardTooltip>;
    NOT_STARTED: Array<IDashboardCardTooltip>;
    COMPLETED: Array<IDashboardCardTooltip>;
  };
}

export interface IClientAdminLicenseStatistics {
  licenseCount: number;
  usedLicenseCount: number;
  availableLicenseCount: number;
  utilizationPercentage: number;
}

export interface IPhishingAssetCounts {
  numberOfEmailTemplates: number;
  numberOfLandingPages: number;
  numberOfSenderProfiles: number;
}
