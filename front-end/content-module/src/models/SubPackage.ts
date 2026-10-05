import { ISelectTopic, ITopicDetails } from './Course';
import { RiskGroup, Status } from './Global';
import { IUserPackageDetails } from './Package';
import { IProductDetails } from './Product';

export interface ISubPackageDetails {
  id: string;
  name: string;
  description: string;
  clientId: string;
  clientAdminId: string;
  productId: string;
  productName: string;
  packageId: string;
  packageName: string;
  createdBy: string;
  status: Status;
  createdAt: string;
  updatedAt: string;
  productDetails: IProductDetails;
  packageDetails: IUserPackageDetails;
  topicDetails: ITopicDetails[];
  assignedUserCount: number;
}

export interface ISubPackageAssignedUser {
  userId: string;
  email: string;
  fullName: string;
  department: string;
  riskGroup: RiskGroup;
  subPackageName: string;
  status: string;
  assignedAt: string;
  expiryDate: string;
  progress: number;
}

export interface ICreateSubPackageFormData {
  productPackage: {
    productId: string;
    packageId: string;
    productPackageId: string;
  };
  subPackageName: { name: string; description: string; status?: string };
  topics: Array<ISelectTopic>;
}
