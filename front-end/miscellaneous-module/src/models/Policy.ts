import { Status } from './Global';

export interface IPolicy {
  id: string;
  policyName: string;
  policyTypeId: string;
  policyTypeName: string;
  effectiveDate: string;
  status: Status;
  policyEndDate: string;
  description: string;
  files: {
    fileUrl: string;
    fileType: 'WORD' | 'PDF' | 'IMAGE' | string;
  }[];
  industryId: string;
  companyName: string;
  createdAt: string;
  updatedAt: string;
}

export interface IPolicyPayload {
  policyName: string;
  description: string;
  policyType: string;
  effectiveDate: string;
  status: string;
  assignedMSP: string;
  country: string;
  industry: string;
  policyExpiryDate: string;
}

export interface IPolicyDetails {
  id: string;
  policyName: string;
  policyTypeId: string;
  policyTypeName: string;
  effectiveDate: string;
  status: 'ACTIVE' | 'INACTIVE' | string;
  policyEndDate: string;
  description: string;
  files: {
    fileUrl: string;
    fileType: 'WORD' | 'PDF' | 'IMAGE' | string;
  }[];
  industryId: string;
  industryName: string;
  companyName: string;
  country: {
    id: string;
    code: string;
    name: string;
  };
  industry: {
    id: string;
    code: string;
    name: string;
  };
}

export interface IPolicyList {
  id: string;
  policyName: string;
  policyTypeId: string;
  policyTypeName: string;
  effectiveDate: string;
  status: 'ACTIVE';
  policyEndDate: string;
  description: string;
  files: [
    {
      fileUrl: string;
      fileType: 'PDF' | 'WORD' | 'IMAGE' | string;
    },
  ];
  industryId: string;
  companyName: string;
  createdAt: string;
  updatedAt: string;
  clientAdminId: string;
}
