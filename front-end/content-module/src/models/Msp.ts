export interface IMSPErrors {
  mspName?: string;
  mspTier?: string;
  contactEmail?: string;
  phoneNumber?: string;
  country?: string;
  stateProvince?: string;
  industry?: string;
  organizationSize?: string;
  mspType?: string;
  mspAdminEmail?: string;
  address?: string;
  timeZone?: string;
  language?: string;
  creditAmount?: string;
  netTerms?: string;
  selectedMSP?: string;
  selectedClient?: string;
}

export interface IMSPDropdownData {
  id: string;
  organizationName: string;
  country: string;
  stateProvince: string;
}
