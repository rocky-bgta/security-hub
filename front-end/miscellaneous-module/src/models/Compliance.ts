export interface ICompliancePayload {
  complianceName: string;
  acronym: string;
  description: string;
  sortOrder: number;
}

export interface ICompliance extends ICompliancePayload {
  id: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}
