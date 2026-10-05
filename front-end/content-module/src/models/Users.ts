import { RiskGroup, Status } from './Global';

export interface IUser {
  id: string;
  fullName: string;
  email: string;
  status: string;
  phoneNumber: string;
  department: string;
}

export interface IUserInfo {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  department: string;
  countryName: string;
  countryCode: string;
  status: Status;
  clientAdminId: string;
  riskGroup: RiskGroup;
  lastLoginAt: string;
  organizationName: string;
  designation: string;
  address: string;
  timeZone: string;
  language: string;
  profilePicture: string;
}

export interface IUserTranscript {
  topicName: string;
  completionDate: string;
}

export interface IUserTopicsProgress {
  topicId: string;
  name: string;
  start_date: string;
  expire_date: string;
  progress: string;
}

export interface IActivityLog {
  id: string;
  actionType: string;
  timestamp: string;
  userType: string;
  deviceInfo: string;
  ipAddress: string;
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
