export interface ISubIndustriesPayload {
  industryId: string;
  name: string;
  code: string;
  active: boolean;
}

export interface ISubIndustries extends ISubIndustriesPayload {
  id: string;
  name: string;
  code: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}
