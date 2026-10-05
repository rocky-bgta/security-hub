import { Status } from './Global';

export interface IPolicyTypesPayload {
  name: string;
  code: string;
  status: Status;
}

export interface IPolicyTypes extends IPolicyTypesPayload {
  id: string;
  name: string;
  code: string;
  createdAt?: string;
  updatedAt?: string;
}
