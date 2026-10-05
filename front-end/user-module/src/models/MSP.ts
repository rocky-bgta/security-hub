import { IInvoice } from './Client';

export interface IMSPOrganizationDetails {
  organizationName: string;
  organizationType: string;
  contactEmail: string;
  phoneNumber: string;
  phoneCode: string;
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
  tierId: string;
  mspTypeId?: string;
  mspAdminEmail: string;
  organizationLogo?: File | null;
}

export interface IMSPBillingDetails {
  billingEmail: string;
  billingName: string;
  useSameAsOrganizationAddress: boolean;
  streetAddress: string;
  streetAddressLine2?: string;
  city: string;
  stateProvince: string;
  country: string;
  zipPostalCode: string;
}

export interface IMSPSelectedProduct {
  productId: string;
  productName: string;
  packageId: string;
  packageName: string;
  licenseCount: number;
  pricePerLicense: number;
  validityPeriod: number;
  validityUnit: string;
  userRangeId?: string;
}

export interface IMSPCreditInfo {
  enableCredit: boolean;
  creditAmount: number;
  reason: string;
  netDaysId: string;
  autoSuspendOnOverdue: boolean;
  creditStartDate: string;
  creditEndDate: string;
  netDays: IMSPTierInfo | null;
}

export interface IMSPOnboarding {
  organization: IMSPOrganizationDetails;
  billing: IMSPBillingDetails;
  productSelections: Array<IMSPSelectedProduct>;
  invoice: IInvoice;
  creditInfo: IMSPCreditInfo;
}

export interface IMSPTier {
  id: string;
  tierName: string;
  commissionPercentage: number;
  salesThreshold: number;
  active: boolean;
  eligibilityCriteria: string;
  tierBenefits: string;
  createdAt: string;
  tierDescription: string;
}

export interface IMSPData {
  id: string;
  companyName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  website: string;
  industry: string;
  companySize: string;
  status: 'active' | 'inactive' | 'suspended' | 'pending';
  subscriptionPlan: string;
  licenseCount: number;
  usedLicenses: number;
  billingEmail: string;
  taxId: string;
  notes: string;
  settings: {
    emailNotifications: boolean;
    smsNotifications: boolean;
    autoRenewal: boolean;
    apiAccess: boolean;
    customBranding: boolean;
    multiFactorAuth: boolean;
    ssoEnabled: boolean;
    reportingAccess: boolean;
  };
  createdAt: string;
  lastLogin: string;
  contractEndDate: string;
}

export interface IMSPDropdownData {
  id: string;
  organizationName: string;
  country: string;
  stateProvince: string;
}

export interface IMSPList {
  id: string;
  mspId: string;
  organizationName: string;
  logoUrl: string | null;
  mspAdminEmail: string;
  contactEmail: string;
  phoneNumber: string | null;
  country: string | null;
  stateProvince: string | null;
  status: string;
  joinedDate: string;
  updatedAt: string;
  totalClients: number;
  licenseCount: number;
  usedLicenseCount: number;
  revenue: number;
  lastLoginAt: string;
  productLists: string[];
}

export interface IMSPTierInfo {
  id: string;
  name: string;
}

export interface IMSPDropdownRef {
  id: string;
  name: string;
}

export type IMSPDropdownValue = string | IMSPDropdownRef;

export interface IMSPOrganization {
  organizationName: string;
  organizationType: IMSPDropdownValue;
  contactEmail: string;
  phoneNumber: string;
  country: IMSPDropdownValue;
  stateProvince: IMSPDropdownValue;
  timeZone: IMSPDropdownValue;
  language: IMSPDropdownValue;
  industry: IMSPDropdownValue;
  domain: string;
  organizationSize: IMSPDropdownValue;
  streetAddress: string;
  streetAddressLine2: string;
  city: string;
  zipPostalCode: string;
  logoUrl: string;
  tier: IMSPTierInfo;
  mspType: IMSPTierInfo;
  netDays: IMSPTierInfo;
  mspAdminEmail: string;
}

export interface IMSPCreditInfo {
  enableCredit: boolean;
  creditAmount: number;
  reason: string;
  netDays: IMSPTierInfo | null;
  autoSuspendOnOverdue: boolean;
  creditStartDate: string;
  creditEndDate: string;
}

export interface IMSPAssignedClient {
  clientId: string;
  clientName: string;
  contactEmail: string;
  status: string;
  licenseCount: number;
  createdAt: string;
}

export interface IMSPLicenseAllocation {
  totalLicenses: number;
  usedLicenses: number;
  availableLicenses: number;
  usagePercentage: number;
}

export interface IMSPProfile {
  id: string;
  mspId: string;
  organization: IMSPOrganization;
  billingEmail: string;
  billingName: string;
  billingStreetAddress: string;
  billingStreetAddressLine2: string;
  billingCity: string;
  billingStateProvince: IMSPDropdownValue;
  billingCountry: IMSPDropdownValue;
  billingZipPostalCode: string;
  billingAddress: string;
  creditInfo: IMSPCreditInfo;
  notes: string;
  status: string;
  assignedClients: IMSPAssignedClient[];
  licenseAllocation: IMSPLicenseAllocation;
  createdAt: string;
  updatedAt: string;
}

export interface IMSPProductUpdate {
  mspProductId?: string;
  productId: string;
  packageId: string;
  licenseCount: number;
  pricePerLicense: number;
  validityPeriod: number;
  validityUnit: string;
  remove?: boolean;
}

export interface IMSPCreditUpdate {
  enableCredit: boolean;
  reason: string;
  creditAmount: number;
  netDaysId: string;
  creditStartDate: string;
  autoSuspendOnOverdue: boolean;
}

export interface IMSPEditPayload {
  organizationName: string;
  mspTier: string;
  mspTypeId: string;
  contactEmail: string;
  mspAdminEmail: string;
  phoneNumber: string;
  phoneCode: string;
  country: string;
  stateProvince: string;
  timeZone: string;
  language: string;
  industry: string;
  domain: string;
  organizationType: string;
  organizationSize: string;
  organizationStreetAddress: string;
  organizationStreetAddressLine2: string;
  organizationCity: string;
  organizationStateProvince: string;
  organizationCountry: string;
  organizationZipPostalCode: string;
  logoUrl: string;
  billingEmail: string;
  billingName: string;
  billingStreetAddress: string;
  billingStreetAddressLine2: string;
  billingCity: string;
  billingStateProvince: string;
  billingCountry: string;
  billingZipPostalCode: string;
  productUpdates: Array<IMSPProductUpdate>;
  clientIdsToAssign: Array<string>;
  clientIdsToRemove: Array<string>;
  creditUpdate: IMSPCreditUpdate;
  notes: string;
}
