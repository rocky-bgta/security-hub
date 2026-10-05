export interface IClientAdminInfoResponse {
  id: string;
  email: string;
  organizationName: string;
  contactEmail: string;
  phoneNumber: string;
  billingName: string;
  billingEmail: string;
  billingUseSameAsOrganizationAddress: boolean;
  billingStreetAddress: string;
  billingStreetAddressLine2: string;
  billingCity: string;
  billingZipPostalCode: string;
  billingCountry: string;
  billingStateProvince: string;
  mspId: string;
  country: {
    id: string;
    code: string;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  state: {
    id: string;
    code: string;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  timeZone: {
    id: string;
    code: string;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  language: {
    id: string;
    code: string;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  industry: {
    id: string;
    code: string;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  organizationSize: {
    id: string;
    code: string | null;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  organizationType: {
    id: string;
    code: null;
    name: string;
    displayName: string | null;
    range: string | null;
    active: boolean;
  };
  domain: string;
  streetAddress: string;
  streetAddressLine2: string;
  city: string;
  zipPostalCode: string;
  logoUrl: string;
  status: string;
  createdAt: string;
  clientAdminId: string | null;
  creditId: string | null;
  tierId: string | null;
  mspType: string | null;
  mspAdminEmail: string | null;
  netDays: number | null;
  department: string | null;
  roleIds: Array<string> | null;
}

export interface IClientAdminInfoPayload {
  organizationName: string;
  industry: string;
  contactEmail: string;
  country: string;
  phoneNumber: string;
  organizationType: string;
  streetAddress: string;
  streetAddressLine2: string;
  city: string;
  stateProvince: string;
  zipPostalCode: string;
  timeZone: string;
  language: string;
  organizationSize: string;
  logoUrl: string;
  organizationLogo?: File | null;
  countryCode: string;
  stateCode: string;
  sameAsOrganizationAddress: boolean;
  billingName: string;
  billingEmail: string;
  billingCountry: string;
  billingStreetAddress: string;
  billingStreetAddressLine2: string;
  billingCity: string;
  billingZipPostalCode: string;
  billingStateProvince: string;
}
