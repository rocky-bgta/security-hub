export interface IClientOnboarding {
  address: string;
  billingAddress: string;
  billingEmail: string;
  billingName: string;
  clientAdminId: null;
  clientProducts: Array<IClientProduct>;
  contactEmail: string;
  country: string;
  countryCode: null;
  createdAt: string;
  creditId: null;
  department: null;
  domain: string;
  email: string;
  id: string;
  industry: string;
  language: string;
  logoUrl: '';
  mspAdminEmail: null;
  mspId: string;
  mspType: null;
  netDays: null;
  organizationName: string;
  organizationSize: string;
  organizationType: string;
  phoneNumber: string;
  roleIds: null;
  state: string;
  stateCode: null;
  status: string;
  tierId: null;
  timeZone: string;
}

export interface IClientProduct {
  id: string;
  clientAdminId: string;
  productId: string;
  packageId: string;
  product: IProduct;
  packageDetails: IPackageDetails;
  licenseCount: number;
  usedLicenseCount: number;
  pricePerLicense: number;
  totalPrice: number;
  validityPeriod: number;
  validityUnit: string;
  assignedAt: string;
  expiryDate: string | null;
  licenseStatus: string;
}

// Product details
export interface IProduct {
  productId: string;
  productName: string;
  productDescription: string;
  productStatus: string;
  thumbnailUrl: string;
}

export interface IOrganizationDetails {
  organizationName: string;
  organizationType: string;
  contactEmail: string;
  phoneNumber: string;
  countryCode: string;
  country: string;
  stateProvince: string;
  timeZone: string;
  language: string;
  industry: string;
  domain: string;
  organizationSize: string;
  adminEmail: string;
  address: string;
  organizationLogo?: File | null;
  logoUrl?: string;
}

export interface IClientAdminDetails {
  id: string;
  email: string;
  organizationName: string;
  contactEmail: string;
  phoneNumber: string;
  billingName: string;
  billingEmail: string;
  billingAddress: string;
  mspId: string;
  countryCode: string | null;
  stateCode: string | null;
  domain: string;
  address: string;
  logoUrl: string;
  status: string;
  createdAt: string;
  clientAdminId: string | null;
  creditId: string | null;
  tierId: string | null;
  mspType: string | null;
  mspAdminEmail: string | null;
  netDays: number | null;
  department: string | null;
  roleIds: string[] | null;
  clientProducts: IClientProduct[];
  country: string;
  state: string;
  timeZone: string;
  language: string;
  industry: string;
  organizationSize: string;
  organizationType: string;
}

// Package details
export interface IPackageDetails {
  id: string;
  packageName: string;
  price: number;
  packageStatus: string;
  basePackageId: string | null;
}

// MSP interfaces
export interface IMSPOnboarding {
  address: string;
  billingAddress: string;
  billingEmail: string;
  billingName: string;
  contactEmail: string;
  country: string;
  countryCode: string | null;
  createdAt: string;
  creditId: string | null;
  domain: string;
  email: string;
  id: string;
  industry: string;
  language: string;
  logoUrl: string;
  mspType: string | null;
  netDays: number | null;
  organizationName: string;
  organizationSize: string;
  organizationType: string;
  phoneNumber: string;
  state: string;
  stateCode: string | null;
  status: string;
  tierId: string | null;
  timeZone: string;
  mspProducts?: Array<IMSPProduct>;
}

export interface IMSPProduct {
  id: string;
  mspId: string;
  productId: string;
  packageId: string;
  product: IProduct;
  packageDetails: IPackageDetails;
  licenseCount: number;
  usedLicenseCount: number;
  pricePerLicense: number;
  totalPrice: number;
  validityPeriod: number;
  validityUnit: string;
  assignedAt: string;
  expiryDate: string | null;
  licenseStatus: string;
}

export interface IMSPAdminDetails {
  id: string;
  mspId: string;
  organization: {
    organizationName: string;
    organizationType: null;
    contactEmail: string;
    phoneNumber: string;
    country: string;
    stateProvince: string;
    timeZone: string;
    language: string;
    industry: string;
    domain: string;
    organizationSize: string;
    streetAddress: string;
    streetAddressLine2: string;
    city: string;
    zipPostalCode: string;
    logoUrl: string;
    tier: {
      id: string;
      name: string;
    };
    mspType: {
      id: string;
      name: null;
    };
    netDays: {
      id: string;
      name: null;
    };
    mspAdminEmail: string;
  };
  billing: {
    billingEmail: string;
    billingName: string;
    streetAddress: string;
    streetAddressLine2: string;
    city: string;
    stateProvince: string;
    country: string;
    zipPostalCode: string;
  };
  creditInfo: {
    enableCredit: boolean;
    creditAmount: number;
    reason: string;
    netDays: {
      id: string;
      name: null;
    };
    autoSuspendOnOverdue: boolean;
    creditStartDate: string;
    creditEndDate: string;
  };
  products: IClientProduct[];
  packages: IPackageDetails[];
  clientProducts: IClientProduct[];
  invoiceStatus: string;
  notes: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}
