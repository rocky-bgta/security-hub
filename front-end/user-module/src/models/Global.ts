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
  startDate?: string;
  endDate?: string;
  department?: string | Array<string>;
  clientAdminId?: string;
  actionType?: string;
  username?: string;
}

export enum Status {
  ALL = '',
  COMPLETED = 'COMPLETED',
  PENDING = 'PENDING',
  DISABLED = 'DISABLED',
  ENABLED = 'ENABLED',
  EXPIRED = 'EXPIRED',
  PAID = 'PAID',
  FAILED = 'FAILED',
  INACTIVE = 'INACTIVE',
  ACTIVE = 'ACTIVE',
  EXISTING = 'EXISTING',
  SUSPEND = 'SUSPEND',
}

export enum CourseStatus {
  ALL = '',
  COMPLETED = 'COMPLETED',
  PENDING = 'PENDING',
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
}

export enum RiskGroup {
  LOW_LEVEL = 'LOW_LEVEL',
  MEDIUM_LEVEL = 'MEDIUM_LEVEL',
  HIGH_LEVEL = 'HIGH_LEVEL',
  CRITICAL_LEVEL = 'CRITICAL_LEVEL',
}

export enum ValidityUnit {
  MONTH = 'MONTH',
  YEAR = 'YEAR',
}

export enum ActivityType {
  USER_CREATED = 'USER_CREATED',
  USER_UPDATED = 'USER_UPDATED',
  USER_DELETED = 'USER_DELETED',
  USER_STATUS_CHANGED = 'USER_STATUS_CHANGED',
  USER_LOGIN = 'USER_LOGIN',
  USER_LOGOUT = 'USER_LOGOUT',
  PASSWORD_CHANGED = 'PASSWORD_CHANGED',
  PASSWORD_RESET = 'PASSWORD_RESET',
  LICENSE_ALLOCATED = 'LICENSE_ALLOCATED',
  LICENSE_DEALLOCATED = 'LICENSE_DEALLOCATED',
  LICENSE_TRANSFERRED = 'LICENSE_TRANSFERRED',
  MSP_CREATED = 'MSP_CREATED',
  MSP_UPDATED = 'MSP_UPDATED',
  MSP_STATUS_CHANGED = 'MSP_STATUS_CHANGED',
  MSP_TIER_CHANGED = 'MSP_TIER_CHANGED',
  CLIENT_CREATED = 'CLIENT_CREATED',
  CLIENT_UPDATED = 'CLIENT_UPDATED',
  CLIENT_ASSIGNED = 'CLIENT_ASSIGNED',
  CLIENT_REMOVED = 'CLIENT_REMOVED',
  CLIENT_STATUS_CHANGED = 'CLIENT_STATUS_CHANGED',
  PRODUCT_ASSIGNED = 'PRODUCT_ASSIGNED',
  PRODUCT_REMOVED = 'PRODUCT_REMOVED',
  PRODUCT_UPDATED = 'PRODUCT_UPDATED',
  CREDIT_ENABLED = 'CREDIT_ENABLED',
  CREDIT_DISABLED = 'CREDIT_DISABLED',
  CREDIT_UPDATED = 'CREDIT_UPDATED',
}

export enum ClientActivityType {
  USER_CREATED = 'USER_CREATED',
  USER_UPDATED = 'USER_UPDATED',
  USER_STATUS_CHANGED = 'USER_STATUS_CHANGED',
  USER_LOGIN = 'USER_LOGIN',
  USER_LOGOUT = 'USER_LOGOUT',
  PASSWORD_CHANGED = 'PASSWORD_CHANGED',
  PASSWORD_RESET = 'PASSWORD_RESET',
}

export enum UserType {
  ASPIRE_ADMIN = 'ASPIRE_ADMIN',
  MSP = 'MSP',
  MSP_ADMIN = 'MSP_ADMIN',
  CLIENT = 'CLIENT',
  CLIENT_ADMIN = 'CLIENT_ADMIN',
  USER = 'USER',
  SUPER_ADMIN = 'SUPER_ADMIN',
  FINANCE_ADMIN = 'FINANCE_ADMIN',
  NONE = 'none',
}

export enum UserActivityType {
  LOGIN = 'USER_LOGIN',
  LOGOUT = 'USER_LOGOUT',
  PASSWORD_CHANGED = 'PASSWORD_CHANGED',
  PASSWORD_RESET = 'PASSWORD_RESET',
}

export enum PaymentMethod {
  BANK_TRANSFER = 'BANK_TRANSFER',
  CHECK_PAYMENT = 'CHECK_PAYMENT',
}