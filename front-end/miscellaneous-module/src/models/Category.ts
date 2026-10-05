import { Status } from './Global';

export interface ICategoryPayload {
  categoryName: string;
  description: string;
  sortOrder: number;
}

export interface ICategory extends ICategoryPayload {
  id: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ILatestNewsCategory {
  id: string;
  name: string;
  description: string;
  status: Status;
}
