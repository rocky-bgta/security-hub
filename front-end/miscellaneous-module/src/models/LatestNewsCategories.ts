import { Status } from './Global';

export interface ILatestNewsCategoriesPayload {
  name: string;
  description: string;
  status: Status;
}

export interface ILatestNewsCategories extends ILatestNewsCategoriesPayload {
  id: string;
  createdAt?: string;
  updatedAt?: string;
}
