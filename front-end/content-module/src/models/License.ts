import { IProduct } from './Product';

export interface IPaymentPayload {
  currency: string;
  amount: number;
  subtotal: number;
  vatAmount: number;
  discountAmount: number;
  discountPercentage: number;
  date: string;
  notes?: string;
}

export interface IClientAssignedPackageDetail {
  id: string;
  licenseCount: number;
  usedLicenseCount: number;
  pricePerLicense: number;
  totalPrice: number;
  validityPeriod: number;
  validityUnit: string;
  assignedAt: string;
  expiryDate: string;
  licenseStatus: string;
  topicCount: number;
  paymentPayload?: IPaymentPayload;
  product?: IProduct;
  packageDetails: {
    id: string;
    packageName: string;
    packageStatus: string;
    features?: Array<{ id: string; name: string }>;
  };
}

export interface IAssignedLicense {
  id: string;
  clientAdminId: string | null;
  productId: string;
  packageId: string;
  product: IProduct;
  packageDetails: {
    id: string;
    packageName: string;
    packageStatus: string;
  };
  licenseCount: number;
  usedLicenseCount: number;
  pricePerLicense?: number;
  totalPrice?: number;
  validityPeriod?: number;
  validityUnit?: string;
  licenseStatus?: string;
  paymentPayload?: IPaymentPayload;
  assignedAt: string;
  expiryDate: string | null;
  topicCount: number;
}

export interface ILicenseOverviewSummary {
  totalLicenses: number;
  assignedUsers: number;
  assignedUsersPercent: number;
  availableSeats: number;
  availableSeatsPercent: number;
  utilizationRate: number;
  utilizationChangeVsLastMonth: number;
  expiringSoon: number;
  expiringSoonDays: number;
  activeProducts: number;
}

export interface ILicenseAssignment {
  userId: string;
  fullName: string;
  email: string;
  department: string;
  packageId: string;
  packageName: string;
  productId: string;
  productName: string;
  subPackageId: string;
  subPackageName: string;
  assignedDate: string;
  expiryDate: string;
  status: string;
}

export interface ILicenseHistory {
  productId: string;
  productName: string;
  packageId: string;
  packageName: string;
  licenseCount: number;
  usedLicenseCount: number;
  assignedAt: string;
  expiryDate: string;
  licenseStatus: string;
  mspId: string;
  mspName: string;
  email: string;
  clientName: string;
}
