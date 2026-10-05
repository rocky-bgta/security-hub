export interface IResponse<T> {
  message: string;
  statusCode: number;
  data: T;
}

export interface IList<T> {
  offset: number;
  pageSize: number;
  total: number;
  items: Array<T>;
}

export interface IGetListParams {
  offset?: number;
  pageSize?: number;
  search?: string;
  courseId?: string;
  userId?: string;
  packageId?: string;
  status?: Status;
  isSaved?: boolean;
  contentId?: string;
  searchParam?: string;
  isActive?: string | boolean;
  isExpired?: string | boolean;
  clientId?: string;
  clientName?: string;
  startDate?: string;
  endDate?: string;
  countryFilter?: string;
  vatRate?: string;
  type?: string;
  mspId?: string;
  countryId?: string;
  roleType?: string;
  couponType?: string;
  productId?: string;
  mspTier?: string;
  order?: OrderType;
  sortBy?: string;
}

export enum Status {
  ALL = 'ALL',
  COMPLETED = 'COMPLETED',
  PENDING = 'PENDING',
  ON_PROGRESS = 'ON_PROGRESS',
  DISABLED = 'DISABLED',
  ENABLED = 'ENABLED',
  EXPIRED = 'EXPIRED',
  PAID = 'PAID',
  FAILED = 'FAILED',
  INACTIVE = 'INACTIVE',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  CANCELLED = 'CANCELLED',
  SUCCESS = 'SUCCESS',
  PARTIAL = 'PARTIAL',
  OVERDUE = 'OVERDUE',
}

export enum CourseStatus {
  ALL = '',
  COMPLETED = 'COMPLETED',
  PENDING = 'PENDING',
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
}

export enum RoleType {
  MSP = 'MSP',
  CLIENT = 'CLIENT',
}

export enum ModalType {
  ADD = 'ADD',
  EDIT = 'EDIT',
  VIEW = 'VIEW',
  DELETE = 'DELETE',
  COMMENT = 'COMMENT',
  SEND_EMAIL = 'SEND_EMAIL',
  PAYMENT_NOW = 'PAYMENT_NOW',
  NONE = 'NONE',
}

export interface IDropdownOption {
  value: string;
  label: string;
}

export enum ValidityUnit {
  MONTH = 'MONTH',
  YEAR = 'YEAR',
}

export enum OrderType {
  ASC = 'asc',
  DESC = 'desc',
}