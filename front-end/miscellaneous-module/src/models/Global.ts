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
  sortBy?: string;
  order?: OrderType;
  priority?: string;
  status?: string;
  category?: string;
  assignCategory?: string;
  clientId?: string;
  createdBy?: string;
  supportType?: string;
  mspId?: string;
  policyName?: string;
  isSystemDefined?: string;
  active?: string;
  isActive?: string;
  categoryId?: string;
  type?: string;
  resourceType?: string;
  industryId?: string;
  countryId?: string;
  isOwnPolicy?: string;
}

export enum OrderType {
  ASC = 'asc',
  DESC = 'desc',
}

export enum Status {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DRAFT = 'DRAFT',
}

export enum VideoType {
  UPLOAD_FILE = 'UPLOAD_FILE',
  YOUTUBE_URL = 'YOUTUBE_URL',
  VIMEO_URL = 'VIMEO_URL',
}

export enum ModalType {
  ADD = 'ADD',
  EDIT = 'EDIT',
  VIEW = 'VIEW',
  DELETE = 'DELETE',
  FORWARD = 'FORWARD',
  SOLVE = 'SOLVE',
  NONE = 'NONE',
}

export enum FileType {
  CONTENT = 'CONTENT',
  LOGO = 'LOGO',
  THUMBNAIL = 'THUMBNAIL',
}
