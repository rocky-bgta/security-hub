export interface IVatRegion {
  id: string;
  regionName: string;
  vatRate: number;
}

export interface IVatConfiguration {
  id: string;
  countryName: string;
  defaultVatRate: number;
  regionBased: boolean;
  regions: IVatRegion[];
  createdAt: number;
  updatedAt: number;
}

export interface ISyncMember {
  id: string;
  displayName: string;
  email: string;
  userPrincipalName: string;
  givenName: string;
  surname: string;
  jobTitle: string;
  department: string;
  officeLocation: string;
  mobilePhone: string;
  accountEnabled: boolean;
}
export interface ISyncGroup {
  id: string;
  displayName: string;
  description: string;
  mailNickname: string;
  memberCount: number;
  members: ISyncMember[];
}
