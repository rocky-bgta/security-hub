import { IList } from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';

export type DepartmentFilterChangeHandler = (
  newValue: TMultiValue<ISelectOption> | TSingleValue<ISelectOption>,
) => void;

export interface IEndUser {
  id: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  department: string;
  status: EndUserStatus;
  clientAdminId: string;
  lastLoginAt: string;
  riskGroup: RiskGroup;
}

export enum EndUserStatus {
  ALL = 'ALL',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPEND = 'SUSPEND',
}

export enum RiskGroup {
  ALL = 'ALL',
  HIGH_RISK = 'HIGH_RISK',
  MEDIUM_RISK = 'MEDIUM_RISK',
  LOW_RISK = 'LOW_RISK',
}

export interface IEndUserQueryParams {
  clientAdminId: string;
  search: string;
  status: EndUserStatus | '';
  departments: string[];
  riskGroup: RiskGroup | '';
  offset: number;
  pageSize: number;
  productPackageId: string;
}

export interface IDepartmentOption {
  id: string;
  name: string;
}

export interface IDepartmentUserCount {
  departmentName: string;
  userCount: number;
}

export interface IRiskGroupUserCount {
  riskGroup: RiskGroup;
  userCount: number;
}

export interface IAudienceDirectory {
  users: IList<IEndUser>;
  usersLoading: boolean;
  departmentList: IDepartmentOption[];
  departmentUserCounts: IDepartmentUserCount[];
  riskGroupUserCounts: IRiskGroupUserCount[];
  departmentFilter: ISelectOption[];
  riskGroupFilter: string;
  searchInput: string;
  setSearchInput: (value: string) => void;
  queryParams: IEndUserQueryParams;
  clientAdminId: string;
  fetchUsers: () => Promise<void>;
  refreshDirectory: () => Promise<void>;
  loadLicensedAudience: (campaignId: string) => Promise<void>;
  handlePageChange: (page: number) => void;
  handlePageSizeChange: (pageSize: number) => void;
  handleRiskGroupChange: (value: string) => void;
  handleDepartmentFilterChange: DepartmentFilterChangeHandler;
  handleResetFilters: () => void;
}
