export interface Customer {
  id: string;
  name: string;
  tier: string;
  status: 'Active' | 'Inactive' | 'Suspended';
  email: string;
  phone: string;
  totalLicenses: number;
  usedLicenses: number;
  availableCredit: number;
  balance: number;
  totalPayments: number;
  joinDate: string;
}

export interface CreditDetails {
  id: string;
  clientId: string;
  totalCredits: number;
  availableCredits: number;
  expirationDate: string;
  active: boolean;
  reason: string;
  createdAt: string;
  addedBy: string;
}

export interface Transaction {
  id: string;
  clientId: string;
  amount: number;
  type: string;
  description: string;
  referenceId: string;
  referenceType: string;
  initiatedBy: string;
  createdAt: string;
}

export interface DepositRequest {
  creditAmount: number;
  expirationDate: string;
  reason: string;
  active: boolean;
}
