export interface IClientDropdownData {
  id: string;
  organizationName: string;
  country: string;
  state: string;
  mspId: string;
  email?: string;
}

export interface IClientDetails {
  id: string;
  email: string;
  organizationName: string;
  contactEmail: string;
  billingCountry: string;
  billingStateProvince: string;
}
