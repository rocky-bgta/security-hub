export interface IPackageAssignmentItem {
  user: string;
  package: string;
  assignedDate: string;
  expiryDate: string;
  status: string;
  assignedBy: string;
}

export interface IPackageAssignmentReportData {
  totalPackages: number;
  activeAssignments: number;
  expiringSoon: number;
  expired: number;
  assignments: IPackageAssignmentItem[];
}

export interface ITopicProgressItem {
  topic: string;
  completed: number;
  assigned: number;
  avgProgress: number;
}

export interface ITopicAssignmentReportData {
  totalTopics: number;
  activeAssignments: number;
  completedTopics: number;
  avgProgress: number;
  topics: ITopicProgressItem[];
}

export interface ISubPackageItem {
  name: string;
  parentPackage: string;
  users: number;
  usage: number;
  lastAccessed: string;
}

export interface ISubPackageReportData {
  totalSubPackages: number;
  activeUsage: number;
  underutilized: number;
  subPackages: ISubPackageItem[];
}
