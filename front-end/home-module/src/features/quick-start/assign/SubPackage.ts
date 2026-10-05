import { ITopicDetails } from './Course';
import { IUserPackageDetails } from './Package';
import { IProductDetails } from './Product';

export interface ISubPackageDetails {
  id: string;
  name: string;
  description: string;
  clientId: string;
  clientAdminId: string | null;
  createdBy: string | null;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
  updatedAt: string;
  productDetails: IProductDetails;
  packageDetails: IUserPackageDetails;
  topicDetails: ITopicDetails[];
  assignedUserCount: number;
}
