import { RiskGroup, Status } from './Global';

export interface ISystemUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  companyName: string;
  designation: string;
  department: string;
  country: string;
  zipCode: string;
  supervisorName: string;
  status: Status;
  riskGroup: RiskGroup;
  roles: Array<string>;
  createdAt: string;
  lastLoginAt: string;
}

export interface IUser {
  id: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  department: string;
  riskGroup: string;
  status: Status;
  clientAdminId: string;
}

export interface IUserData {
  id: string;
  name: string;
  firstName: string;
  lastName: string;
  email: string;
  companyName: string;
  department: string;
  designation: string;
  country: string;
  role: string;
  userStatus: string;
  supervisorName: string;
  supervisorEmail: string;
  group: string;
  zipCode: string;
}

export interface IUserDataResponse {
  data: IUserData[];
}

export interface IActivityLog {
  id: string;
  actionType: string;
  timestamp: string;
  userType: string;
  deviceInfo: string;
  ipAddress: string;
  username: string;
}

export interface IActivityLogItem {
  activityId: string;
  userId: string;
  userType: string;
  userName: string;
  userEmail: string;
  fullName: string;
  activityType: string;
  activityDescription: string;
  activityStatus: string;
  timestamp: string;
  ipAddress: string;
  description: string;
  oldValue: string;
  newValue: string;
}

export interface UserRangePricing {
  id: string;
  packageId: string;
  userRangeId: string;
  rangeName: string;
  minUsers: number;
  maxUsers: number;
  pricePerUser: number;
  yearlyPricePerUser: number;
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
}
