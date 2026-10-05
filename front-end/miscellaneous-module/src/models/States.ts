export interface IStatePayload {
  name: string;
  countryId: string;
  code: string;
  displayOrder: number;
  active: boolean;
}

export interface IState extends IStatePayload {
  id: string;
  countryId: string;
  name: string;
  code: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}
