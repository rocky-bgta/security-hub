export interface IInvoice {
  id: string;
  clientName: string;
  email: string;
  mspName: string;
  customerId: string;
  totalAmount: number;
  currency: string;
  status: string;
  dueDate: string;
  createdAt: string;
  updatedAt: string;
  paymentMethod?: string;
}

export interface IPaymentSource {
  method: string;
  amount: number;
  online: boolean;
  transactionId: string;
  metadata: {
    transactionReceiptUrl: string;
  };
}

export interface IPaymentReport {
  id: string;
  invoiceId: string;
  invoiceNumber: string;
  clientName: string;
  email: string;
  mspName: string;
  discountType: string;
  discountPercentage: number;
  vatPercentage: number;
  amount: number;
  dueAmount: number;
  outstanding: number;
  totalAmount: number;
  date: string;
  invoiceDate: string;
  paymentMethod: string;
  status: string;
  description: string;
  transactionId: string;
  invoicePdfLink: string;
  invoiceCreatedAt: string;
  createdAt: string;
  paymentSources: IPaymentSource[];
  paymentId: string;
  currency: string;
  online: boolean;
  clientId: string;
  notes: string;
  paymentDate: string;
  couponId: string;
  couponCode: string;
  couponDiscountAmount: number;
  discountAmount: number;
  actualAmount: number;
  subtotal: number;
  vatAmount: number;
  metaData: string;
  breakdown: [
    {
      packageId: string;
      originalPrice: number;
      discountedPrice: number;
      couponApplied: boolean;
    },
  ];
  invoiceFileKey: string;
  receiptGenerated: boolean;
  clientAdminId: string;
  clientProductIds: string[];
  invoiceSubtotal: number;
  invoiceDiscountAmount: number;
  invoiceVatAmount: number;
  statusNote: string;
  invoiceStatus: string;
  paidAt: string;
  productSelections: [
    {
      productId: string;
      packageId: string;
      licenseCount: number;
      pricePerLicense: number;
      validityPeriod: number;
      validityUnit: string;
      productName: string;
      packageName: string;
    },
  ];
  active: boolean;
}

export enum PaymentMethod {
  STRIPE = 'STRIPE',
  PAYPAL = 'PAYPAL',
  BANK_TRANSFER = 'BANK_TRANSFER',
  CHECK_PAYMENT = 'CHECK_PAYMENT',
  CREDIT = 'CREDIT',
  OTHER = 'OTHER',
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export interface ISummaryData {
  totalPayments: number;
  totalPaymentsChange: number;
  outstandingAmount: number;
  outstandingAmountChange: number;
  paidInvoicesCount: number;
  totalInvoicesCount: number;
  overdueInvoicesCount: number;
}

export interface IPaymentComment {
  id: string;
  invoiceId: string;
  date: string;
  userName: string;
  userRole: string;
  comment: string;
  actionTakenId: string;
  actionName: string;
  nextStepId: string;
  nextStepName: string;
  approved: boolean;
  createdAt: string;
}

export interface IInvoiceDetails {
  id: string;
  clientAdminId: string;
  clientName: string;
  mspAdminId: string;
  mspName: string;
  clientProductIds: string[];
  subtotal: number;
  discountType: string;
  discountAmount: number;
  discountPercentage: number;
  vatAmount: number;
  vatPercentage: number;
  totalAmount: number;
  invoicePdfLink: string;
  status: string;
  reason?: string;
  statusNote?: string;
  createdAt: string;
  paidAt: string;
  couponCode: string;
  couponId: string;
  couponDiscountAmount: number;
  productSelections: [
    {
      productId: string;
      packageId: string;
      licenseCount: number;
      pricePerLicense: number;
      validityPeriod: number;
      validityUnit: string;
      productName: string;
      packageName: string;
    },
  ];
  countryId: string;
  countryName: string;
  roleType: string;
  paymentMethod: string;
  paymentDetails: {
    bankName?: string;
    accountNumber?: string;
    bankBranchName?: string;
    transactionNumber?: string;
    paymentDate?: string;
    paymentAmount?: number;
    transactionReceiptUrl?: string;
    checkNumber?: string;
    branchName?: string;
    checkImageUrl?: string;
  };
}
