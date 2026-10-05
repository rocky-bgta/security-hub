export interface ITimeZonePayload {
  countryId: string;
  stateId: string;
  timezoneId: string;
  displayName: string;
  displayOrder: number;
  active: boolean;
}

export interface ITimeZone extends ITimeZonePayload {
  id: string;
  countryId: string;
  stateId: string;
  timezoneId: string;
  displayName: string;
  displayOrder: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}
