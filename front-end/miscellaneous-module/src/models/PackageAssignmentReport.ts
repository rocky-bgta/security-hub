import { IList } from 'models/Global';

export interface IPackageAssignmentSummary {
  totalPackages: number;
  totalSubPackages: number;
  activeAssignments: number;
  expiringSoon: number;
  expired: number;
  completeAssignments: number;
}

export interface IPackageAssignmentLogItem {
  user: string;
  packageName: string;
  subPackageName: string;
  assignedDate: string;
  expiryDate: string;
  status: string;
  assignedBy: string;
}

export interface IPackageAssignmentReportData {
  summary: IPackageAssignmentSummary;
  assignmentLog: IList<IPackageAssignmentLogItem>;
}
