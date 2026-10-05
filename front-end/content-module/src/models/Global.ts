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
  userId?: string;
  packageId?: string;
  status?: CourseStatus;
  isSaved?: boolean;
  contentId?: string;
  clientAdminId?: string;
  mspId?: string;
  countryId?: string;
  department?: string;
  riskLevel?: string;
  statusFilter?: string;
  productId?: string;
  topicId?: string;
  group?: string;
  assigned?: string;
  subPackageId?: string;
  bookmarked?: boolean;
  certificateName?: string;
  isClientAdmin?: boolean;
  filterType?: string;
  startDate?: string;
  endDate?: string;
  username?: string;
  actionType?: string;
  pollId?: string;
  answerId?: string;
  questionId?: string;
  mspProductStatus?: string;
}

export enum UserActivityType {
  LOGIN = 'USER_LOGIN',
  LOGOUT = 'USER_LOGOUT',
  PASSWORD_CHANGED = 'PASSWORD_CHANGED',
  PASSWORD_RESET = 'PASSWORD_RESET',
}

export enum Status {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum CourseStatus {
  ALL = '',
  COMPLETED = 'COMPLETED',
  PENDING = 'PENDING',
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
  EXPIRED = 'EXPIRED',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum RiskGroup {
  CRITICAL_RISK = 'CRITICAL_RISK',
  HIGH_RISK = 'HIGH_RISK',
  LOW_RISK = 'LOW_RISK',
  MEDIUM_RISK = 'MEDIUM_RISK',
  ALL = 'ALL',
}

export enum FileType {
  CONTENT = 'CONTENT',
  LOGO = 'LOGO',
  THUMBNAIL = 'THUMBNAIL',
  PROFILE_IMAGE = 'PROFILE_IMAGE',
}

export enum ModalType {
  ADD = 'ADD',
  EDIT = 'EDIT',
  VIEW = 'VIEW',
  DELETE = 'DELETE',
  COMMENT = 'COMMENT',
  SEND_EMAIL = 'SEND_EMAIL',
  NONE = 'NONE',
}
