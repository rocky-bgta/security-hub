import { Status } from 'models/Global';
import { INewPackageData, IPackage } from './Package';

export interface IProduct {
  productId: string;
  productName: string;
  productDescription: string;
  productStatus: Status;
  thumbnailUrl: string | null;
  createdAt: string;
  updatedAt: string;
  lastModifiedBy: string;
  packages: IPackage[];
  tags: Array<string>;
}

export interface IProductDetails {
  id: string;
  productName: string;
  productDescription: string;
  productStatus: Status;
  courseIds: Array<{
    id: string;
    courseName: string;
    courseDescription: string;
  }>;
  createdAt: string;
  updatedAt: string;
  lastModifiedBy: string;
}

export interface INewProductData {
  productName: string;
  productDescription: string;
  displayOrder?: number;
  tags: Array<string>;
  packages: Array<INewPackageData>;
  productStatus: Status;
}
