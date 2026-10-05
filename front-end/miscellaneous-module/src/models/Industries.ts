export interface IIndustriesPayload {
  name: string;
  code: string;
  active: boolean;
}

export interface IIndustries extends IIndustriesPayload {
  id: string;
  name: string;
  code: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}
