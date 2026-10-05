export interface ICountryPayload {
  name: string;
  code: string;
  phoneCode: string;
  displayOrder: number;
  active: boolean;
}

export interface ICountry extends ICountryPayload {
  id: string;
  createdAt: string;
  updatedAt: string;
}
