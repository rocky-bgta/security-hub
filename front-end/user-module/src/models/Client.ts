export interface IOrganizationDetails {
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
  subIndustryId: string;
  subIndustryName: string;
  complianceId: string;
  complianceName: string;
  domain: string;
  organizationSize: string;
  streetAddress: string;
  streetAddressLine2: string;
  city: string;
  zipPostalCode: string;
  adminEmail: string;
  logoUrl?: string;
  organizationLogo?: File | null;
}

export interface IBillingDetails {
  billingName: string;
  billingEmail: string;
  useSameAsOrganizationAddress: boolean;
  streetAddress: string;
  streetAddressLine2?: string;
  city: string;
  stateProvince: string;
  country: string;
  zipPostalCode: string;
}

export interface IProductSelection {
  productId: string;
  packageId: string;
  packageName: string;
  productName: string;
  licenseCount: number;
  pricePerLicense: number;
  validityPeriod: number;
  validityUnit: string;
  userRangeId?: string;
}

export interface IInvoice {
  subtotal: number;
  discountType: 'FLAT' | 'PERCENTAGE';
  discountValue: number;
  discountPercentage: number;
  discountAmount: number;
  couponCode: string;
  vatRate: number;
  vatAmount: number;
  totalAmount: number;
  paymentStatus: 'COMPLETED' | 'PENDING';
  completedPayment: {
    paymentMethod: 'BANK_TRANSFER' | 'CHECK_PAYMENT';
    bankTransferDetails?: {
      bankName: string;
      accountNumber: string;
      bankBranchName: string;
      transactionNumber: string;
      paymentDate: string;
      paymentAmount: number;
      transactionReceiptUrl: string;
      transactionReceiptFile?: File | null;
    };
    checkPaymentDetails?: {
      checkNumber: string;
      bankName: string;
      branchName: string;
      paymentDate: string;
      paymentAmount: number;
      checkImageUrl: string;
      checkImageFile?: File | null;
    };
  };
}

export interface IClientOnboarding {
  organization: IOrganizationDetails;
  billing: IBillingDetails;
  mspId: string;
  mspName: string;
  productSelections: Array<IProductSelection>;
  invoice: IInvoice;
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

export interface IClientProduct {
  productId: string;
  productName: string;
}

export interface IOrganization {
  id: string;
  email: string;
  organizationName: string;
  contactEmail: string;
  phoneNumber: string;
  phoneCode: string;
  billingName: string;
  billingEmail: string;
  billingAddress: string;
  mspId: string;
  country: string;
  countryCode: string | null;
  state: string;
  stateCode: string | null;
  timeZone: string;
  language: string;
  industry: string;
  domain: string;
  organizationSize: 'Small' | 'Medium' | 'Large' | string;
  address: string;
  logoUrl: string;
  clientProducts: IClientProduct[];
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | string;
  createdAt: string;
  clientAdminId: string | null;
  creditId: string | null;
  tierId: string | null;
  mspType: string | null;
  mspAdminEmail: string | null;
  netDays: number | null;
  department: string | null;
  roleIds: string[] | null;
}

export enum DiscountType {
  FLAT = 'FLAT',
  PERCENTAGE = 'PERCENTAGE',
}

export interface IClientDropdownData {
  id: string;
  organizationName: string;
}

export interface IPaymentComment {
  invoiceId: string;
  comment: string;
  actionTakenId: string;
  nextStepId: string;
  userName: string;
  userRole: string;
  approved: boolean;
}

export interface IBillingAction {
  id: string;
  name: string;
}

export interface IBillingNextStep {
  id: string;
  name: string;
}
