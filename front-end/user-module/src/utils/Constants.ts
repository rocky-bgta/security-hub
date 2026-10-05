export const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const PUBLIC_URL = import.meta.env.VITE_PUBLIC_URL;
export const FILE_PATH_PREFIX = 'https://content.aspireelearning.com/';
export const BULK_IMPORT_TEMPLATE_URL = 'https://aspiretss.s3.us-east-1.amazonaws.com/a-sat-v2.0/Bulk+Import/Bulk_user_import_template_data.csv';

export const DOT = -1;

export const DEFAULT_PAGINATION_OFFSET = 0;

export const DEFAULT_PAGINATION_LIMIT = 10;

export const InitGetListParams = {
  offset: DEFAULT_PAGINATION_OFFSET,
  pageSize: DEFAULT_PAGINATION_LIMIT,
};
