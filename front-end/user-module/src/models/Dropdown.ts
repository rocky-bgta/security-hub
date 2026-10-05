export interface IDropdownData {
  organizationTypes: Array<IOrganizationType>;
  countries: Array<ICountry>;
  states: Array<IState>;
  timeZones: Array<ITimeZone>;
  languages: Array<ILanguage>;
  industries: Array<IIndustry>;
  organizationSizes: Array<IOrganizationSize>;
  tier?: Array<ITier>;
  mspTypes?: Array<IMspType>;
  subIndustries?: Array<ISubIndustry>;
  compliances?: Array<ICompliance>;
}

export interface IDropdownOption {
  id: string;
  name: string;
}

export interface IOrganizationType extends IDropdownOption {
  organizationType?: string;
}

export interface ICountry extends IDropdownOption {
  code: string;
  phoneCode: string;
}

export interface IState extends IDropdownOption {
  countryId: string;
}

export interface ITimeZone extends IDropdownOption {
  countryId: string;
  stateId: string;
  displayName?: string;
  timezoneId?: string;
}

export interface ILanguage extends IDropdownOption {
  code: string;
  displayName?: string;
}

export type IIndustry = IDropdownOption;

export interface IOrganizationSize extends IDropdownOption {
  range?: string;
}

export type ITier = IDropdownOption;

export type IMspType = IDropdownOption;

export interface ISubIndustry extends IDropdownOption {
  code?: string;
  industryId?: string;
}

export interface ICompliance extends IDropdownOption {
  complianceName?: string;
}
