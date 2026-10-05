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
  status?: CourseStatus;
  isSaved?: boolean;
  contentId?: string;
  filterType?: string;
  productId?: string;
  group?: string;
  department?: string;
  clientAdminId?: string;
  subPackageId?: string;
}

export enum Status {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
  INACTIVE = 'INACTIVE',
  ACTIVE = 'ACTIVE',
}

export enum CourseStatus {
  ALL = '',
  COMPLETED = 'COMPLETED',
}

export enum ClientProductTag {
  PHISHING = 'Phishing',
  SECURITY = 'Security',
}