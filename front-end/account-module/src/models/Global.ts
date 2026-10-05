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

export interface IDropdownData {
  organizationTypes: Array<{ id: string; organizationType: string }>;
  countries: Array<{ id: string; name: string; code?: string }>;
  states: Array<{ id: string; name: string; countryId: string; code?: string }>;
  timeZones: Array<{
    id: string;
    name: string;
    countryId: string;
    stateId: string;
  }>;
  languages: Array<{ id: string; name: string }>;
  industries: Array<{ id: string; name: string }>;
  organizationSizes: Array<{ id: string; name: string }>;
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
}

export enum Status {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
}

export enum CourseStatus {
  ALL = '',
  COMPLETED = 'COMPLETED',
}

export enum FileType {
  CONTENT = 'CONTENT',
  LOGO = 'LOGO',
  THUMBNAIL = 'THUMBNAIL',
  PROFILE_IMAGE = 'PROFILE_IMAGE',
}
