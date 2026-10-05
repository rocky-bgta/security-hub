export const RADIAN = Math.PI / 180;

export const DOT = -1;

export const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const PUBLIC_URL = import.meta.env.VITE_PUBLIC_URL;

export const UPLOAD_URL = import.meta.env.VITE_API_UPLOAD_URL;

export const UPLOAD_PATH_URL = import.meta.env.VITE_API_UPLOAD_PATH_URL;

export const FILE_PATH_PREFIX = 'https://content.aspireelearning.com/';

export const DEFAULT_PAGINATION_OFFSET = 0;

export const DEFAULT_PAGINATION_LIMIT = 10;

export const DEFAULT_PAGINATION_PER_PAGE = [
  {
    id: 10,
    value: '10',
    label: '10',
  },
  {
    id: 15,
    value: '15',
    label: '15',
  },
  {
    id: 20,
    value: '20',
    label: '20',
  },
];

export const InitGetListParams = {
  offset: DEFAULT_PAGINATION_OFFSET,
  pageSize: DEFAULT_PAGINATION_LIMIT,
};
