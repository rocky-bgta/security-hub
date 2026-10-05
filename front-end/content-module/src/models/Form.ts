import { ISelectTopic } from './Course';

export interface IUserTypeDetails {
  selectRole?: string;
  countryFilter?: string;
  provinceFilter?: string;
  selectedUser?: string;
  selectedMSP?: string;
  selectedClient?: string;
}

export interface ISelectedProduct {
  productId: string;
  packageId: string;
  licenseCount: number;
  pricePerLicense: number;
  validityPeriod: number;
  validityUnit: string;
  productName: string;
  packageName: string;
  userRangeId?: string;
}

export interface IProductSelection {
  selectAll: boolean;
  products: Array<ISelectedProduct>;
}

export interface IInvoice {
  subtotal: number;
  discountType: 'FLAT' | 'PERCENTAGE';
  discountValue: string;
  discountPercentage: number;
  discountAmount: number;
  couponCode: string;
  vatRate: string;
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
      paymentAmount: string;
      transactionReceiptUrl: string;
      transactionReceiptFile?: File | null;
    };
    checkPaymentDetails?: {
      checkNumber: string;
      bankName: string;
      branchName: string;
      paymentDate: string;
      paymentAmount: string;
      checkImageUrl: string;
      checkImageFile?: File | null;
    };
  };
}

export interface IEmailTemplate {
  templateType: string;
  templateBody: string;
  sendConfirmationEmail: boolean;
}

export interface IAssignLicenseFormData {
  selectUserType?: IUserTypeDetails;
  productSelections?: IProductSelection;
  invoice?: IInvoice;
  emailTemplate?: IEmailTemplate;
}

export interface IMSPOnboardingFormData {
  companyName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  industry: string;
  expectedUsers: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  billingAddress: string;
}

export interface IValidationErrors {
  [key: string]: string;
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

export interface ISubPackageName {
  name: string;
  description: string;
  status?: string;
}

export interface IAssignUserFormData {
  subPackageName: ISubPackageName;
  // selectUserType: IUserTypeDetails;
  productSelections: IProductSelection;
  // emailTemplate: IEmailTemplate;
}

export interface ISubPackageFormData {
  selectUserType: IUserTypeDetails;
  emailTemplate: IEmailTemplate;
}

export interface ICreateSubPackageFormData {
  subPackageName: ISubPackageName;
  topics: Array<ISelectTopic>;
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
