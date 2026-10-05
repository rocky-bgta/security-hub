import { BreachStatus, RecipientBreachStatus } from 'models/Breach';
import { CampaignChannel, CampaignStatus } from 'models/Campaign';

/**
 * Standard API response wrapper
 */
export interface IResponse<T> {
  message: string;
  statusCode: number;
  data: T;
}

/** Active language row from `/registration/api/v1/dropdown/languages/active` */
export interface IActiveLanguage {
  id: string;
  code: string;
  displayName: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Paginated list response
 */
export interface IList<T> {
  offset: number;
  pageSize: number;
  total: number;
  items: Array<T>;
}

/**
 * Common query parameters for list endpoints
 */
export interface IGetListParams {
  offset?: number;
  pageSize?: number;
  search?: string;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
  status?: CampaignStatus | RecipientBreachStatus | BreachStatus | '';
  days?: string;
  format?: string;
  domain?: string;
  searchParam?: string;
  fromDate?: string;
  toDate?: string;
  isActive?: boolean;
  sortOrder?: 'asc' | 'desc';
  email?: string;
  clientAdminId?: string;
  channel?: CampaignChannel;
  limit?: number;
  type?: string;
}

/**
 * Default list parameters
 */
export const InitGetListParams: IGetListParams = {
  offset: 0,
  pageSize: 10,
  search: '',
  sortBy: 'createdAt',
  sortDirection: 'desc',
  status: '',
};

export enum PageTypeEnum {
  LANDING_PAGE = 'LANDING_PAGE',
  PAGE_NOT_FOUND_404 = 'PAGE_NOT_FOUND_404',
  CUSTOM = 'CUSTOM',
}

export enum FileType {
  CONTENT = 'CONTENT',
  LOGO = 'LOGO',
  THUMBNAIL = 'THUMBNAIL',
  PROFILE_IMAGE = 'PROFILE_IMAGE',
}

export enum TemplateStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DRAFT = 'DRAFT',
  PROCESSING = 'PROCESSING',
  FAILED = 'FAILED',
}
