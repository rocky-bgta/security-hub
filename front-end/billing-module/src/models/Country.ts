export interface ICountryDropdown {
  id: string;
  name: string;
  code: string;
  active: boolean;
}

export interface IStateDropdown {
  id: string;
  name: string;
  code: string;
  active: boolean;
  countryId: string;
}
